package media.alera.osgi.core.shared.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.IJsonTypes;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@JsonTypeName(IJsonTypes.TYPE_JOB_TEST)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonPropertyOrder({ "ver", "source-uid", "instance-uid", "activity-uid", "owner", "owner-uid", "email", "options" })
public record CoreTestJob(
  @NonNull String ver,
  @NonNull @JsonProperty("job-uid") String jobId,
  @JsonProperty("task-uid") String taskId,
  @NonNull @JsonProperty("system-uid") String systemId,
  @NonNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CoreUtils.INFO_ISO_DATE_FORMAT, timezone = "GMT") @JsonProperty("event-time") Instant eventTime)
implements IJsonJobEventData {

  public static final String CURRENT_VERSION = "1.0.0";

  public CoreTestJob(final String jobId, final String taskId, final String systemId) {
    this(CURRENT_VERSION, jobId, taskId, systemId, Instant.now());
  }

  public CoreTestJob(final String systemId) {
    this(CURRENT_VERSION, UUID.randomUUID().toString(), null, systemId, Instant.now());
  }

  public CoreTestJob {
    if (taskId == null) {
      taskId = CoreUtils.generateActivityId(jobId);
    }
  }

  public static class CoreTestJobBuilder {
    String ver = CURRENT_VERSION;
    String jobId = UUID.randomUUID().toString();
  }
}