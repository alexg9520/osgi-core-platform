# OSGi Core Platform - AI Agent Instructions

## Architecture Overview

This is a modular OSGi-based platform with 5 main modules:
- **core-init**: Bootstrap services, environment variable management, configuration
- **core-shared**: Common utilities, event handling, JSON processing, core exceptions
- **core-services**: REST endpoints using JAX-RS (`@JaxrsResource`, `@Path`)
- **core-app**: Main application assembly and BND run configurations
- **core-app-test**: Integration testing with BND test runner

### Key Architectural Patterns

**OSGi Component Model**: All services use `@Component` with specific patterns:
```java
@Component(enabled = true, immediate = true, service = ServiceClass.class)
@GogoCommand(scope = "core", function = {"command1", "command2"}) // For CLI commands
```

**Event-Driven Architecture**: Local OSGi events + optional ActiveMQ integration
- Event handlers implement `EventHandler` and register topics via `EventConstants.EVENT_TOPIC`
- `EventBus` service coordinates local and remote messaging
- Topics defined in `EventTopics` constants

**Configuration Management**: Environment variables via `.env` files
- `CoreVariableService` loads .env and maps to system properties
- Use constants like `CORE_WEB_HTTPS_PORT`, `CORE_APP_SSH_PORT` for configuration keys
- Working directory controlled by `media.alera.osgi.core.working.folder` system property

## Development Workflow

### Essential Build Commands
```bash
# Standard build and verify
mvn clean verify

# Build with index export (for dependency resolution)
mvn clean verify -P index-export

# Run main application (blocking)
mvn package bnd-run:run@core-app -DskipTests

# Run with debugging
mvn package bnd-run:run@core-app-debug -DskipTests

# Integration tests
mvn package bnd-run:run@integration-test -DskipTests
```

### BND Configuration Files
- `*.bndrun` files define OSGi runtime configurations
- `core-app.bndrun`: Main application with start levels (SCR=1, logging=2, core-init=3)
- `integration-test.bndrun`: Test runtime extending core-app configuration
- Always specify `-runvm` properties for env/working folder paths

### Testing Patterns
- Use `@Test` with JUnit 5 in `core-shared/src/test/java`
- Integration tests in `core-app-test` run in full OSGi environment
- Test utilities in `TestUtils` class

## Code Conventions

### Service Registration
```java
@Component(enabled = true, immediate = true, service = {ServiceInterface.class})
public class ServiceImpl implements ServiceInterface {
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    public void setDependency(DependencyService dep) { }
    
    public void unsetDependency(DependencyService dep) { }
}
```

### REST Services
```java
@JaxrsResource
@Path("api-path") 
@Component(enabled = true, immediate = true, service = ServiceClass.class)
public class RestService {
    
    @GET
    @Path("/endpoint/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResource(@PathParam("param") String param) { }
}
```

### Event Handling
```java
@Component(service = EventHandler.class, property = {
    EventConstants.EVENT_TOPIC + "=" + EventTopics.YOUR_TOPIC
})
public class YourHandler implements EventHandler {
    public void handleEvent(Event event) { }
}
```

## Package Structure Convention
All code uses `media.alera.osgi.core.*` package naming:
- `*.init.*` - Bootstrap and configuration services
- `*.shared.*` - Cross-module utilities and interfaces  
- `*.services.*` - REST endpoints and web services
- `*.event.*` - Event handling and messaging

## Infrastructure Integration

### SSH Console Access
```bash
ssh localhost -p 2222 -l <user_id>  # Default OSGi console port
```

### ActiveMQ Integration
- Event bus can route to ActiveMQ topics/queues via `EventBusMessageProducer`
- Environment variables: `CORE_ACTIVEMQ_EVENTBUS_CONNECTION_*`
- Docker Compose includes Artemis + Nginx proxy setup

### Deployment
- Docker: Single JAR at `/app/core-app.jar` (ports 2222 SSH, 8443 HTTPS)
- Certificates in `certs/` directory for SSL/TLS
- Environment-specific `.env` files for configuration

## Critical Files to Understand
- `core-app/core-app.bndrun`: Main runtime assembly and bundle start levels
- `core-init/src/main/java/media/alera/osgi/core/init/CoreVariableService.java`: Configuration bootstrapping
- `core-shared/src/main/java/media/alera/osgi/core/shared/event/EventBus.java`: Event coordination
- `pom.xml`: Maven multi-module setup with BND tooling and OSGi bundle headers

When making changes, always verify bundle dependencies and ensure proper OSGi service registration patterns.