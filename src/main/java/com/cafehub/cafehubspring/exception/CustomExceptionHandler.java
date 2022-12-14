package com.cafehub.cafehubspring.exception;

import com.cafehub.cafehubspring.exception.http.InternalServerErrorException;
import com.cafehub.cafehubspring.exception.http.NotAcceptableException;
import com.cafehub.cafehubspring.exception.http.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 400 Bad Request |
     * 잘못된 응답 문법으로 인하여 서버가 요청하여 이해할 수 없습니다.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        String responseMessage = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        String responseCode;
        LocalDateTime timestamp = LocalDateTime.now();

        if (Objects.requireNonNull(responseMessage).contains("입력")) {
            responseCode = "FIELD_REQUIRED";
        } else {
            responseCode = exception.getFieldError().getField().toUpperCase();
        }

        log.error("ERROR | " + responseMessage + " At " + timestamp + " | "
                + exception.getFieldError().getField() + " = " + exception.getFieldError().getRejectedValue());

        return ResponseEntity.status(status).body(
                DefaultExceptionResponseDto.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build()
        );
    }

    /**
     * 404 Not Found |
     * 요청받은 리소스를 찾을 수 없습니다.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception) {
        String responseMessage = exception.getMessage();
        String responseCode = "NOT_FOUND";
        LocalDateTime timestamp = LocalDateTime.now();

        log.error("ERROR | " + responseMessage + " At " + timestamp + " | " + exception);

        return ResponseEntity.status(404).body(
                DefaultExceptionResponseDto.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build()
        );
    }

    /**
     * 405 Method Not Allowed |
     * 요청한 메소드는 서머에서 알고 있지만, 제거되었고 사용할 수 없습니다.
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception,
                                                                         HttpHeaders headers,
                                                                         HttpStatus status,
                                                                         WebRequest request) {
        String responseMessage = "사용할 수 없는 메소드입니다";
        String responseCode = "METHOD_NOT_ALLOWED";
        LocalDateTime timestamp = LocalDateTime.now();

        log.error("ERROR | " + responseMessage + " At " + timestamp + " | " + exception);

        return ResponseEntity.status(status).body(
                DefaultExceptionResponseDto.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build()
        );
    }

    /**
     * 406 Not Acceptable |
     * 서버 주도 콘텐츠 협상을 수행한 후, 사용자 에이전트에서 정해준 규격에 따른 어떠한 콘텐츠도 찾지 않습니다.
     */
    @ExceptionHandler(NotAcceptableException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> handleNotAcceptableException(NotAcceptableException exception) {
        String responseMessage = exception.getMessage();
        String responseCode = "NOT_ACCEPTABLE";
        LocalDateTime timestamp = LocalDateTime.now();

        log.error("ERROR | " + responseMessage + " At " + timestamp + " | "
                + exception.getMessage() + " = " + exception.getCause());

        return ResponseEntity.status(406).body(
                DefaultExceptionResponseDto.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build()
        );
    }

    /**
     * 500 Internal Server Error |
     * 서버에 문제가 있음을 의미하지만 서버는 정확한 무제에 대해 더 구체적으로 설명할 수 없습니다.
     */
    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleInternalServerException(InternalServerErrorException exception) {
        String responseMessage = "서버 에러가 발생했습니다";
        String responseCode = "INTERNAL_SERVER_ERROR";
        LocalDateTime timestamp = LocalDateTime.now();

        log.error("ERROR | " + responseMessage + " At " + timestamp + " | "
                + exception.getMessage() + " = " + exception.getCause());

        return ResponseEntity.status(500).body(
                DefaultExceptionResponseDto.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build()
        );
    }
}
