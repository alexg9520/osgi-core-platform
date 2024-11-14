package media.alera.osgi.core.shared.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import media.alera.osgi.core.shared.CoreException;
import media.alera.osgi.core.shared.CoreUtils;
import media.alera.osgi.core.shared.JsonFactory;

public class TestUtils {

  private TestUtils() {
    // NO-OP
  }

  public static String getJSONforClass(final JsonFactory factory, final Object jsonObj) throws CoreException {
    return String.format("%s%n%s%n%s%n%s%n", CoreUtils.DIVIDER, jsonObj.getClass().getSimpleName(), factory.toString(jsonObj, true), CoreUtils.DIVIDER);
  }

  public static String getJSONSchemaforClass(final Class<?> clz) {
    JacksonModule jacksonModul = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED, JacksonOption.FLATTENED_ENUMS_FROM_JSONPROPERTY);
    SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
      .with(jacksonModul);
    SchemaGeneratorConfig config = configBuilder.build();
    SchemaGenerator generator = new SchemaGenerator(config);
    JsonNode jsonSchema = generator.generateSchema(clz);
    return String.format(CoreUtils.getSchemaForConsole(jsonSchema.toPrettyString(), clz));
  }
}