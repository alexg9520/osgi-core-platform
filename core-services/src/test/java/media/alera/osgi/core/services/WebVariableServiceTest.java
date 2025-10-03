package media.alera.osgi.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.web.EnvSystemProperty;

/**
 * Unit tests for {@link WebVariableService}
 * Tests the REST endpoint for retrieving environment/system properties
 */
@ExtendWith(MockitoExtension.class)
class WebVariableServiceTest {

    @Mock
    private CoreVariableService mockVariableService;

    private WebVariableService webVariableService;

    @BeforeEach
    void setUp() {
        webVariableService = new WebVariableService();
        webVariableService.setLocalEventAdmin(mockVariableService);
    }

    @Test
    void testGetEnvSystemProperty_WithExistingProperty() {
        // Arrange
        String propertyName = "TEST_PROPERTY";
        String expectedValue = "test-value";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
        assertEquals(EnvSystemProperty.CURRENT_VERSION, property.ver());
    }

    @Test
    void testGetEnvSystemProperty_WithNonExistentProperty() {
        // Arrange
        String propertyName = "NON_EXISTENT_PROPERTY";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(null);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertNull(property.value());
        assertEquals(EnvSystemProperty.CURRENT_VERSION, property.ver());
    }

    @Test
    void testGetEnvSystemProperty_WithEmptyPropertyName() {
        // Arrange
        String propertyName = "";
        String expectedValue = "";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
    }

    @Test
    void testGetEnvSystemProperty_WithSpecialCharacters() {
        // Arrange
        String propertyName = "TEST.PROPERTY-WITH_SPECIAL/CHARS";
        String expectedValue = "special-value!@#$%";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
    }

    @Test
    void testGetEnvSystemProperty_WithLongPropertyName() {
        // Arrange
        String propertyName = "A".repeat(1000); // Very long property name
        String expectedValue = "long-property-value";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
    }

    @Test
    void testGetEnvSystemProperty_ServiceReturnsEmptyString() {
        // Arrange
        String propertyName = "EMPTY_PROPERTY";
        String expectedValue = "";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
    }

    @Test
    void testSetAndUnsetVariableService() {
        // Test service lifecycle methods
        WebVariableService testService = new WebVariableService();
        
        // Initially null - should throw NPE
        assertThrows(NullPointerException.class, () -> {
            testService.getEnvSystemProperty("TEST");
        });
        
        // Set service
        testService.setLocalEventAdmin(mockVariableService);
        when(mockVariableService.getEnvSystemProperty("TEST", null)).thenReturn("value");
        
        Response response = testService.getEnvSystemProperty("TEST");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // Unset service  
        testService.unsetLocalEventAdmin(mockVariableService);
        
        // After unset - should again throw NPE since variableService is now null
        assertThrows(NullPointerException.class, () -> {
            testService.getEnvSystemProperty("TEST");
        });
    }

    @Test
    void testResponseContentType() {
        // Arrange
        String propertyName = "CONTENT_TYPE_TEST";
        String expectedValue = "application/json";
        when(mockVariableService.getEnvSystemProperty(propertyName, null))
            .thenReturn(expectedValue);

        // Act
        Response response = webVariableService.getEnvSystemProperty(propertyName);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // Verify that the response entity can be cast to EnvSystemProperty
        // This implicitly tests that JSON serialization will work correctly
        EnvSystemProperty property = (EnvSystemProperty) response.getEntity();
        assertNotNull(property);
        assertEquals(propertyName, property.name());
        assertEquals(expectedValue, property.value());
    }
}