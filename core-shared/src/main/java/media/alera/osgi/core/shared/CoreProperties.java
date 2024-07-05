package media.alera.osgi.core.shared;

import java.util.TimeZone;

import media.alera.osgi.core.init.CoreVariableService;

public class CoreProperties {

  public static final String CORE_CONSOLE_TIMEZONE = "CORE_CONSOLE_TIMEZONE";

  public static final String CORE_CONSOLE_DATE_FORMAT = "CORE_CONSOLE_DATE_FORMAT";

  private CoreProperties() {
    // NO-OP
  }

  public static String getEventBusTopic(final CoreVariableService variableService) {
    return variableService.getEnvSystemProperty(CORE_CONSOLE_TIMEZONE, TimeZone.getDefault().getID());
  }

  public static String getConsoleTimeZone(final CoreVariableService variableService) {
    return variableService.getEnvSystemProperty(CORE_CONSOLE_TIMEZONE, TimeZone.getDefault().getID());
  }

  public static String getConsoleDateFormat(final CoreVariableService variableService) {
    return variableService.getEnvSystemProperty(CORE_CONSOLE_DATE_FORMAT, CoreUtils.DEFAULT_DISPLAY_DATE_FORMAT);
  }
}