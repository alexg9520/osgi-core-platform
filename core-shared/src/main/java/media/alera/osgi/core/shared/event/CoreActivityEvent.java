package media.alera.osgi.core.shared.event;

import java.time.Instant;

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
@JsonTypeName(IJsonTypes.TYPE_EVENT_CORE_ACTIVITY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonPropertyOrder({ "ver", "event-uid", "task-uid", "system-uid", "event-time"})
public record CoreActivityEvent(
  @NonNull String ver,
  @NonNull @JsonProperty("event-uid") String eventId,
  @NonNull @JsonProperty("task-uid") String taskId,
  @NonNull @JsonProperty("system-uid") String systemId,
  @NonNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CoreUtils.INFO_ISO_DATE_FORMAT, timezone = "GMT") @JsonProperty("event-time") Instant eventTime) {

}
