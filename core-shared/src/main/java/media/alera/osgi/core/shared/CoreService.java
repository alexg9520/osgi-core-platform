package media.alera.osgi.core.shared;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.shared.event.EventTopics;

@Slf4j
@Component(enabled = true, immediate = true, service = { EventHandler.class }, property = {
  EventConstants.EVENT_TOPIC + "=" + EventTopics.EVENT_ACTIVITY,
  EventConstants.EVENT_TOPIC + "=" + EventTopics.EVENT_TASK })
public class CoreService implements EventHandler {

    @Activate
    public void activate() {
        System.out.println("Activated Again");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("Dectivated");
    }

    @Override
    public void handleEvent(Event event) {
      log.info("Received event {}", event.getTopic());
    }
    
}