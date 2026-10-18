package org.ahilmi.schoolmanagement.controller;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail status(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(),
                exception.getReason() == null ? "İşlem tamamlanamadı." : exception.getReason());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                exception.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; ")));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail uploadSize(MaxUploadSizeExceededException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "Fotoğraf en fazla 5 MB olabilir.");
    }
}
