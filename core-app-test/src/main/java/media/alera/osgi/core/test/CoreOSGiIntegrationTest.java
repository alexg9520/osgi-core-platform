package media.alera.osgi.core.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.shared.CoreSystemStatus;
import media.alera.osgi.core.shared.event.CoreActivityEvent;
import media.alera.osgi.core.shared.event.EventBus;
import media.alera.osgi.core.shared.event.EventTopics;

@Slf4j
public class CoreOSGiIntegrationTest {
  
  private static EventTestService eventTestService;

  private static EventBus eventBus;

  @BeforeAll
  public static void setUp() {
    Bundle bundle = FrameworkUtil.getBundle(CoreOSGiIntegrationTest.class);
    assertNotNull(bundle, "OSGi Bundle tests must be run inside an OSGi framework");

    eventTestService = getService(bundle.getBundleContext(), EventTestService.class);
    assertNotNull(eventTestService, "Event Test Service must be available");

    eventBus = getService(bundle.getBundleContext(), EventBus.class);
    assertNotNull(eventBus, "Event Bus must be available");
  }

  @Test
  public void testEventBus() throws InterruptedException {
    log.info("Event Bus Testing Started");
    CoreActivityEvent event = CoreActivityEvent.builder().status(CoreSystemStatus.STARTED).statusMsg("Event Bus Started").build();
    eventBus.postEvent(EventTopics.EVENT_ACTIVITY + "/test-service", event);
    Thread.sleep(1000);
    List<CoreActivityEvent> eventDataItems = eventTestService.getEventDataItems(CoreActivityEvent.class);
    assertTrue(!eventDataItems.isEmpty());
    boolean found = false;
    for (CoreActivityEvent coreActivityEvent : eventDataItems) {
      if (coreActivityEvent.status() == CoreSystemStatus.STARTED) {
        found = true;
      }
    }
    assertTrue(found);
    log.info("Event Bus Test Complete");
  }

  private static <T> T getService(final BundleContext bc, final Class<T> clazz) {
    ServiceReference<T> serviceReference = bc.getServiceReference(clazz);
    if (serviceReference == null) {
      return null;
    }
    return bc.getService(serviceReference);
  }
}
