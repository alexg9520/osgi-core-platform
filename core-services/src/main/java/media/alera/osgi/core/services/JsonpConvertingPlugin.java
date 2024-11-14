package media.alera.osgi.core.services;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsExtension;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsMediaType;

import media.alera.osgi.core.shared.JsonFactory;

@Component(scope = PROTOTYPE)
@JaxrsExtension
@JaxrsMediaType(APPLICATION_JSON)
public class JsonpConvertingPlugin<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

  private JsonFactory factory;

  @Reference(cardinality = ReferenceCardinality.MANDATORY, policyOption = ReferencePolicyOption.GREEDY)
  public void setFactory(final JsonFactory jsonFactory) {
    this.factory = jsonFactory;
  }

  public void unsetFactory(final JsonFactory jsonFactory) {
    this.factory = null;
  }

  @Override
  public boolean isWriteable(final Class<?> c, final Type t, final Annotation[] a, final MediaType mediaType) {
    return APPLICATION_JSON_TYPE.isCompatible(mediaType) || mediaType.getSubtype().endsWith("+json");
  }

  @Override
  public boolean isReadable(final Class<?> c, final Type t, final Annotation[] a, final MediaType mediaType) {
    return APPLICATION_JSON_TYPE.isCompatible(mediaType) || mediaType.getSubtype().endsWith("+json");
  }

  @Override
  public void writeTo(final T o, final Class<?> c, final Type type, final Annotation[] annotation, final MediaType mediaType, final MultivaluedMap<String, java.lang.Object> map, final OutputStream out) throws IOException, WebApplicationException {
    this.factory.getMapper().writerWithDefaultPrettyPrinter().writeValue(out, o);
  }

  @Override
  public T readFrom(final Class<T> c, final Type type, final Annotation[] annotation, final MediaType mediaType, final MultivaluedMap<String, String> map, final InputStream in) throws IOException, WebApplicationException {
    return this.factory.getMapper().readValue(in, c);
  }
}