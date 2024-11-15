package media.alera.osgi.core.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import media.alera.osgi.core.init.CoreVariableService;
import media.alera.osgi.core.shared.web.EnvSystemProperty;

/**
 * Variable Service used to retrieve .env variabels that are set for the application
 * 
 * Deafults to https://<server_name>:<port>/variables/property/{property_name}
 */
@JaxrsResource
@Path("variables")
@Component(enabled = true, immediate = true, service=WebVariableService.class)
public class WebVariableService {

  private CoreVariableService variableService;

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  public void setLocalEventAdmin(final CoreVariableService vs) {
    this.variableService = vs;
  }

  public void unsetLocalEventAdmin(final CoreVariableService vs) {
    this.variableService = null;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/property/{name}")
  public Response getEnvSystemProperty(@PathParam("name")String name) {
    String envSystemProperty = variableService.getEnvSystemProperty(name, null);
    EnvSystemProperty propery = EnvSystemProperty.builder().name(name).value(envSystemProperty).build();
    return Response.ok(propery).build();
  }

}