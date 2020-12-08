import com.lowes.eoms.eordermodservices.constants.EomsConstants;
import com.lowes.eoms.eordermodservices.exception.EomsException;
import com.lowes.eoms.eordermodservices.util.IntegratorUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Filter that log Request Payload and Turnaround Execution Time for each Request
 *
 * @author Naveen Devarajaiah
 */
@Component
@Slf4j
public class RequestPayloadFilter implements WebFilter {

  @Value("${spring.application.name}")
  private String appName;

  @Value("${spring.profiles.active}")
  private String profile;

  @Value("${requestFilter.ignorePatterns}")
  private String ignorePatterns;

  public static final ByteArrayOutputStream EMPTY_BYTE_ARRAY_OUTPUT_STREAM =
      new ByteArrayOutputStream(0);

  public RequestPayloadFilter() {}

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (ignorePatterns != null
        && exchange.getRequest().getURI().getPath().matches(ignorePatterns)) {
      return chain.filter(exchange);
    } else {
      long startTime = System.currentTimeMillis();
      ServerWebExchangeDecorator exchangeDecorator = reqRespLoggingDeecorator(exchange);
      Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap();
      
      if (headers.containsKey(EomsConstants.X_TRACE_ID)) {
        exchange.getResponse().getHeaders().add(EomsConstants.X_TRACE_ID,
            headers.get(EomsConstants.X_TRACE_ID));
      }
      
      return chain.filter(exchangeDecorator)
          .doOnSuccess(aVoid -> logRequestDetails(exchange, startTime, true, HttpStatus.OK.value()))
          .doOnError(throwable -> {
            int errorCode = getErrorCodeFromException(throwable);
            logRequestDetails(exchange, startTime, false, errorCode);
          }).subscriberContext(context -> {
            if (headers.containsKey(EomsConstants.X_TRACE_ID)) {
              return context.put(EomsConstants.X_TRACE_ID, headers.get(EomsConstants.X_TRACE_ID));
            }
            return context;
          });
    }
  }

  private int getErrorCodeFromException(Throwable throwable) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    if (throwable instanceof EomsException) {
      httpStatus = Optional.of(((EomsException) throwable).getStatusCode())
          .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    if (throwable instanceof WebExchangeBindException) {
      httpStatus = ((WebExchangeBindException) throwable).getStatus();
    }

    return httpStatus.value();
  }

  private ServerWebExchangeDecorator reqRespLoggingDeecorator(ServerWebExchange exchange) {
    return new ServerWebExchangeDecorator(exchange) {
      @Override
      public ServerHttpRequest getRequest() {
        return new RequestLoggingInterceptor(super.getRequest(), exchange);
      }
    };
  }

  private void logRequestDetails(ServerWebExchange exchange, long startTime, boolean status,
      int statusCode) {

    long totalTime = System.currentTimeMillis() - startTime;

    Map<String, String> reqDetailsMap =
        (Map<String, String>) exchange.getAttributes().get("reqDetailsMap");

    /**
     * For Requests with Body i.e. POST
     */
    if (reqDetailsMap != null) {
      log.info(
          "Request: appName={}, profile={}, method={}, uri={}, x-lowes-uuid={}, payload={} ");
    }
    /**
     * For GET Requests
     */
    else {
      Map<String, String> requestDetails = new HashMap<>();
      updateRequestDetailsWithHeaders(requestDetails, exchange.getRequest().getHeaders());
      log.info(
          "Request: appName={}, profile={}, method={}, uri={}, x-lowes-uuid={}, Turnaround Execution Time ms={}, success={}, statusCode={}");
    }
  }

  public class RequestLoggingInterceptor extends ServerHttpRequestDecorator {

    private ServerWebExchange exchange;

    public RequestLoggingInterceptor(ServerHttpRequest delegate, ServerWebExchange exchange) {
      super(delegate);
      this.exchange = exchange;
    }

    @Override
    public Flux<DataBuffer> getBody() {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      return super.getBody().map(dataBuffer -> {
        try {
          Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
          String body = IOUtils.toString(baos.toByteArray(), "UTF-8");
          Map<String, String> reqDetailsMap;
          if (getDelegate().getPath().toString().contains("getOrderList")) {
            reqDetailsMap = buildRequestDetailsMapForOrderList(getDelegate().getMethod(),
                getDelegate().getPath(), body, getDelegate().getHeaders());

          } else if (getDelegate().getPath().toString().contains("createAppointment")) {
            reqDetailsMap = buildRequestDetailsMapForCreateAppointment(getDelegate().getMethod(),
                getDelegate().getPath(), body, getDelegate().getHeaders());
          } else {
            reqDetailsMap = buildRequestDetailsMap(getDelegate().getMethod(),
                getDelegate().getPath(), body, getDelegate().getHeaders());
          }
          exchange.getAttributes().put("reqDetailsMap", reqDetailsMap);
        } catch (IOException e) {
          log.error("Error while logging request body.", e);
        }
        return dataBuffer;
      });
    }
  }

  private Map<String, String> buildRequestDetailsMap(HttpMethod method, RequestPath path,
      String body, HttpHeaders httpHeaders) {
    Map<String, String> requestDetails = new HashMap<>();
    requestDetails.put(EomsConstants.REQUEST_LOG_METHOD, method.name());
    requestDetails.put(EomsConstants.REQUEST_LOG_URI, path.value());
    requestDetails.put(EomsConstants.REQUEST_LOG_PAYLOAD, body);
    updateRequestDetailsWithHeaders(requestDetails, httpHeaders);
    return requestDetails;
  }
}
