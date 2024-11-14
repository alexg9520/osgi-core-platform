package media.alera.osgi.core.shared.event;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractMultiEclipseJobService<T extends AbstractEclipseJob<?>> {

  public AbstractMultiEclipseJobService() {
    super();
  }

  public abstract List<T> getAllJobs();

  public void deactivate() {
    List<T> jobList = getAllJobs();
    stopJobs(jobList);
    waitForJobs(jobList);
  }

  protected void stopJobs(final List<T> jobs) {
    for (T job : jobs) {
      job.shutdown();
    }
  }

  protected void waitForJobs(final List<T> jobs) {
    for (T job : jobs) {
      try {
        job.join(5000, new NullProgressMonitor());
      } catch (InterruptedException e) {
        // no op
      }
    }
  }

  public List<T> getActiveJobs() {
    return getAllJobs().stream().filter(n -> (n.getState() == Job.RUNNING || n.getState() == Job.WAITING)).collect(Collectors.toList());
  }

}