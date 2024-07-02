package media.alera.osgi.core.shared;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@Component(enabled = true, immediate = true)
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
        throw new UnsupportedOperationException("Unimplemented method 'handleEvent'");
    }
    
}