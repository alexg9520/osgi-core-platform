package media.alera.osgi.core.shared.event;

import java.util.HashMap;
import java.util.UUID;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.shared.CoreSystemStatus;
import media.alera.osgi.core.shared.CoreUtils;

@Slf4j
@Component(immediate = true, service = { EventBus.class, EventHandler.class }, property = { EventConstants.EVENT_TOPIC + "=" + EventTopics.EVENT_TEST_MESSAGE })
@GogoCommand(scope = "eventtests", function = { "sendEventTest", "sendTaskTest" })
public class EventBus implements EventHandler {

  private static final String DESTINATION_WAS_NOT_SET = "Destination was not set";

  private static final String FAILED_TO_SEND_EVENT = "Failed to send event";

  private EventBusMessageProducer eventBusMessageProducer;

  private EventBusMessageConsumer eventBusMessageConsumer;

  private EventAdmin localEventAdmin;

  private CommandSession session = null;

  @Activate
  public void activate() {
    log.debug("Eventbus Activated");
  }

  @Deactivate
  public void deactivate() {
    log.debug("Eventbus Deactivated");
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void setEventBusMessageProducer(final EventBusMessageProducer messageProducer) {
    this.eventBusMessageProducer = messageProducer;
    log.debug("Event Bus Message Producer set in EventBus");
  }

  @SuppressWarnings("java:S1172")
  public void unsetEventBusMessageProducer(final EventBusMessageProducer messageProducer) {
    this.eventBusMessageProducer = null;
    log.debug("Event Bus Message Producer unset in EventBus");
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void setEventBusMessageConsumer(final EventBusMessageConsumer messageConsumer) {
    this.eventBusMessageConsumer = messageConsumer;
    log.debug("Event Bus Message Consumer set in EventBus");
  }

  @SuppressWarnings("java:S1172")
  public void unsetEventBusMessageConsumer(final EventBusMessageConsumer messageConsumer) {
    this.eventBusMessageConsumer = null;
    log.debug("Event Bus Message Consumer in EventBus");
  }

  @Reference(cardinality=ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
  public void setLocalEventAdmin(final EventAdmin eventAdmin) {
    this.localEventAdmin = eventAdmin;
    log.debug("Local event bus set in EventBus");
  }

  @SuppressWarnings("java:S1172")
  public void unsetLocalEventAdmin(final EventAdmin eventAdmin) {
    this.localEventAdmin = null;
    log.debug("Local event bus unset in EventBus");
  }

  public boolean postEvent(final String topic, final IJsonData data) {
    return this.postEvent(topic, data, false);
  }

  public boolean postTask(final String topic, final String destination, final IJsonData data) {
    return this.eventBusMessageProducer.sendTask(topic, destination, data);
  }

  public boolean postEvent(final String topic, final IJsonData data, final boolean sendRemote) {
    if (this.eventBusMessageProducer == null || !sendRemote) {
      this.localEventAdmin.postEvent(createEvent(topic, data, null));
      return true;
    }

    this.localEventAdmin.postEvent(createEvent(topic, data, null));
    return this.eventBusMessageProducer.sendEvent(topic, data);
  }

  public static Event createEvent(final String topic, final IJsonData data, final String sourceId) {
    HashMap<String, Object> props = new HashMap<>();
    props.put(EventConstants.EVENT_TOPIC, topic);
    if (data != null) {
      props.put(EventTopics.PARAM_EVENT_MSG_DATA, data);
    }
    if (sourceId != null) {
      props.put(EventTopics.PARAM_EVENT_SOURCE_ID, sourceId);
    }
    return new Event(topic, props);
  }

  @Descriptor("Send a test event to the remote event bus and then it should posted to the console when it is consumed")
  public void sendEventTest(final CommandSession session, @Descriptor("the message to send") final String msg) {
    try {
      this.session = session;
      if (msg != null && !msg.isBlank()) {
        this.eventBusMessageProducer.sendEvent(EventTopics.EVENT_TEST_MESSAGE, CoreTestEvent.builder().msg(msg).build());
      } else {
        session.getConsole().println("Message was not set");
      }
    } catch (Exception ex) {
      log.error(FAILED_TO_SEND_EVENT, ex);
    }
  }

  @Descriptor("Send a test event to a specific queue")
  public void sendTaskTest(final CommandSession session, @Descriptor("the name of the queue") final String destination) {
    try {
      final CoreTestJob taskEvent = new CoreTestJob(CoreTestJob.CURRENT_VERSION, "9221aac0-e18c-45b7-9d24-a4a9a88e2201", UUID.randomUUID().toString(), CoreUtils.getCurrentInstant());

      if (destination != null && !destination.isBlank()) {
        this.eventBusMessageProducer.sendTask(EventTopics.EVENT_TEST_MESSAGE, destination, taskEvent);
      } else {
        session.getConsole().println(DESTINATION_WAS_NOT_SET);
      }
    } catch (Exception ex) {
      log.error(FAILED_TO_SEND_EVENT, ex);
    }
  }

  @Override
  public void handleEvent(final Event event) {
    Object eventObj = event.getProperty(EventTopics.PARAM_EVENT_MSG_DATA);
    if (eventObj instanceof CoreTestEvent) {
      if (this.session != null) {
        this.session.getConsole().printf("EventBus received test message: %s", ((CoreTestEvent) eventObj).msg());
        this.session = null;
      } else {
        log.info("EventBus received test message: {}", ((CoreTestEvent) eventObj).msg());
      }
    }
  }

  public void addEventListener(final String topic, final String destination) {
    this.eventBusMessageConsumer.addEventsListener(topic, destination);
  }

  public void addTaskListener(final String topic, final String destination) {
    this.eventBusMessageConsumer.addTasksListener(topic, destination);
  }
}