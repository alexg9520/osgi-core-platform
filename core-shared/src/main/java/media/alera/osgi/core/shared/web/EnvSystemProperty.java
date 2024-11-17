package media.alera.osgi.core.shared.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;
import media.alera.osgi.core.shared.event.IJsonData;

@Builder(toBuilder = true)
@JsonTypeName(JsonTypes.TYPE_SYSTEM_ENV_PROPERTY)
public record EnvSystemProperty (
    @NonNull @JsonProperty(value = JsonUtils.FIELD_VER, required = true) String ver,
    @JsonProperty(value = JsonUtils.FIELD_EVENT_ID, required = false) String id,
    @NonNull @JsonProperty(value = "name", required = true) String name,
    @JsonProperty(value = "value", required = false) String value) 
  implements IJsonData {

  public static final String CURRENT_VERSION = "1.0.0";

  public static class EnvSystemPropertyBuilder {
    String ver = CURRENT_VERSION;
    // String id = UUID.randomUUID().toString();
  }    
  
}
