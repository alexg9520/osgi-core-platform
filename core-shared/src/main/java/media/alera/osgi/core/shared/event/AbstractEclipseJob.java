package media.alera.osgi.core.shared.event;

import java.time.Instant;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.CoreProperties;

@Slf4j
public abstract class AbstractEclipseJob<J extends AutoCloseable> extends Job {

  private int errorWaitTime = 2000;

  public static final String PARAM_SERVICE_MAX_WAIT_COUNT = "maxWaitCount";

  private static final int MAX_ERROR_WAIT_TIME = 120000;

  private static final int INC_ERROR_WAIT_TIME = 4000;

  private static final int MAX_ERROR_RETRY_COUNT = 1000;

  private boolean shutdown = false;

  private int maxErrorWaitTime;

  private int incErrorWaitTime;

  private int maxErrorRetryCount;

  private int retryCount = 0;

  public abstract J createConnection(Map<String, Object> jobData) throws CoreException;

  public abstract void jobComplete(int waitCount, Map<String, Object> jobData, boolean hasError, boolean rescheduleForError);

  public abstract int setupAndProcessData(final IProgressMonitor monitor, final J closeable, final Map<String, Object> jobData, final int maxWaitCount) throws CoreException;

  protected AbstractEclipseJob(final String name) {
    this(name, -1, -1, -1);
  }

  protected AbstractEclipseJob(final String name, final int maxErrorRetryCount, final int maxErrorWaitTime, final int incErrorWaitTime) {
    super(name);
    this.maxErrorWaitTime = maxErrorWaitTime;
    this.incErrorWaitTime = incErrorWaitTime;
    this.maxErrorRetryCount = maxErrorRetryCount;
  }

  public void shutdown() {
    this.shutdown = true;
    this.cancel();
    synchronized (this) {
      this.notifyAll();
    }
  }

  public boolean isShutdown() {
    return this.shutdown;
  }

  @Override
  public boolean shouldRun() {
    return !this.shutdown;
  }

  protected boolean isJobCanceled(final IProgressMonitor monitor) {
    if (monitor.isCanceled() || this.shutdown) {
      log.debug("Stopping Receive Events Job");
      return true;
    }
    return false;
  }

  public int getErrorWaitTime() {
    return this.errorWaitTime;
  }

  public void resetErrorWaitTime() {
    this.errorWaitTime = 2000;
    this.retryCount = 0;
  }

  private int getMaxErrorWaitTime() {
    if (this.maxErrorWaitTime > 0) {
      return this.maxErrorWaitTime;
    }
    return MAX_ERROR_WAIT_TIME;
  }

  private int getIncErrorWaitTime() {
    if (this.incErrorWaitTime > 0) {
      return this.incErrorWaitTime;
    }
    return INC_ERROR_WAIT_TIME;
  }

  private int getMaxErrorRetryCount() {
    if (this.maxErrorRetryCount > 0) {
      return this.maxErrorRetryCount;
    }
    return MAX_ERROR_RETRY_COUNT;
  }

  private void increaseErrorWaitTime(final int updateTime) {
    this.errorWaitTime += updateTime;
    if (this.errorWaitTime > getMaxErrorWaitTime()) {
      this.errorWaitTime = getMaxErrorWaitTime();
    }
  }

  public static String getDestination(final CoreVariableService variableService) {
    return CoreProperties.getEventBusTopic(variableService);
  }

  public boolean handleConnectionError(final IProgressMonitor monitor) throws InterruptedException {
    synchronized (this) {
      if (this.retryCount > getMaxErrorRetryCount()) {
        log.error("Failed to connect to service after {} retries. Exiting.", this.retryCount);
        return false;
      }

      // Wait so that there is time between before this job can be rescheduled.
      // And increase time if it keeps failing
      long startTime = Instant.now().toEpochMilli();
      // Wait for the error wait time or until the job is canceled
      while (((startTime + getErrorWaitTime()) > Instant.now().toEpochMilli()) && !monitor.isCanceled()) {
        long waitTime = (startTime + getErrorWaitTime()) - Instant.now().toEpochMilli();
        this.wait(waitTime);
      }

      if (!monitor.isCanceled()) {
        // Increase the error wait time if the job is not canceled
        increaseErrorWaitTime(getIncErrorWaitTime());
      }

      this.retryCount++;
    }
    return true;
  }

  public IStatus executeJob(final IProgressMonitor monitor, final Map<String, Object> mapQueueData) {
    int waitCount = 0;
    int maxWaitCount = (int) mapQueueData.get(PARAM_SERVICE_MAX_WAIT_COUNT);
    boolean rescheduleForError = true;
    boolean hasError = false;
    try (J connection = createConnection(mapQueueData)) {
      mapQueueData.put("connection", connection);
      waitCount = setupAndProcessData(monitor, connection, mapQueueData, maxWaitCount);
    } catch (CoreException ex) {
      log.warn("Error connecting to: {}. Waiting at least {} seconds before trying again.", ex.getMessage(), (getErrorWaitTime() / 1000));
      try {
        hasError = true;
        rescheduleForError = handleConnectionError(monitor);
      } catch (InterruptedException iEx) {
        log.error("Process queue job interrupted", iEx);
        Thread.currentThread().interrupt();
      }
    } catch (Exception ex) {
      log.error("Failed to create connection", ex);
      // Exiting due to error
      return Status.OK_STATUS;
    }

    // Only call job complete if the service hasn't been shutdown
    if (!isShutdown()) {
      jobComplete(waitCount, mapQueueData, hasError, rescheduleForError);
    }
    return Status.OK_STATUS;
  }
}