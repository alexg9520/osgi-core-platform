package media.alera.osgi.core.shared.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.event.artemis.ArtemisEventProcessor;

@Slf4j
public class PostEventEclipseJob extends AbstractInternalQueueEclipseJob<JmsTextMessage, AutoCloseable> implements IEventProcessor {

  private ArtemisEventProcessor eventProcessor;

  private CoreVariableService vs;

  private String destinationName;

  private JsonFactory factory;

  private boolean isQueue;

  public PostEventEclipseJob(final String destinationName, final CoreVariableService vs, final JsonFactory factory, final boolean isQueue) {
    super(String.format("Post Events Job (%s)", destinationName));
    this.destinationName = destinationName;
    this.vs = vs;
    this.factory = factory;
    this.isQueue = isQueue;
    this.eventProcessor = new ArtemisEventProcessor(this, isQueue);
  }

  public boolean isQueue() {
    return this.isQueue;
  }

  public String getDestinationName() {
    return this.destinationName;
  }

  @Override
  public void processItem(final JmsTextMessage eventMsg, final Map<String, Object> mapQueueData) throws CoreException {
    Session session = (Session) mapQueueData.get("session");
    MessageProducer producer = (MessageProducer) mapQueueData.get("producer");
    try {
      TextMessage msg = session.createTextMessage();
      msg.setText(this.factory.toString(eventMsg.msg()));
      msg.setStringProperty(EventTopics.PARAM_EVENT_TOPIC, eventMsg.topic());
      if (eventMsg.sourceId() != null) {
        msg.setStringProperty(EventTopics.PARAM_EVENT_SOURCE_ID, eventMsg.sourceId());
      }
      producer.send(msg);
    } catch (JMSException jmsEx) {
      log.error("Failed to send message", jmsEx);
      throw new CoreException("Failed to send message", jmsEx);
    }
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

  @Override
  public int setupAndProcessData(final IProgressMonitor monitor, final AutoCloseable amqFactory, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    return this.eventProcessor.setupAndProcessArtemisData(monitor, amqFactory, mapQueueData, maxWaitCount);
  }

  @Override
  public void jobComplete(int waitCount, Map<String, Object> jobData, boolean hasError, boolean rescheduleForError) {
    List<JmsTextMessage> messageList = getItemList();
    synchronized (messageList) {
      if (!messageList.isEmpty()) {
        // If the queue is not empty, reschedule
        this.schedule();
        log.debug("Message queue is NOT empty. {} messages in queue. rescheduling job", messageList.size());
      } else {
        log.debug("Message queue is empty and no new ones have been seen in {} seconds, ending job", ((waitCount * getWaitCountInterval()) / 1000));
      }
    }
  }

  @Override
  public int processData(final IProgressMonitor monitor, final AutoCloseable amqFactory, final Session session, final Destination destination, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    String brokerUrl = (String) mapQueueData.get("brokerUrl");
    try {
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      log.info("Connected to broker '{}' for sending messages on {}.", brokerUrl, destination);

      mapQueueData.put("session", session);
      mapQueueData.put("producer", producer);

      // Reset wait time now that it has successfully connected
      resetErrorWaitTime();
      return processQueue(maxWaitCount, monitor, mapQueueData);
    } catch (JMSException jmsEx) {
      throw new CoreException("Broker may not be available", jmsEx);
    }
  }

}