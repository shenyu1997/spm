package tech.kuiperbelt.spm.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.kuiperbelt.spm.SpmApplication;

import java.util.Optional;

@Slf4j
@RestControllerAdvice(basePackageClasses = {SpmApplication.class, RepositoryRestExceptionHandler.class})
public class ExceptionHandlerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handle(IllegalArgumentException ex) {
        log.error("error: {}", Optional.ofNullable(ex.getMessage()).orElse(ex.getClass().getSimpleName()), ex);
        return ResponseEntity.badRequest()
                .body(ErrorMessage.builder()
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handle(Exception ex) {
        String message = Optional.of(ex.getMessage())
                .orElse(ex.getClass().getSimpleName());
        log.error("error: {}", message, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorMessage.builder()
                        .message(message)
                        .build());
    }

    @Getter
    @Setter
    @Builder
    public static class ErrorMessage {
        private String message;
    }
}
