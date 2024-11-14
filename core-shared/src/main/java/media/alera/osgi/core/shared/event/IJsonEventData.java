package media.alera.osgi.core.shared.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;

@JsonPropertyOrder({ JsonUtils.TAG_OBJECT_TYPE, JsonUtils.FIELD_VER, JsonUtils.FIELD_EVENT_ID, JsonUtils.FIELD_JOB_ID, JsonUtils.FIELD_TASK_ID, JsonUtils.FIELD_EVENT_TIME })
@JsonSubTypes({
  @JsonSubTypes.Type(value = CoreActivityEvent.class, name = JsonTypes.TYPE_EVENT_CORE_ACTIVITY),
  @JsonSubTypes.Type(value = CoreTestEvent.class, name = JsonTypes.TYPE_EVENT_TEST),
  @JsonSubTypes.Type(value = CoreTestJob.class, name = JsonTypes.TYPE_JOB_TEST)
})
public interface IJsonEventData extends IJsonData {

  public String taskId();

  public Instant eventTime();

}
