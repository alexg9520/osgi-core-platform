package media.alera.osgi.core.shared.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.CoreSystemStatus;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;

@Builder(toBuilder = true)
@JsonTypeName(JsonTypes.TYPE_EVENT_CORE_ACTIVITY)
public record CoreActivityEvent(
    @NonNull @JsonProperty(value = JsonUtils.FIELD_VER, required = true) String ver,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_EVENT_ID, required = true) String id,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_TASK_ID, required = true) String taskId,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_EVENT_TIME, required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CoreUtils.INFO_ISO_DATE_FORMAT, timezone = "GMT") Instant eventTime,
    @NonNull @JsonProperty(value = "status-message", required = true) String statusMsg,
    @NonNull @JsonProperty(value = "status", required = true) CoreSystemStatus status) 
  implements IJsonEventData {

  public static final String CURRENT_VERSION = "1.0.0";

  public static class CoreActivityEventBuilder {
    String ver = CURRENT_VERSION;
    String id = UUID.randomUUID().toString();
    String taskId = CoreUtils.generateActivityId(id);
    Instant eventTime = Instant.now();
  }    
}
