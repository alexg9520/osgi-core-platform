package media.alera.osgi.core.shared.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import media.alera.osgi.core.shared.JsonUtils;

@JsonPropertyOrder({ JsonUtils.TAG_OBJECT_TYPE, JsonUtils.FIELD_VER, JsonUtils.FIELD_EVENT_ID, JsonUtils.FIELD_JOB_ID, JsonUtils.FIELD_TASK_ID, JsonUtils.FIELD_EVENT_TIME })
public interface IJsonEventData extends IJsonData {

  public String taskId();

  public Instant eventTime();

}
