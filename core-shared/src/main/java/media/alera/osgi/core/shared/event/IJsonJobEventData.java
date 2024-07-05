package media.alera.osgi.core.shared.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import media.alera.osgi.core.shared.IJsonTypes;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = CoreActivityEvent.class, name = IJsonTypes.TYPE_EVENT_CORE_ACTIVITY),
})
public interface IJsonJobEventData extends IJsonData {

}