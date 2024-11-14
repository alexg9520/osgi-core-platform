package media.alera.osgi.core.shared.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.shared.CoreException;

@Slf4j
public abstract class AbstractInternalQueueEclipseJob<T, J extends AutoCloseable> extends AbstractEclipseJob<J> {

  private static final int WAIT_COUNT_INTERVAL = 500;

  private List<T> itemList = Collections.synchronizedList(new ArrayList<>());

  public abstract void processItem(T item, Map<String, Object> mapQueueData) throws CoreException;

  protected AbstractInternalQueueEclipseJob(final String name) {
    super(name);
  }

  public int getWaitCountInterval() {
    return WAIT_COUNT_INTERVAL;
  }

  @Override
  public boolean shouldRun() {
    if (isShutdown()) {
      return false;
    }
    synchronized (this.itemList) {
      return !this.itemList.isEmpty();
    }
  }

  @Override
  protected boolean isJobCanceled(final IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      if (!this.itemList.isEmpty()) {
        log.warn("Stopping Job with {} messages in the queue", this.itemList.size());
      } else {
        log.debug("Stopping Job. Queue empty.");
      }
      return true;
    }
    return false;
  }

  public void addEventMessage(final T item) {
    synchronized (this.itemList) {
      this.itemList.add(item);
    }
  }

  public List<T> getItemList() {
    return this.itemList;
  }

  protected int processQueue(final int maxWaitCount, final IProgressMonitor monitor, final Map<String, Object> mapQueueData) throws CoreException {
    int msgSendErrorWait = 1000;
    int maxErrorCount = 5;
    int waitCount = 0;
    int errorCount = 0;
    while ((!this.itemList.isEmpty() || waitCount < maxWaitCount) && errorCount < maxErrorCount) {
      if (isJobCanceled(monitor)) {
        return waitCount;
      }

      T item = null;
      synchronized (this.itemList) {
        if (!this.itemList.isEmpty()) {
          item = this.itemList.remove(0);
        }
      }

      if (item != null) {
        waitCount = 0;
        try {
          processItem(item, mapQueueData);
          errorCount = 0;
          msgSendErrorWait = 1000;
        } catch (Exception ex) {
          if (ex instanceof CoreException) {
            log.warn("Failed to send data to system: {}", ex.getMessage());
          } else {
            log.error("Failed to send data to system: {}", ex.getMessage(), ex);
          }
          // Put message back on queue
          this.itemList.add(0, item);
          if (isJobCanceled(monitor)) {
            return waitCount;
          }
          synchronized (this) {
            try {
              // Wait so that there is time between before trying again
              this.wait(msgSendErrorWait);
              msgSendErrorWait += 2000;
            } catch (InterruptedException iEx) {
              log.error("Eventbus job interrupted", iEx);
            }
            errorCount++;
          }
        }
      } else {
        // Wait to see if new messages will appear soon
        synchronized (this) {
          try {
            // Wait so that there is time before trying again
            this.wait(WAIT_COUNT_INTERVAL);
            waitCount++;
          } catch (InterruptedException iEx) {
            log.error("Eventbus job interrupted", iEx);
          }
        }
      }
    }

    if (errorCount >= maxErrorCount) {
      throw new CoreException("Issues sending data, going to reschedule job");
    }
    return waitCount;
  }

  /**
   * @throws CoreException
   */
  @Override
  public int setupAndProcessData(final IProgressMonitor monitor, final J connection, final Map<String, Object> mapQueueData, final int maxWaitCount) throws CoreException {
    return processQueue(maxWaitCount, monitor, mapQueueData);
  }

}