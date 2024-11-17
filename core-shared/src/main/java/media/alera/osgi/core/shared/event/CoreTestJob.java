package media.alera.osgi.core.shared.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonTypes;

@Builder(toBuilder = true)
@JsonTypeName(JsonTypes.TYPE_JOB_TEST)
public record CoreTestJob(
  @NonNull @JsonProperty(value = "ver", required = true) String ver,
  @NonNull @JsonProperty(value = "job-uid", required = true) String id,
  @NonNull @JsonProperty(value = "task-uid", required = true) String taskId,
  @NonNull @JsonProperty(value = "event-time", required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CoreUtils.INFO_ISO_DATE_FORMAT, timezone = "GMT") Instant eventTime)
implements IJsonEventData {

  public static final String CURRENT_VERSION = "1.0.0";

  public static class CoreTestJobBuilder {
    String ver = CURRENT_VERSION;
    String id = CoreUtils.generateUuid();
    String taskId = CoreUtils.generateUuid();
    Instant eventTime = Instant.now();
  }
}