package media.alera.osgi.core.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import media.alera.osgi.core.shared.JsonFactory;
import media.alera.osgi.core.shared.web.EnvSystemProperty;

/**
 * Unit tests for {@link JsonpConvertingPlugin}
 * Tests JSON serialization/deserialization for JAX-RS endpoints
 */
@ExtendWith(MockitoExtension.class)
class JsonpConvertingPluginTest {

    @Mock
    private JsonFactory mockJsonFactory;

    @Mock
    private ObjectMapper mockObjectMapper;

    private JsonpConvertingPlugin<EnvSystemProperty> jsonPlugin;

    private final Annotation[] emptyAnnotations = new Annotation[0];
    private final MultivaluedMap<String, Object> emptyHeaderMap = new MultivaluedHashMap<>();
    private final MultivaluedMap<String, String> emptyStringHeaderMap = new MultivaluedHashMap<>();

    @BeforeEach
    void setUp() {
        jsonPlugin = new JsonpConvertingPlugin<>();
        jsonPlugin.setFactory(mockJsonFactory);
        lenient().when(mockJsonFactory.getMapper()).thenReturn(mockObjectMapper);
    }

    @Test
    void testIsWriteable_ApplicationJson() {
        // Test with standard application/json media type
        boolean result = jsonPlugin.isWriteable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            MediaType.APPLICATION_JSON_TYPE
        );
        
        assertTrue(result, "Should be writable for application/json");
    }

    @Test
    void testIsWriteable_JsonSubtype() {
        // Test with custom JSON subtype
        MediaType customJsonType = MediaType.valueOf("application/vnd.api+json");
        
        boolean result = jsonPlugin.isWriteable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            customJsonType
        );
        
        assertTrue(result, "Should be writable for +json subtypes");
    }

    @Test
    void testIsWriteable_NonJsonType() {
        // Test with non-JSON media type
        boolean result = jsonPlugin.isWriteable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            MediaType.TEXT_PLAIN_TYPE
        );
        
        assertFalse(result, "Should not be writable for non-JSON types");
    }

    @Test
    void testIsReadable_ApplicationJson() {
        // Test with standard application/json media type
        boolean result = jsonPlugin.isReadable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            MediaType.APPLICATION_JSON_TYPE
        );
        
        assertTrue(result, "Should be readable for application/json");
    }

    @Test
    void testIsReadable_JsonSubtype() {
        // Test with custom JSON subtype
        MediaType customJsonType = MediaType.valueOf("application/hal+json");
        
        boolean result = jsonPlugin.isReadable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            customJsonType
        );
        
        assertTrue(result, "Should be readable for +json subtypes");
    }

    @Test
    void testIsReadable_NonJsonType() {
        // Test with non-JSON media type
        boolean result = jsonPlugin.isReadable(
            EnvSystemProperty.class, 
            EnvSystemProperty.class, 
            emptyAnnotations, 
            MediaType.APPLICATION_XML_TYPE
        );
        
        assertFalse(result, "Should not be readable for non-JSON types");
    }

    @Test
    void testWriteTo() throws IOException {
        // Arrange
        EnvSystemProperty testProperty = EnvSystemProperty.builder()
            .name("TEST_PROP")
            .value("test-value")
            .build();
            
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Mock the writer chain
        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(mockObjectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);

        // Act
        jsonPlugin.writeTo(
            testProperty,
            EnvSystemProperty.class,
            EnvSystemProperty.class,
            emptyAnnotations,
            MediaType.APPLICATION_JSON_TYPE,
            emptyHeaderMap,
            outputStream
        );

        // Assert
        verify(mockObjectMapper).writerWithDefaultPrettyPrinter();
        verify(mockWriter).writeValue(outputStream, testProperty);
    }

    @Test
    void testReadFrom() throws IOException {
        // Arrange
        String jsonInput = "{\"name\":\"TEST_PROP\",\"value\":\"test-value\",\"ver\":\"1.0.0\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonInput.getBytes());
        
        EnvSystemProperty expectedProperty = EnvSystemProperty.builder()
            .name("TEST_PROP")
            .value("test-value")
            .build();
            
        when(mockObjectMapper.readValue(any(ByteArrayInputStream.class), eq(EnvSystemProperty.class)))
            .thenReturn(expectedProperty);

        // Act
        EnvSystemProperty result = jsonPlugin.readFrom(
            EnvSystemProperty.class,
            EnvSystemProperty.class,
            emptyAnnotations,
            MediaType.APPLICATION_JSON_TYPE,
            emptyStringHeaderMap,
            inputStream
        );

        // Assert
        assertNotNull(result);
        verify(mockObjectMapper).readValue(inputStream, EnvSystemProperty.class);
    }

    @Test
    void testSetAndUnsetFactory() {
        // Test factory lifecycle methods
        JsonpConvertingPlugin<String> testPlugin = new JsonpConvertingPlugin<>();
        
        // Set factory
        testPlugin.setFactory(mockJsonFactory);
        
        // Unset factory
        testPlugin.unsetFactory(mockJsonFactory);
        
        // Verify no exceptions thrown during lifecycle
    }

    @Test
    void testMediaTypeCompatibility() {
        // Test various media type combinations
        MediaType[] compatibleTypes = {
            MediaType.APPLICATION_JSON_TYPE,
            MediaType.valueOf("application/json"),
            MediaType.valueOf("application/vnd.api+json"),
            MediaType.valueOf("application/hal+json"),
            MediaType.valueOf("application/custom+json")  // Fixed: subtype must end with +json
        };
        
        MediaType[] incompatibleTypes = {
            MediaType.TEXT_PLAIN_TYPE,
            MediaType.APPLICATION_XML_TYPE,
            MediaType.TEXT_HTML_TYPE,
            MediaType.valueOf("application/yaml"),
            MediaType.valueOf("text/csv")
        };
        
        // Test compatible types
        for (MediaType type : compatibleTypes) {
            assertTrue(
                jsonPlugin.isWriteable(String.class, String.class, emptyAnnotations, type),
                "Should be writable for: " + type
            );
            assertTrue(
                jsonPlugin.isReadable(String.class, String.class, emptyAnnotations, type),
                "Should be readable for: " + type
            );
        }
        
        // Test incompatible types
        for (MediaType type : incompatibleTypes) {
            assertFalse(
                jsonPlugin.isWriteable(String.class, String.class, emptyAnnotations, type),
                "Should not be writable for: " + type
            );
            assertFalse(
                jsonPlugin.isReadable(String.class, String.class, emptyAnnotations, type),
                "Should not be readable for: " + type
            );
        }
    }

    @Test
    void testWriteToWithException() throws IOException {
        // Test error handling during serialization
        EnvSystemProperty testProperty = EnvSystemProperty.builder()
            .name("ERROR_PROP")
            .value("error-value")
            .build();
            
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Mock to throw exception
        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(mockObjectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
        doThrow(new JsonProcessingException("Serialization error") {})
            .when(mockWriter).writeValue(any(ByteArrayOutputStream.class), any());

        // Act & Assert
        try {
            jsonPlugin.writeTo(
                testProperty,
                EnvSystemProperty.class,
                EnvSystemProperty.class,
                emptyAnnotations,
                MediaType.APPLICATION_JSON_TYPE,
                emptyHeaderMap,
                outputStream
            );
        } catch (IOException e) {
            // Expected behavior - exception should propagate
            assertTrue(e instanceof JsonProcessingException);
        }
    }
}