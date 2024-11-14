package media.alera.osgi.core.shared.event.artemis;

import java.util.Map;
import java.util.Optional;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.event.IEventProcessor;

public class ArtemisEventProcessor {

  private IEventProcessor eventProcessor;

  private boolean isQueue;

  public ArtemisEventProcessor(final IEventProcessor eventProcessor, final boolean isQueue) {
    this.eventProcessor = eventProcessor;
    this.isQueue = isQueue;
  }

  public AutoCloseable createConnection(final Map<String, Object> mapQueueData) throws CoreException {
    String brokerUrl = (String) mapQueueData.get("brokerUrl");
    Optional<String> eventBusUserName = Optional.ofNullable((String) mapQueueData.get("eventBusUserName"));
    Optional<byte[]> eventBusPassword = Optional.ofNullable((byte[]) mapQueueData.get("eventBusPassword"));
    if (eventBusUserName.isPresent() && eventBusPassword.isPresent()) {
      ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(eventBusUserName.get(), new String(eventBusPassword.get()), brokerUrl);
      try {
        return activeMQConnectionFactory.createConnection();
      } catch (JMSException e) {
        throw new CoreException("Failed to create connection to ActiveMQ", e);
      }
    }
    throw new IllegalArgumentException("Event Bus User Name and Password must be set");
  }

  public int setupAndProcessArtemisData(final IProgressMonitor monitor, final AutoCloseable autoClose, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    // Create connection and send messages
    String destinationName = (String) mapQueueData.get("destinationName");
    try (Connection connection = ((Connection) autoClose)) {
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final Destination destination;
      if (this.isQueue) {
        destination = session.createQueue(destinationName);
      } else {
        destination = session.createTopic(destinationName);
      }

      return this.eventProcessor.processData(monitor, autoClose, session, destination, mapQueueData, maxWaitCount);

    } catch (JMSException jmsEx) {
      Connection connection = (Connection) autoClose;
      throw new CoreException("Broker may not be available: " + connection.toString()); //.getMetaData().getJMSXPropertyNames().getScheme() + "://" + connectionFactory.toURI().getHost() + ":" + connectionFactory.toURI().getPort(), jmsEx);
    }
  }

}