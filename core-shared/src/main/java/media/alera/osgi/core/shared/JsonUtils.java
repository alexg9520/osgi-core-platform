package media.alera.osgi.core.shared;

import org.apache.commons.lang3.ArrayUtils;

public class JsonUtils {
  
  public static final String TAG_OBJECT_TYPE = "obj-type";

  public static final String FIELD_VER = "ver";

  public static final String FIELD_EVENT_ID = "event-uid";

  public static final String FIELD_JOB_ID = "job-uid";

  public static final String FIELD_TASK_ID = "task-uid";

  public static final String FIELD_EVENT_TIME = "event-time";

  public static final String[] FIELD_ORDER_ARRAY = { JsonUtils.TAG_OBJECT_TYPE, JsonUtils.FIELD_VER,JsonUtils.FIELD_EVENT_ID, JsonUtils.FIELD_TASK_ID, JsonUtils.FIELD_EVENT_TIME };

  private JsonUtils() {
    // static only
  }

  public static final String[] getFieldOrderList(String[] addedFields) {
    return ArrayUtils.addAll(FIELD_ORDER_ARRAY, addedFields);
  }
}
