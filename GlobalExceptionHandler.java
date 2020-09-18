import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import com.lowes.eoms.eordermodservices.model.sterling.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Global exception handling for REST APIs invoked used WebClient
 * 
 * @author Naveen Devarajaiah
 *
 */
@Component
@Order(-2)
public class GlobalExceptionHandler<T extends Throwable> extends
    AbstractErrorWebExceptionHandler {

  @Autowired
  private ErrorResponseComposer<T> errorResponseComposer;

  public GlobalExceptionHandler(ErrorAttributes errorAttributes,
      ResourceProperties resourceProperties, ApplicationContext applicationContext) {
    super(errorAttributes, resourceProperties, applicationContext);
    ServerCodecConfigurer serverCodecConfigurer = new DefaultServerCodecConfigurer();
    setMessageWriters(serverCodecConfigurer.getWriters());
    setMessageReaders(serverCodecConfigurer.getReaders());
  }

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
    return buildCustomErrorAttrs(request);
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  /**
   * Method invoked when there is any error while constructing WebClient Request. Also Error Message
   * is constructed in below format to keep consistency.
   *
   * {
   *    "exceptionCode": "Some Code",
   *    "exceptionMessage": "Some Description"
   * }
   *
   * @param serverRequest
   * @return ServerResponse
   */
  private Mono<ServerResponse> renderErrorResponse(ServerRequest serverRequest) {

    final Map<String, Object> errorAttributes = getErrorAttributes(serverRequest, false);
    final int httpStatus = (Integer) errorAttributes.remove(ErrorAttribute.STATUS.value);
    return ServerResponse.status(httpStatus).contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorAttributes));
  }

  private Map<String, Object> buildCustomErrorAttrs(ServerRequest request) {

    Map<String, Object> errorAttributes = super.getErrorAttributes(request, false);
    Throwable ex = getError(request);
    Map<String,Object> customErrorAttrsMap = new HashMap<>();

    Optional<ErrorResponse> errorResponseOptional =  errorResponseComposer.compose((T)ex);

    if(errorResponseOptional.isPresent()){
      ErrorResponse errorResponse = errorResponseOptional.get();
      customErrorAttrsMap.put(ErrorAttribute.EXCEPTION_MESSAGE.value,errorResponse.getExceptionMessage());
      customErrorAttrsMap.put(ErrorAttribute.EXCEPTION_CODE.value,errorResponse.getExceptionCode());
      customErrorAttrsMap.put(ErrorAttribute.STATUS.value,errorResponse.getHttpStatus().value());

      return customErrorAttrsMap;
    }else{
      return errorAttributes;
    }
  }

  enum ErrorAttribute {
    EXCEPTION_MESSAGE("exceptionMessage"),
    EXCEPTION_CODE("exceptionCode"),
    STATUS("status");

    private final String value;

    ErrorAttribute(String value) {
      this.value = value;
    }
  }
}
