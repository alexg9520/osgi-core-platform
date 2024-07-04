package media.alera.osgi.core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.shared.eventbus.EventTopics;

@Slf4j
@Component(immediate = true, enabled = true, service = { EventTestService.class, EventHandler.class }, property = {
    EventConstants.EVENT_TOPIC + "=" + EventTopics.EVENTS,
    EventConstants.EVENT_TOPIC + "=" + EventTopics.TASK })
public class EventTestService implements EventHandler {

  List<Object> eventList = new ArrayList<>();

  @Override
  public void handleEvent(final Event event) {
    Object eventObj = event.getProperty(EventTopics.PARAM_EVENT_MSG_DATA);
    if (eventObj instanceof String) {
      log.info("EventBus message: " + (String) eventObj);
    }
    synchronized (this.eventList) {
      this.eventList.add(eventObj);
    }
  }

  public <T> List<T> getEventDataItems(final Class<T> clazz) throws InterruptedException {
    int retry = 0;
    List<T> itemList = null; // Collections.emptyList();
    while (itemList == null && retry < 100) {
      synchronized (this.eventList) {
        itemList = this.eventList.stream().filter(n -> (clazz.isInstance(n))).map(n -> clazz.cast(n)).collect(Collectors.toList());
        if (itemList.size() == 0) {
          itemList = null;
        }
      }

      if (itemList == null) {
        Thread.sleep(100);
      }
      retry++;
    }

    if (itemList == null) {
      return Collections.emptyList();
    }
    return itemList;
  }

  public <T> Optional<T> getEventData(final Class<T> clazz) throws InterruptedException {
    List<T> eventDataItems = getEventDataItems(clazz);
    if (!eventDataItems.isEmpty()) {
      return Optional.of(eventDataItems.get(0));
    }
    return Optional.empty();
  }

  public void clear() {
    synchronized (this.eventList) {
      this.eventList.clear();
    }
  }
}
