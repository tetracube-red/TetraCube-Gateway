package red.tetracube.configuration.properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BearerTokenConfigurationProperties {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "mp.jwt.verify.audiences")
    Set<String> audiences;

    public String getIssuer() {
        return issuer;
    }

    public Set<String> getAudiences() {
        return audiences;
    }
}
