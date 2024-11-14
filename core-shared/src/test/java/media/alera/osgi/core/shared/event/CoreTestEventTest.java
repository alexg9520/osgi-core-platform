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
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;
import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.event.CoreTestEvent.CoreTestEventBuilder;
import media.alera.osgi.core.shared.test.TestUtils;

class CoreTestEventTest {

  private static final String EVENT_ID = UUID.randomUUID().toString();

  private static final String TASK_ID = CoreUtils.generateActivityId(UUID.randomUUID().toString());

  private static final String MESSAGE = "Test Message";

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
        "msg" : "%s"
      }
      """
      .formatted(
          JsonUtils.TAG_OBJECT_TYPE, JsonTypes.TYPE_EVENT_TEST,
          JsonUtils.FIELD_VER, VERSION,
          JsonUtils.FIELD_EVENT_ID, EVENT_ID,
          JsonUtils.FIELD_TASK_ID, TASK_ID,
          JsonUtils.FIELD_EVENT_TIME, EVENT_TIME_STRING,
          MESSAGE);

  private CoreTestEvent coreTestEventExpected;

  @BeforeEach
  public void setup() {
    this.coreTestEventExpected = new CoreTestEvent(VERSION, EVENT_ID, TASK_ID, EVENT_TIME, MESSAGE);
  }

  @Test
  void create() throws CoreException {
    CoreTestEvent event = createEvent();
    assertEquals(this.coreTestEventExpected, event);
    System.out.print(TestUtils.getJSONforClass(JSON_OBJECT_FACTORY, event));
    System.out.print(TestUtils.getJSONSchemaforClass(event.getClass()));
  }

  @Test
  void jsonToObjects() throws CoreException {
    CoreTestEvent event = (CoreTestEvent) JSON_OBJECT_FACTORY.fromString(JSON_CORE_ACTIVITY_EVENT, IJsonEventData.class);
    assertEquals(this.coreTestEventExpected, event);
  }

  @Test
  void objectToJson() throws CoreException, JsonMappingException, JsonProcessingException {
    CoreTestEvent event = createEvent();
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
    CoreTestEvent event1 = new CoreTestEvent(VERSION, CoreUtils.generateUuid(), CoreUtils.generateUuid(), EVENT_TIME, MESSAGE);
    CoreTestEvent event2 = new CoreTestEvent(VERSION, CoreUtils.generateUuid(), CoreUtils.generateUuid(), EVENT_TIME, MESSAGE);
    assertNotEquals(event1, event2);
    assertEquals(event1.msg(), event2.msg());
    assertEquals(event1.eventTime(), event2.eventTime());
    assertEquals(event1.ver(), event2.ver());
  }

  @Test
  void createWithBuilderToString() {
    CoreTestEvent event = createEvent();
    String builderString = event.toBuilder().toString();
    String builderStringExpected = this.coreTestEventExpected.toBuilder().toString();
    assertEquals(builderStringExpected, builderString);
  }

  @Test
  void testFullBuilder() {
    CoreTestEventBuilder builder = CoreTestEvent.builder();
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
        
    builder.msg(MESSAGE);
    builder.build();
  }

  @Test
  void testShortBuilder() {
    CoreTestEventBuilder builder = CoreTestEvent.builder();
    assertThrows(NullPointerException.class,
        () -> {
          builder.build();
        });
    builder.msg(MESSAGE);
    builder.build();
  }  

  private CoreTestEvent createEvent() {
    CoreTestEvent event = CoreTestEvent.builder()
        .ver(CoreTestEvent.CURRENT_VERSION)
        .id(EVENT_ID)
        .taskId(TASK_ID)
        .msg(MESSAGE)
        .eventTime(EVENT_TIME)
        .build();

    return event;
  }

}