package media.alera.osgi.core.shared.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.JsonFactory;

@Component(immediate = true, enabled = true, service = { EventBusMessageProducer.class })
@GogoCommand(scope = "core", function = { "listEventProducers" })
public class EventBusMessageProducer extends AbstractMultiEclipseJobService<PostEventEclipseJob> implements IEventData {

  private CoreVariableService variableService = null;

  private PostEventEclipseJob sendJob;

  private JsonFactory factory;

  private Map<String, PostEventEclipseJob> mapDestToId = new HashMap<>();

  @Activate
  public void activate() {
    String destinationName = EventBusProperties.getEventBusTopic(this.variableService);
    this.sendJob = new PostEventEclipseJob(destinationName, this.variableService, this.factory, false);
  }

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setVariable(final CoreVariableService vs) {
    this.variableService = vs;
  }

  public void unsetVariable(final CoreVariableService vs) {
    this.variableService = null;
  }

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setFactory(final JsonFactory jsonFactory) {
    this.factory = jsonFactory;
  }

  public void unsetFactory(final JsonFactory jsonFactory) {
    this.factory = null;
  }

  @Override
  @Deactivate
  public void deactivate() {
    super.deactivate();

    this.sendJob = null;
    this.mapDestToId.clear();
  }

  @Descriptor("List all event producers that are currently running")
  public void listEventProducers(final CommandSession session) {
    List<PostEventEclipseJob> activeJobs = getActiveJobs();
    if (!activeJobs.isEmpty()) {
      for (PostEventEclipseJob job : activeJobs) {
        String type = "Topic";
        if (job.isQueue()) {
          type = "Queue";
        }
        session.getConsole().printf("Sending Data to %s (%s)%n", job.getDestinationName(), type);
      }
    } else {
      session.getConsole().printf("No event producers are running%n");
    }
  }

  @Override
  public List<PostEventEclipseJob> getAllJobs() {
    Collection<PostEventEclipseJob> jobs = this.mapDestToId.values();
    List<PostEventEclipseJob> jobList = new ArrayList<>();
    jobList.add(this.sendJob);
    jobList.addAll(jobs);
    return Collections.unmodifiableList(jobList);
  }

  public boolean sendEvent(final String topic, final IJsonData event) {
    return sendTaskOrEvent(topic, null, event, false);
  }

  public boolean sendEvent(final String topic, final IJsonData event, final String runtimeUuid) {
    return sendTaskOrEvent(topic, null, event, false, runtimeUuid);
  }

  public boolean sendTask(final String topic, final String destination, final IJsonData event) {
    return sendTaskOrEvent(topic, destination, event, true);
  }

  private boolean sendTaskOrEvent(final String topic, final String destination, final IJsonData event, final boolean isTask) {
    return sendTaskOrEvent(topic, destination, event, isTask, null);
  }

  private boolean sendTaskOrEvent(final String topic, final String destination, final IJsonData event, final boolean isTask, final String runtimeUuid) {
    String uuid = this.variableService.getRuntimeUUID();
    if (runtimeUuid != null) {
      uuid = runtimeUuid;
    }
    JmsTextMessage msg = new JmsTextMessage(event, topic, uuid);
    if (destination == null && !isTask) {
      this.sendJob.addEventMessage(msg);
      this.sendJob.schedule();
      return true;
    }

    synchronized (this.mapDestToId) {
      final PostEventEclipseJob postEventJob;
      if (this.mapDestToId.containsKey(destination)) {
        postEventJob = this.mapDestToId.get(destination);
      } else {
        final String destQueue;
        if (destination == null) {
          destQueue = EventBusProperties.getCoreDestinationQueue(this.variableService);
        } else {
          destQueue = destination;
        }
        postEventJob = new PostEventEclipseJob(destQueue, this.variableService, this.factory, isTask);
        this.mapDestToId.put(destination, postEventJob);
      }
      postEventJob.addEventMessage(msg);
      postEventJob.schedule();
    }

    return true;
  }
}