package media.alera.osgi.core.shared.event;

public class EventTopics {

  public static final String PARAM_EVENT_MSG_DATA = "data";

  public static final String PARAM_EVENT_TOPIC = "topic";

  public static final String PARAM_EVENT_SOURCE_ID = "eventSourceId";

  public static final String ALERA_EVENTS = "media/alera";

  public static final String OSGI_EVENTS = ALERA_EVENTS + "/osgi";

  public static final String CORE_EVENTS = OSGI_EVENTS + "/core";

  public static final String EVENT_ACTIVITY = CORE_EVENTS + "/event";

  public static final String EVENT_TEST_MESSAGE = CORE_EVENTS + "/test/msg";

  public static final String EVENT_TASK = CORE_EVENTS + "/task";

}