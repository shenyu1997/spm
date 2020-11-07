package tech.kuiperbelt.spm.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.kuiperbelt.spm.SpmApplication;

import java.util.Optional;

@RestControllerAdvice(basePackageClasses = {SpmApplication.class, RepositoryRestExceptionHandler.class})
public class ExceptionHandlerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage illegalArgumentException(IllegalArgumentException ex) {
        return ErrorMessage.builder().message(ex.getMessage()).build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage runtimeException(RuntimeException ex) {
        String message = Optional.of(ex.getMessage())
                .orElse(ex.getClass().getSimpleName());
        return ErrorMessage.builder().message(message).build();
    }

    @Getter
    @Setter
    @Builder
    public static class ErrorMessage {
        private String message;
    }
}
