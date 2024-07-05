package media.alera.osgi.core.shared;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import media.alera.osgi.core.init.CoreVariableService;

@Slf4j
public class CoreUtils {

  public static final String DEFAULT_DISPLAY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  public static final String INFO_ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  public static final String CURRENT_TIME_VARIABLE_NAME = "currentTime";

  public static final String DIVIDER = "--------------------------------------------------";

  private static DateTimeFormatter customFormatter;

  static {
    final DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();
    customFormatter = dtfb.appendOptional(DateTimeFormatter.ofPattern(DEFAULT_DISPLAY_DATE_FORMAT))
        .appendOptional(DateTimeFormatter.ofPattern(INFO_ISO_DATE_FORMAT))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
  }

  private CoreUtils() {
    // static only
  }

  public static String getDateAsString(final Instant date) {
    return getDateAsString(date, DEFAULT_DISPLAY_DATE_FORMAT);
  }

  public static String getDateAsString(final Instant date, final String dateFormat) {
    return getDateAsString(date, DateTimeFormatter.ofPattern(dateFormat), TimeZone.getDefault());
  }

  public static String getDateAsString(final Instant date, TimeZone timeZone) {
    return getDateAsString(date, DEFAULT_DISPLAY_DATE_FORMAT, timeZone);
  }
  
  public static String getDateAsString(final Instant date, final String dateFormat, TimeZone timeZone) {
    return getDateAsString(date, DateTimeFormatter.ofPattern(dateFormat), timeZone);
  }

  public static String getDateAsString(final Instant date, final DateTimeFormatter dateFormat, TimeZone timeZone) {
    return dateFormat.withZone(timeZone.toZoneId()).format(date);
  }

  public static String getDateAsString(final Instant date, final DateTimeFormatter dateFormat) {
    return dateFormat.withZone(TimeZone.getDefault().toZoneId()).format(date);
  }

  public static String getDateAsStringInUTC(final Instant date) {
    return DateTimeFormatter.ofPattern(DEFAULT_DISPLAY_DATE_FORMAT).withZone(ZoneOffset.UTC).format(date);
  }

  public static String getDateAsZuluString(final Instant date) {
    return DateTimeFormatter.ofPattern(INFO_ISO_DATE_FORMAT).withZone(ZoneOffset.UTC).format(date);
  }

  public static Instant parseStringToDate(final String date) {
    return OffsetDateTime.parse(date, customFormatter).toInstant().truncatedTo(ChronoUnit.MILLIS);
  }

  public static Instant getCurrentInstant() {
    return Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

  public static String generateActivityId(String instanceId) {
    return UUID.nameUUIDFromBytes(instanceId.getBytes()).toString();
  }

  public static String getDateStringForConsole(final Instant date, CoreVariableService variableService) {
    String dateFormat = CoreProperties.getConsoleDateFormat(variableService);
    String timeZone = CoreProperties.getConsoleTimeZone(variableService);
    TimeZone tz = TimeZone.getDefault();
    DateTimeFormatter datePattern = null;

    try {
      tz = TimeZone.getTimeZone(timeZone);
    } catch (Exception e) {
      log.warn("Failed to get timezone for '{}', using default", timeZone);
    }

    try {
      datePattern = DateTimeFormatter.ofPattern(dateFormat);
    } catch (Exception e) {
      log.warn("Failed to get date format for '{}', using default", dateFormat);
    }

    if (datePattern != null) {
      return getDateAsString(date, datePattern, tz);
    }
    return getDateAsString(date, tz);
  }

  public static String getSchemaForConsole(final String schema, Class<?> clz) {
    return String.format("%s%n%s Class Schema%n%s%n%s%n", CoreUtils.DIVIDER, clz.getSimpleName(), schema, CoreUtils.DIVIDER);
  }

  // Trim name to the specified amount by taking the left and right sides and putting a ... in the middle
  public static String trimDisplayName(String name, int maxLength) {
    if (name.length() > maxLength) {
      int leftLength = (maxLength/2) - 1;
      String left = StringUtils.left(name, leftLength).trim();
      int rightLength = maxLength - left.length() - 3;
      String right = StringUtils.right(name, rightLength).trim();
      // return left + new String(Character.toChars(0x2026)) + right;
      return left + "..." + right;
    }
    return name;
  }
}
