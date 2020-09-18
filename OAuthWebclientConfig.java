import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring Security (OAuth 2.0) WebClient Configuration
 *
 * <p>
 * Autowire
 * <p>
 *      @Qualifier("apicOAuthWebClientInvoker")
 *      private AbstractRestApiInvoker apicOAuthWebClient;
 * <p>
 * in respective classes if OAuth secured APIs are to be invoked.
 * 
 * Ex: Item Price, Item Selling Restrictions APIC URLs are secured using OAuth
 * <p>
 * 
 * @author Naveen Devarajaiah
 *
 */
@Configuration
public class OAuthWebclientConfig {

  @Bean("apicOAuthWebClient")
  public WebClient apicOAuthWebClient(ReactiveClientRegistrationRepository clientRegistrations) {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations,
            new WebSessionServerOAuth2AuthorizedClientRepository());
    oauth.setDefaultClientRegistrationId("ibmApic");
    return WebClient.builder().filter(oauth).build();

  }

}
