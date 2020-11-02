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
  
  @Autowired
  private OAuth2ClientProperties oAuth2ClientProperties;

  /**
   * Custom OAuth Rest Template
   * 
   * @return - returns OAuth Rest Template
   */
  @Bean
  @Qualifier("oAuthRestTemplate")
  public RestTemplate oAuthRestTemplate() {

    ImmutablePair<AuthorizedClientServiceOAuth2AuthorizedClientManager, List<ClientRegistration>> clientManagerClientListPair =
        constructClientRegistrationList();

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setInterceptors(Arrays.asList(new OAuthRestTemplateInterceptorConfig(
        clientManagerClientListPair.getLeft(), clientManagerClientListPair.getRight())));

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setReadTimeout(readTimeout);
    requestFactory.setConnectTimeout(connectTimeout);
    restTemplate.setRequestFactory(requestFactory);

    return restTemplate;
  }

  /**
   * Custom OAuth Async Rest Template
   * 
   * @param threadPoolTaskExecutor {@link ThreadPoolTaskExecutor}
   * @return - returns OAuth Async Rest Template
   */
  @Bean
  @Qualifier("asyncOAuthRestTemplate")
  public AsyncRestTemplate asyncOAuthRestTemplate(ThreadPoolTaskExecutor threadPoolTaskExecutor) {

    ImmutablePair<AuthorizedClientServiceOAuth2AuthorizedClientManager, List<ClientRegistration>> clientManagerClientListPair =
        constructClientRegistrationList();

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setTaskExecutor(threadPoolTaskExecutor);
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);

    AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
    asyncRestTemplate.setAsyncRequestFactory(requestFactory);
    asyncRestTemplate.setInterceptors(Arrays.asList(new AsyncOAuthRestTemplateInterceptorConfig(
        clientManagerClientListPair.getLeft(), clientManagerClientListPair.getRight())));

    return asyncRestTemplate;

  }

  private ImmutablePair<AuthorizedClientServiceOAuth2AuthorizedClientManager, List<ClientRegistration>> constructClientRegistrationList() {

    List<ClientRegistration> clientRegistrationList = new ArrayList<>();

    oAuth2ClientProperties.getProvider().forEach((providerId, providerObj) -> {

      Registration registration = oAuth2ClientProperties.getRegistration().entrySet().stream()
          .filter(registrationObj -> registrationObj.getKey().equalsIgnoreCase(providerId))
          .findAny().get().getValue();

      clientRegistrationList.add(ClientRegistration.withRegistrationId(providerId)
          .authorizationGrantType(
              new AuthorizationGrantType(registration.getAuthorizationGrantType()))
          .clientId(registration.getClientId()).clientSecret(registration.getClientSecret())
          .tokenUri(providerObj.getTokenUri()).scope(registration.getScope()).build());

    });

    ClientRegistrationRepository clientRegistrationRepository =
        new InMemoryClientRegistrationRepository(clientRegistrationList);

    InMemoryOAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService =
        new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);

    OAuth2AuthorizedClientProvider authorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
            inMemoryOAuth2AuthorizedClientService);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    return ImmutablePair.of(authorizedClientManager, clientRegistrationList);

  }

}
