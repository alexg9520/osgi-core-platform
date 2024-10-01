package media.alera.osgi.core.shared.event;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import jakarta.jms.Destination;
import jakarta.jms.Session;
import media.alera.osgi.core.shared.CoreException;

public interface IEventProcessor {

  public int processData(final IProgressMonitor monitor, final AutoCloseable amqFactory, Session session, Destination destination, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException;

}