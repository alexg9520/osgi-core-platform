package media.alera.osgi.core.shared.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.event.EventAdmin;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.event.artemis.ArtemisEventProcessor;

@Slf4j
public class ReceiveEventEclipseJob extends AbstractEclipseJob<AutoCloseable> implements IEventProcessor {

  private ArtemisEventProcessor eventProcessor;

  private CoreVariableService vs;

  private String destinationName;

  private EventAdmin localEventAdmin;

  private JsonFactory factory;

  private String sessionUuid;

  private boolean isQueue;

  public ReceiveEventEclipseJob(final String destinationName, final CoreVariableService vs, final JsonFactory factory, final boolean isQueue, final EventAdmin localEventAdmin, final String sessionUuid) {
    super(String.format("Receive Events Job (%s)", destinationName));
    this.destinationName = destinationName;
    this.vs = vs;
    this.factory = factory;
    this.isQueue = isQueue;
    this.localEventAdmin = localEventAdmin;
    this.sessionUuid = sessionUuid;
    this.eventProcessor = new ArtemisEventProcessor(this, isQueue);
  }

  public boolean isQueue() {
    return this.isQueue;
  }

  public String getDestinationName() {
    return this.destinationName;
  }

  private void receiveMessages(final MessageConsumer consumer, final IProgressMonitor monitor) throws JMSException, CoreException {
    while (!monitor.isCanceled()) {
      Message message = consumer.receive(1000);
      if (isJobCanceled(monitor)) {
        return;
      }

      if (message != null) {
        processMessageType(message);
      }

      if (isJobCanceled(monitor)) {
        return;
      }
    }
  }

  private void processMessageType(final Message message) throws CoreException, JMSException {
    if (message instanceof TextMessage txtMsg) {
      boolean processMessage = processTextMessage(txtMsg);
      if (processMessage) {
        txtMsg.acknowledge();
      }
    } else {
      log.warn("Unknown Message type: {}", message.getClass().getName());
      throw new CoreException("Unknown message type: " + message);
    }
  }

  private boolean processTextMessage(final TextMessage txtMsg) throws CoreException {
    try {
      String text = txtMsg.getText();
      String topic = txtMsg.getStringProperty(EventTopics.PARAM_EVENT_TOPIC);
      String eventSourceId = txtMsg.getStringProperty(EventTopics.PARAM_EVENT_SOURCE_ID);

      // Don't process events from the same source since they were already published and handled by the local event bus
      if (!this.isQueue && !topic.startsWith(EventTopics.EVENT_TEST_MESSAGE) && eventSourceId != null && eventSourceId.equalsIgnoreCase(this.sessionUuid)) {
        log.trace("Ingoring event from: {}", eventSourceId);
        return false;
      }

      postToLocalEventBus(text, topic, eventSourceId);

      return true;
    } catch (JMSException jmsEx) {
      throw new CoreException("Failed to process message", jmsEx);
    }
  }

  private void postToLocalEventBus(final String text, final String topic, final String eventSourceId) throws CoreException {
    if (this.localEventAdmin != null) {
      IJsonEventData data = this.factory.fromString(text, IJsonEventData.class);
      this.localEventAdmin.postEvent(EventBus.createEvent(topic, data, eventSourceId));
    } else {
      log.warn("Local event bus is not ready in Event Consumer");
    }
  }

  @Override
  public int processData(final IProgressMonitor monitor, final AutoCloseable amqFactory, final Session session, final Destination destination, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    String brokerUrl = EventBusProperties.getEventBusAddress(this.vs);
    try {
      MessageConsumer consumer = session.createConsumer(destination);
      log.info("Connected to broker '{}' for receiving messages on {}.", brokerUrl, destination);

      // Reset wait time now that it has successfully connected
      resetErrorWaitTime();
      receiveMessages(consumer, monitor);
      return -1;
    } catch (JMSException jmsEx) {
      throw new CoreException("Broker may not be available", jmsEx);
    }
  }

  @Override
  public int setupAndProcessData(final IProgressMonitor monitor, final AutoCloseable amqFactory, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    return this.eventProcessor.setupAndProcessArtemisData(monitor, amqFactory, mapQueueData, maxWaitCount);
  }

  @Override
  public void jobComplete(final int waitCount, final Map<String, Object> jobData, final boolean hasError, final boolean rescheduleForError) {
    // Schedule a new job
    this.schedule(100);
    log.debug("Rescheduling job for receiving messages from event bus {}", this.getName());
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    Map<String, Object> mapQueueData = new HashMap<>();
    if (this.destinationName == null) {
      // No destination was set in the connector, use default
      this.destinationName = EventBusProperties.getCoreDestinationQueue(this.vs);
    }
    mapQueueData.put(PARAM_SERVICE_MAX_WAIT_COUNT, EventBusProperties.getMaxWaitCount(this.vs));
    mapQueueData.put("destinationName", this.destinationName);
    mapQueueData.put("brokerUrl", EventBusProperties.getEventBusAddress(this.vs));

    Optional<String> eventBusUserName = EventBusProperties.getEventBusUserName(this.vs);
    Optional<byte[]> eventBusPassword = EventBusProperties.getEventBusPassword(this.vs);
    if (eventBusUserName.isPresent() && eventBusPassword.isPresent()) {
      mapQueueData.put("eventBusUserName", eventBusUserName.get());
      mapQueueData.put("eventBusPassword", eventBusPassword.get());
    }
    return executeJob(monitor, mapQueueData);
  }

  @Override
  public AutoCloseable createConnection(final Map<String, Object> mapQueueData) throws CoreException {
    return this.eventProcessor.createConnection(mapQueueData);
  }

}