package ee.taltech.elastic.errors;

import ee.taltech.elastic.exception.BadRequestException;
import ee.taltech.elastic.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ToString
public class RestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> elasticsearchBadRequestException(BadRequestException ex) {
        log.error("AtuElasticsearchBadRequestException", ex);
        RestError restError = new RestError(BAD_REQUEST.value(), "Bad request");
        restError.setErrorCode(ex.getCode());
        restError.setMoreInfo(ex.getMessage());
        return new ResponseEntity<>(restError, BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> elasticsearchNotFoundException(NotFoundException ex) {
        log.error("AtuElasticsearchNotFoundException", ex);
        RestError restError = new RestError(NOT_FOUND.value(), "Not found");
        restError.setErrorCode(ex.getCode());
        restError.setMoreInfo(ex.getMessage());
        return new ResponseEntity<>(restError, NOT_FOUND);
    }
}
