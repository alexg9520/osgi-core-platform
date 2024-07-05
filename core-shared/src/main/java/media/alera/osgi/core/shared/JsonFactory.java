package media.alera.osgi.core.shared;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// @Slf4j
@Component(enabled = true, immediate = true, service = JsonFactory.class)
public class JsonFactory {

  private static final String FAILED_TO_DESERIALIZE_JSON_AS = "Failed to deserialize json as ";

  // private RSAKey rsaJWK;
  // private RSAKey rsaPublicJWK;

  private ObjectMapper mapper;

  public JsonFactory() {
    this.mapper = JsonMapper.builder().addModule(new JavaTimeModule())
        .serializationInclusion(JsonInclude.Include.NON_EMPTY)
        .serializationInclusion(JsonInclude.Include.NON_NULL).build();
    setupPackageSetting(this.mapper);
  }

  @Activate
  public void activate() throws CoreException {
    // try {
    // // 2048-bit RSA signing key for RS256 alg
    // rsaJWK = new RSAKeyGenerator(2048)
    // .algorithm(JWSAlgorithm.RS256)
    // .keyUse(KeyUse.SIGNATURE)
    // .keyID("1")
    // .generate();

    // // // EC signing key with P-256 curve for ES256 alg
    // // ecJWK = new ECKeyGenerator(Curve.P_256)
    // // .algorithm(JWSAlgorithm.ES256)
    // // .keyUse(KeyUse.SIGNATURE)
    // // .keyID("2")
    // // .generate();

    // // Get the public keys to allow recipients to verify the signatures
    // rsaPublicJWK = rsaJWK.toPublicJWK();
    // // ecPublicJWK = ecJWK.toPublicJWK();
    // } catch (JOSEException jEx) {
    // log.error("Failed to setup JWS signature: " + jEx.getMessage(), jEx);
    // throw new CoreException("Failed to setup JWS signature: " +
    // jEx.getMessage());
    // }
  }

  @Deactivate
  public void deactivate() {
    // rsaJWK = null;
    // rsaPublicJWK = null;
  }

  public static void setupPackageSetting(final ObjectMapper mapper) {
    SimpleModule mod = new SimpleModule();
    mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(mod);
  }

  public <T> T fromBytes(final byte[] bytes, final Class<T> c) throws CoreException {
    try {
      return getMapper().readValue(bytes, c);
    } catch (IOException e) {
      throw new CoreException(FAILED_TO_DESERIALIZE_JSON_AS + c.getSimpleName(), e);
    }
  }

  // public <T> T fromSignedString(final String json, final Class<T> c) throws
  // CoreException {
  // try {
  // Payload detachedPayload = new Payload(json);

  // // Create and sign JWS
  // JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS512)
  // .base64URLEncodePayload(false)
  // .criticalParams(Collections.singleton("b64"))
  // .build();

  // // Create RSA-signer with the private key
  // JWSSigner signer = new RSASSASigner(rsaJWK);

  // JWSObject jwsObject = new JWSObject(header, detachedPayload);
  // jwsObject.sign(signer);

  // boolean isDetached = true;
  // String jws = jwsObject.serialize(isDetached);
  // System.out.println("Signature:" + jws);

  // // String recJson = json.replaceAll("a", "b");
  // // Payload recDetachedPayload = new Payload(recJson);
  // Payload recDetachedPayload = new Payload(json);

  // // Parse JWS with detached payload
  // JWSObject parsedJWSObject = JWSObject.parse(jws, recDetachedPayload);

  // // Verify the HMAC
  // if (parsedJWSObject.verify(new RSASSAVerifier(rsaPublicJWK))) {
  // System.out.println("Valid Signature");
  // return fromString(parsedJWSObject.getPayload().toString(), c);
  // } else {
  // System.out.println("Invalid Signature");
  // throw new CoreException("Failed to validate JWS signature");
  // }

  // // // Parse the JWS JSON
  // // JWSObjectJSON jwsObjectJSON = JWSObjectJSON.parse(json);

  // // // Verify the signatures with the available public JWKSs
  // // for (JWSObjectJSON.Signature sig: jwsObjectJSON.getSignatures()) {
  // // // The JWS kid header parameter is used to identify the signing key
  // // if (rsaPublicJWK.getKeyID().equals(sig.getHeader().getKeyID())) {
  // // if (! sig.verify(new RSASSAVerifier(rsaPublicJWK))) {
  // // System.out.println("Invalid RSA signature for key " +
  // rsaPublicJWK.getKeyID());
  // // }
  // // }
  // // if (ecPublicJWK.getKeyID().equals(sig.getHeader().getKeyID())) {
  // // if (! sig.verify(new ECDSAVerifier(ecPublicJWK))) {
  // // System.out.println("Invalid EC signature for key " + ecJWK.getKeyID());
  // // }
  // // }
  // // }

  // // if (JWSObjectJSON.State.VERIFIED.equals(jwsObjectJSON.getState())) {
  // // return fromString(jwsObjectJSON.getPayload().toString(), c);
  // // } else {
  // // throw new CoreException("Failed to validate JWS signature");
  // // }
  // } catch (JOSEException | ParseException jEx) {
  // log.error("Failed to validate JWS signature: " + jEx.getMessage(), jEx);
  // throw new CoreException("Failed to validate JWS signature: " +
  // jEx.getMessage());
  // }
  // }

  public <T> T fromString(final String json, final Class<T> c) throws CoreException {
    try {
      return getMapper().readValue(json, c);
    } catch (IOException e) {
      throw new CoreException(FAILED_TO_DESERIALIZE_JSON_AS + c.getSimpleName() + ": " + json, e);
    }
  }

  public <Y> Y fromStringType(final String json, final Class<Y> c) throws CoreException {
    return fromString(json, c);
  }

  public ObjectMapper getMapper() {
    return this.mapper;
  }

  public <T> String toString(final T p) throws CoreException {
    return toString(p, false);
  }

  public <T> String toString(final T p, final boolean isPretty) throws CoreException {
    try {
      if (isPretty) {
        return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(p);
      }
      return getMapper().writeValueAsString(p);
    } catch (JsonProcessingException e) {
      throw new CoreException("Failed to serialize " + p.getClass() + " to json", e);
    }
  }

  public <T> void toOutputStream(final T p, final OutputStream out) throws CoreException {
    toOutputStream(p, out, false);
  }

  public <T> void toOutputStream(final T p, final OutputStream out, final boolean isPretty) throws CoreException {
    try {
      if (isPretty) {
        getMapper().writerWithDefaultPrettyPrinter().writeValue(out, p);
      } else {
        getMapper().writeValue(out, p);
      }
      out.flush();
    } catch (IOException e) {
      throw new CoreException("Failed to serialize " + p.getClass() + " to json", e);
    }
  }

  @SuppressWarnings("java:S2445")
  public <T> void toFile(final T p, final Path path, final boolean isPretty) throws CoreException {
    synchronized (path) {
      try {
        StringBuilder content;
        if (isPretty) {
          content = new StringBuilder(getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(p));
        } else {
          content = new StringBuilder(getMapper().writeValueAsString(p));
        }
        Path tempSaveFile = path.resolveSibling(path.getFileName() + ".tmp");
        Files.write(tempSaveFile, content.toString().getBytes());
        if (Files.exists(path)) {
          while (!Files.deleteIfExists(path)) {
            synchronized (this) {
              this.wait(100);
            }
          }
        }
        Files.move(tempSaveFile, path);
      } catch (IOException e) {
        throw new CoreException("Failed to serialize as json: " + e.getMessage(), e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new CoreException("Failed delete file: " + e.getMessage(), e);
      }
    }
  }

  public <T> T fromFile(final Path file, final Class<T> c) throws CoreException {
    try {
      return fromBytes(Files.readAllBytes(file), c);
    } catch (IOException e) {
      throw new CoreException("Failed to deserialize json: " + file.getFileName(), e);
    }
  }
}
