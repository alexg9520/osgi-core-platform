package media.alera.osgi.core.shared.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.CoreSystemStatus;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;
import media.alera.osgi.core.shared.event.CoreActivityEvent.CoreActivityEventBuilder;
import media.alera.osgi.core.shared.test.TestUtils;

class CoreActivityEventTest {

  private static final String EVENT_ID = UUID.randomUUID().toString();

  private static final String TASK_ID = CoreUtils.generateActivityId(UUID.randomUUID().toString());

  private static final String STATUS_MESSAGE = "System Started";

  private static final CoreSystemStatus STATUS = CoreSystemStatus.STARTED;

  private static final String VERSION = "1.0.0";

  private static final Instant EVENT_TIME = CoreUtils.getCurrentInstant();

  private static final String EVENT_TIME_STRING = CoreUtils.getDateAsZuluString(EVENT_TIME);

  private static final JsonFactory JSON_OBJECT_FACTORY = new JsonFactory();

  private static final String JSON_CORE_ACTIVITY_EVENT = """
      {
        "%s": "%s",
        "%s" : "%s",
        "%s" : "%s",
        "%s" : "%s",
        "%s" : "%s",
        "status-message" : "%s",
        "status" : "%s"
      }
      """
      .formatted(
          JsonUtils.TAG_OBJECT_TYPE, JsonTypes.TYPE_EVENT_CORE_ACTIVITY,
          JsonUtils.FIELD_VER, VERSION,
          JsonUtils.FIELD_EVENT_ID, EVENT_ID,
          JsonUtils.FIELD_TASK_ID, TASK_ID,
          JsonUtils.FIELD_EVENT_TIME, EVENT_TIME_STRING,
          STATUS_MESSAGE,
          STATUS);

  private CoreActivityEvent coreTestEventExpected;

  @BeforeEach
  public void setup() {
    this.coreTestEventExpected = new CoreActivityEvent(VERSION, EVENT_ID, TASK_ID, EVENT_TIME, STATUS_MESSAGE, STATUS);
  }

  @Test
  void create() throws CoreException {
    CoreActivityEvent event = createEvent();
    assertEquals(this.coreTestEventExpected, event);
    System.out.print(TestUtils.getJSONforClass(JSON_OBJECT_FACTORY, event));
    System.out.print(TestUtils.getJSONSchemaforClass(event.getClass()));
  }

  @Test
  void jsonToObjects() throws CoreException {
    CoreActivityEvent event = JSON_OBJECT_FACTORY.fromString(JSON_CORE_ACTIVITY_EVENT, CoreActivityEvent.class);
    assertEquals(this.coreTestEventExpected, event);
  }

  @Test
  void objectToJson() throws CoreException, JsonMappingException, JsonProcessingException {
    CoreActivityEvent event = createEvent();
    String json = JSON_OBJECT_FACTORY.toString(event);
    ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    JsonNode jsonTree = objectMapper.readTree(json);
    JsonNode jsonTreeExpected = objectMapper.readTree(JSON_CORE_ACTIVITY_EVENT);
    String formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
    String formattedJsonExpected = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTreeExpected);
    assertEquals(formattedJsonExpected, formattedJson);
  }

  @Test
  void createWithSimpleConstructor() {
    CoreActivityEvent event1 = new CoreActivityEvent(VERSION, EVENT_ID, TASK_ID, EVENT_TIME, STATUS_MESSAGE, STATUS);
    CoreActivityEvent event2 = new CoreActivityEvent(VERSION, CoreUtils.generateUuid(), CoreUtils.generateUuid(), EVENT_TIME, STATUS_MESSAGE, STATUS);
    assertNotEquals(event1, event2);
    assertEquals(event1.statusMsg(), event2.statusMsg());
    assertEquals(event1.status(), event2.status());
    assertEquals(event1.eventTime(), event2.eventTime());
    assertEquals(event1.ver(), event2.ver());
  }

  @Test
  void createWithBuilderToString() {
    CoreActivityEvent event = createEvent();
    String builderString = event.toBuilder().toString();
    String builderStringExpected = this.coreTestEventExpected.toBuilder().toString();
    assertEquals(builderStringExpected, builderString);
  }

  @Test
  void testFullBuilder() {
    CoreActivityEventBuilder builder = CoreActivityEvent.builder();
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });

    builder.ver(VERSION);
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });

    builder.id(EVENT_ID);
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });

    builder.taskId(TASK_ID);
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });

    builder.eventTime(EVENT_TIME);
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });            
        
    builder.statusMsg(STATUS_MESSAGE);
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });

    builder.status(STATUS);
    builder.build();
  }

  @Test
  void testShortBuilder() {
    CoreActivityEventBuilder builder = CoreActivityEvent.builder();
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });
    builder.statusMsg(STATUS_MESSAGE).status(STATUS).build();
  }  

  private CoreActivityEvent createEvent() {
    CoreActivityEvent event = CoreActivityEvent.builder()
        .ver(CoreActivityEvent.CURRENT_VERSION)
        .id(EVENT_ID)
        .taskId(TASK_ID)
        .statusMsg(STATUS_MESSAGE)
        .status(STATUS)
        .eventTime(EVENT_TIME)
        .build();

    return event;
  }

}