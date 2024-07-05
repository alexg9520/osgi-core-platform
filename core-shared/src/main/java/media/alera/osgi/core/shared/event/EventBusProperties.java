package media.alera.osgi.core.shared.event;

import java.util.Optional;

import media.alera.osgi.core.init.CoreVariableService;

public class EventBusProperties {

  public static final String CORE_DEFAULT_MESSAGE_TOPIC_NAME = "media.alera.osgi.core.events";

  public static final String CORE_DEFAULT_CORE_DESTINATION_QUEUE_NAME = "media.alera.osgi.core.events.process";

  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PORT = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PORT";
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_SERVER = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_SERVER";
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TYPE = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TYPE";
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_OPTIONS = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_OPTIONS";

  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TOPIC = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TOPIC";
  public static final String CORE_ACTIVEMQ_EVENTBUS_DEFAULT_CORE_QUEUE = "CORE_ACTIVEMQ_EVENTBUS_DEFAULT_CORE_QUEUE";
  public static final String CORE_ACTIVEMQ_EVENTBUS_DEFAULT_REANALYSIS_QUEUE = "CORE_ACTIVEMQ_EVENTBUS_DEFAULT_REANALYSIS_QUEUE";

  public static final String CORE_ACTIVEMQ_EVENTBUS_PROGRESS_QUEUE = "CORE_ACTIVEMQ_EVENTBUS_PROGRESS_QUEUE";
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_USERNAME = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_USERNAME";
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PASSWORD = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PASSWORD";

  // Maximum wait time until the send messages job is shutdown do to no activity
  public static final String CORE_ACTIVEMQ_EVENTBUS_CONNECTION_MAX_WAIT_NEW_MSGS = "CORE_ACTIVEMQ_EVENTBUS_CONNECTION_MAX_WAIT_NEW_MSGS";

  public static String getEventBusAddress(final CoreVariableService variableService) {
    String port = variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PORT, "61616");
    String server = variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_SERVER, "localhost");
    String type = variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TYPE, "tcp");
    String options = variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_OPTIONS, "sslEnabled=true&socket.verifyHostName=true");

    String brokerUrl = type + "://" + server + ":" + port;
    if (!options.isBlank()) {
      brokerUrl += "?" + options;
    }
    return brokerUrl;
  }

  public static String getEventBusTopic(final CoreVariableService variableService) {
    return variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_TOPIC, CORE_DEFAULT_MESSAGE_TOPIC_NAME);
  }

  public static String getCoreDestinationQueue(final CoreVariableService variableService) {
    return variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_DEFAULT_CORE_QUEUE, CORE_DEFAULT_CORE_DESTINATION_QUEUE_NAME);
  }

  public static String getEventBusAddressWithTopic(final CoreVariableService variableService) {
    return getEventBusAddress(variableService) + "/" + getEventBusTopic(variableService);
  }

  public static Optional<String> getEventBusUserName(final CoreVariableService variableService) {
    return Optional.ofNullable(variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_USERNAME));
  }

  public static Optional<byte[]> getEventBusPassword(final CoreVariableService variableService) {
    String property = variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_PASSWORD);
    if (property == null) {
      return Optional.empty();
    }

    return Optional.of(property.getBytes());
  }

  public static int getMaxWaitCount(final CoreVariableService variableService) {
    return Integer.parseInt(variableService.getEnvSystemProperty(EventBusProperties.CORE_ACTIVEMQ_EVENTBUS_CONNECTION_MAX_WAIT_NEW_MSGS, "600"));
  }
}