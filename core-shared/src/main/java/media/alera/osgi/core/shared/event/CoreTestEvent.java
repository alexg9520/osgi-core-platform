package media.alera.osgi.core.shared.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.NonNull;
import media.alera.osgi.core.shared.IJsonTypes;

@Builder(builderMethodName = "internalBuilder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@JsonTypeName(IJsonTypes.TYPE_EVENT_TEST)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonPropertyOrder({ "msg-uid", "msg" })
public record CoreTestEvent(
    @JsonProperty("msg-uid") @NonNull String id,
    @NonNull String msg)
implements IJsonJobEventData {

  public static CoreTestEventBuilder builder(final String msg) {
    CoreTestEventBuilder internalBuilder = internalBuilder();
    return internalBuilder.id(UUID.randomUUID().toString()).msg(msg);
  }
}