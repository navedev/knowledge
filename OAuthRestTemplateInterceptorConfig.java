import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * Interceptor Class to get Access Token from APIC and use that as Bearer token for subsequent
 * requests
 * 
 * @author Naveen Devarajaiah
 *
 */
public class OAuthRestTemplateInterceptorConfig
    implements ClientHttpRequestInterceptor {

  private OAuth2AuthorizedClientManager manager;
  private Authentication principal;
  private ClientRegistration clientRegistration;

  public OAuthClientCredentialsRestTemplateInterceptorConfig(OAuth2AuthorizedClientManager manager,
      ClientRegistration clientRegistration) {
    this.manager = manager;
    this.clientRegistration = clientRegistration;
    this.principal = createPrincipal();
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    
    OAuth2AuthorizeRequest oAuth2AuthorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistration.getRegistrationId())
            .principal(principal).build();
    
    OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);
    if (Objects.isNull(client)) {
      throw new IllegalStateException("Failed to retrieve Access Token for Client with ID: "
          + clientRegistration.getRegistrationId() + " as Client is Null");
    }

    request.getHeaders().add(HttpHeaders.AUTHORIZATION,
        "Bearer " + client.getAccessToken().getTokenValue());
    return execution.execute(request, body);
  }

  private Authentication createPrincipal() {
    return new Authentication() {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
      }

      @Override
      public Object getCredentials() {
        return null;
      }

      @Override
      public Object getDetails() {
        return null;
      }

      @Override
      public Object getPrincipal() {
        return this;
      }

      @Override
      public boolean isAuthenticated() {
        return false;
      }

      @Override
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}

      @Override
      public String getName() {
        return clientRegistration.getClientId();
      }
    };
  }

}
