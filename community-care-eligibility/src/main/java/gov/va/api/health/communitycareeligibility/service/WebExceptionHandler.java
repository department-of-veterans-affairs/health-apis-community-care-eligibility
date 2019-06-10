package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exceptions that escape the rest controllers will be processed by this handler. It will convert
 * exception into different HTTP status codes and produce an error response payload.
 */
@Slf4j
@RestControllerAdvice
@RequestMapping(produces = "application/json")
public class WebExceptionHandler {
  @ExceptionHandler({
    ConstraintViolationException.class,
    Exceptions.UnknownServiceTypeException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse.BadRequest handleBadRequest(Exception e) {
    return responseFor(new ErrorResponse.BadRequest(), e);
  }

  @ExceptionHandler({Exceptions.UnknownPatientIcnException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse.NotFound handleNotFound(Exception e) {
    return responseFor(new ErrorResponse.NotFound(), e);
  }

  @ExceptionHandler({
    Exceptions.EeUnavailableException.class,
    Exceptions.FacilitiesUnavailableException.class
  })
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ErrorResponse.ServiceUnavailable handleServiceUnavailable(Exception e) {
    return responseFor(new ErrorResponse.ServiceUnavailable(), e);
  }

  @ExceptionHandler({Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse.InternalServerError handleSnafu(Exception e) {
    return responseFor(new ErrorResponse.InternalServerError(), e);
  }

  private <T extends ErrorResponse> T responseFor(T response, Exception e) {
    ErrorResponse.applyException(response, e);
    log.error("{}: {}", response.type(), response.message(), e);
    return response;
  }
}
