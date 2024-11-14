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
import org.osgi.service.event.EventAdmin;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.JsonFactory;

/**
 * Reads Events from an ActiveMQ message broker
 */
@Slf4j
@Component(immediate = true, enabled = true, service = { EventBusMessageConsumer.class })
@GogoCommand(scope = "core", function = { "listEventConsumers" })
public class EventBusMessageConsumer extends AbstractMultiEclipseJobService<ReceiveEventEclipseJob> implements IEventData {

  private EventAdmin localEventAdmin;

  private CoreVariableService variableService;

  private JsonFactory factory;

  private ReceiveEventEclipseJob receiveJob;

  private String sessionUUID;

  private Map<String, ReceiveEventEclipseJob> mapDestToId = new HashMap<>();

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setLocalEventAdmin(final EventAdmin eventAdmin) {
    this.localEventAdmin = eventAdmin;
    log.debug("Local event bus set in EventBusMessageConsumer");
  }

  public void unsetLocalEventAdmin(final EventAdmin eventAdmin) {
    this.localEventAdmin = null;
    log.debug("Local event bus unset in EventBusMessageConsumer");
  }

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setFactory(final JsonFactory jsonFactory) {
    this.factory = jsonFactory;
  }

  public void unsetFactory(final JsonFactory jsonFactory) {
    this.factory = null;
  }

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setVariable(final CoreVariableService vs) {
    this.variableService = vs;
  }

  public void unsetVariable(final CoreVariableService vs) {
    this.variableService = null;
  }

  @Activate
  public void activate() {
    this.sessionUUID = this.variableService.getRuntimeUUID();

    String destinationName = EventBusProperties.getEventBusTopic(this.variableService);
    this.receiveJob = new ReceiveEventEclipseJob(destinationName, this.variableService, this.factory, false, this.localEventAdmin, this.sessionUUID);
    this.receiveJob.schedule();
  }

  @Override
  @Deactivate
  public void deactivate() {
    super.deactivate();

    this.receiveJob = null;
    this.mapDestToId.clear();
  }

  @Descriptor("List all event consumers that are currently running")
  public void listEventConsumers(final CommandSession session) {
    List<ReceiveEventEclipseJob> activeJobs = getActiveJobs();
    if (activeJobs.size() > 0) {
      for (ReceiveEventEclipseJob job : activeJobs) {
        String type = "Topic";
        if (job.isQueue()) {
          type = "Queue";
        }
        session.getConsole().printf("Receiving Data from %s (%s)\n", job.getDestinationName(), type);
      }
    } else {
      session.getConsole().printf("No event producers are running\n");
    }
  }

  @Override
  public List<ReceiveEventEclipseJob> getAllJobs() {
    Collection<ReceiveEventEclipseJob> jobs = this.mapDestToId.values();
    List<ReceiveEventEclipseJob> jobList = new ArrayList<>();
    jobList.add(this.receiveJob);
    jobList.addAll(jobs);
    return Collections.unmodifiableList(jobList);
  }

  public void addEventsListener(final String topic, final String destination) {
    addEventsOrTaskListener(topic, destination, false);
  }

  public void addTasksListener(final String topic, final String destination) {
    addEventsOrTaskListener(topic, destination, true);
  }

  public void addEventsOrTaskListener(final String topic, final String destination, final boolean isTask) {
    synchronized (this.mapDestToId) {
      final ReceiveEventEclipseJob recEventJob;
      if (this.mapDestToId.containsKey(destination)) {
        recEventJob = this.mapDestToId.get(destination);
      } else {
        recEventJob = new ReceiveEventEclipseJob(destination, this.variableService, this.factory, isTask, this.localEventAdmin, this.sessionUUID);
        this.mapDestToId.put(destination, recEventJob);
      }
      recEventJob.schedule();
    }
  }


}