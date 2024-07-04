package media.alera.osgi.core.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class CoreOSGiIntegrationTest {
  
  private static EventTestService eventTestService;

  @BeforeAll
  public static void setUp() {
    Bundle bundle = FrameworkUtil.getBundle(CoreOSGiIntegrationTest.class);
    assertNotNull(bundle, "OSGi Bundle tests must be run inside an OSGi framework");

    eventTestService = getService(bundle.getBundleContext(), EventTestService.class);
    assertNotNull(eventTestService, "Event Test Service must be available");
  }

  @Test
  public void testEventBus() throws InterruptedException {
    // eventTestService.getEventDataItems(String.class);
    System.out.println("TEST RUNNING");
  }

  private static <T> T getService(final BundleContext bc, final Class<T> clazz) {
    ServiceReference<T> serviceReference = bc.getServiceReference(clazz);
    if (serviceReference == null) {
      return null;
    }
    return bc.getService(serviceReference);
  }
}
