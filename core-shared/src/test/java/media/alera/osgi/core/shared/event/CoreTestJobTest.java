package media.alera.osgi.core.shared.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.JsonTypes;
import media.alera.osgi.core.shared.JsonUtils;
import media.alera.osgi.core.shared.event.CoreTestJob.CoreTestJobBuilder;
import media.alera.osgi.core.shared.test.TestUtils;

class CoreTestJobTest {

  private static final String JOB_ID = UUID.randomUUID().toString();

  private static final String TASK_ID = CoreUtils.generateActivityId(UUID.randomUUID().toString());

  private static final String VERSION = "1.0.0";

  private static final Instant EVENT_TIME = CoreUtils.getCurrentInstant();

  private static final String EVENT_TIME_STRING = CoreUtils.getDateAsZuluString(EVENT_TIME);

  private static final JsonFactory JSON_OBJECT_FACTORY = new JsonFactory();

  private static final String JSON_CORE_TEST_JOB = """
      {
        "%s" : "%s",
        "%s" : "%s",
        "%s" : "%s",
        "%s" : "%s",
        "%s" : "%s"
      }
      """
      .formatted(
          JsonUtils.TAG_OBJECT_TYPE, JsonTypes.TYPE_JOB_TEST,
          JsonUtils.FIELD_VER, VERSION,
          JsonUtils.FIELD_JOB_ID, JOB_ID,
          JsonUtils.FIELD_TASK_ID, TASK_ID,
          JsonUtils.FIELD_EVENT_TIME, EVENT_TIME_STRING);

  private CoreTestJob coreTestJobExpected;

  @BeforeEach
  public void setup() {
    this.coreTestJobExpected = new CoreTestJob(VERSION, JOB_ID, TASK_ID, EVENT_TIME);
  }

  @Test
  void create() throws CoreException {
    CoreTestJob event = createJob();
    assertEquals(this.coreTestJobExpected, event);
    System.out.print(TestUtils.getJSONforClass(JSON_OBJECT_FACTORY, event));
    System.out.print(TestUtils.getJSONSchemaforClass(event.getClass()));
  }

  @Test
  void jsonToObjects() throws CoreException {
    CoreTestJob event = JSON_OBJECT_FACTORY.fromString(JSON_CORE_TEST_JOB, CoreTestJob.class);
    assertEquals(this.coreTestJobExpected, event);
  }

  @Test
  void objectToJson() throws CoreException, JsonMappingException, JsonProcessingException {
    CoreTestJob event = createJob();
    String json = JSON_OBJECT_FACTORY.toString(event);
    ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    JsonNode jsonTree = objectMapper.readTree(json);
    JsonNode jsonTreeExpected = objectMapper.readTree(JSON_CORE_TEST_JOB);
    String formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
    String formattedJsonExpected = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTreeExpected);
    assertEquals(formattedJsonExpected, formattedJson);
  }

  @Test
  void createWithSimpleConstructor() {
    CoreTestJob event1 = new CoreTestJob(VERSION, JOB_ID, TASK_ID, EVENT_TIME);
    CoreTestJob event2 = new CoreTestJob(VERSION, UUID.randomUUID().toString(), CoreUtils.generateUuid(), EVENT_TIME);
    assertNotEquals(event1, event2);
    assertEquals(event1.eventTime(), event2.eventTime());
    assertEquals(event1.ver(), event2.ver());
  }

  @Test
  void createWithBuilderToString() {
    CoreTestJob event = createJob();
    String builderString = event.toBuilder().toString();
    String builderStringExpected = this.coreTestJobExpected.toBuilder().toString();
    assertEquals(builderStringExpected, builderString);
  }

  @Test
  void testFullBuilder() {
    CoreTestJobBuilder builder = CoreTestJob.builder();
    builder.build();
    builder.ver(VERSION);
    builder.id(JOB_ID);
    builder.taskId(TASK_ID);
    builder.eventTime(EVENT_TIME);
    builder.build();
  }

  @Test
  void testShortBuilder() {
    CoreTestJobBuilder builder = CoreTestJob.builder();
    builder.build();
  }  

  private CoreTestJob createJob() {
    CoreTestJob job = CoreTestJob.builder()
        .ver(CoreTestEvent.CURRENT_VERSION)
        .id(JOB_ID)
        .taskId(TASK_ID)
        .eventTime(EVENT_TIME)
        .build();

    return job;
  }

}