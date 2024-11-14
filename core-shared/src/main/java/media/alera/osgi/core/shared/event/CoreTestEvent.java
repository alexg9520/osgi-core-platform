package media.alera.osgi.core.shared.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;

@Builder(toBuilder = true)
@JsonTypeName(JsonTypes.TYPE_EVENT_TEST)
public record CoreTestEvent(
    @NonNull @JsonProperty(value = JsonUtils.FIELD_VER, required = true) String ver,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_EVENT_ID, required = true) String id,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_TASK_ID, required = true) String taskId,
    @NonNull @JsonProperty(value = JsonUtils.FIELD_EVENT_TIME) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CoreUtils.INFO_ISO_DATE_FORMAT, timezone = "GMT") Instant eventTime,
    @NonNull @JsonProperty(required = true) String msg)
  implements IJsonEventData {

  public static final String CURRENT_VERSION = "1.0.0";

  // public CoreTestEvent(String ver, String id, String taskId, String systemId, Instant eventTime, String msg) {
  //   this.ver = ver;
  //   if (id == null) {
  //     this.id = UUID.randomUUID().toString();
  //   } else {
  //     this.id = id;
  //   }
  //   if (taskId == null) {
  //     this.taskId = CoreUtils.generateActivityId(this.id);
  //   } else {
  //     this.taskId = taskId;
  //   }
  //   this.systemId = systemId;
  //   this.eventTime = eventTime;
  //   this.msg = msg;
  // }

  public static class CoreTestEventBuilder {
    String ver = CURRENT_VERSION;
    String id = UUID.randomUUID().toString();
    String taskId = CoreUtils.generateActivityId(id);
    Instant eventTime = Instant.now();
  }
}