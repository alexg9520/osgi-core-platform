package media.alera.osgi.core.shared.event;

public record JmsTextMessage(
    IJsonData msg,
    String topic,
    String sourceId) {
}