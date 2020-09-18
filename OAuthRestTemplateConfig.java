import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;
import com.lowes.eoms.eordermodservices.interceptor.OAuthClientCredentialsRestTemplateInterceptorConfig;

/**
 * Spring Security (OAuth 2.0) Rest Template Configuration
 * 
 * @author Naveen Devarajaiah
 *
 */
@Configuration
public class OAuthRestTemplateConfig {
  
  @Value(value = "${spring.security.oauth2.client.provider.ibmApic.token-uri}")
  private String tokenURI;
  
  @Value(value = "${spring.security.oauth2.client.registration.ibmApic.client-id}")
  private String clientId;
  
  @Value(value = "${spring.security.oauth2.client.registration.ibmApic.client-secret}")
  private String clientSecret;
  
  @Value(value = "${spring.security.oauth2.client.registration.ibmApic.authorization-grant-type}")
  private String authorizationGrantType;
  
  @Value(value = "#{'${spring.security.oauth2.client.registration.ibmApic.scope}'.split(', ')}")
  private List<String> scope;
  
  @Bean
  @Qualifier("oAuthRestTemplate")
  public RestTemplate oAuthRestTemplate() {

    ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("ibmApic")
        .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType)).clientId(clientId)
        .clientSecret(clientSecret).tokenUri(tokenURI).scope(scope).build();

    ClientRegistrationRepository clientRegistrationRepository =
        new InMemoryClientRegistrationRepository(clientRegistration);

    InMemoryOAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService =
        new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);

    OAuth2AuthorizedClientProvider authorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
            inMemoryOAuth2AuthorizedClientService);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .setInterceptors(Arrays.asList(new OAuthClientCredentialsRestTemplateInterceptorConfig(
            authorizedClientManager, clientRegistration)));

    return restTemplate;
  }

}
