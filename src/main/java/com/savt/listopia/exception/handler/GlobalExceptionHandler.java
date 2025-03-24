package com.savt.listopia.exception.handler;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.ResourceAlreadyExistException;
import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.payload.APIResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import info.movito.themoviedbapi.tools.TmdbException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.TransientObjectException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        }));
        return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<APIResponse> myResourceAlreadyExistException(
            ResourceAlreadyExistException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> myConstraintViolationException(ConstraintViolationException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TmdbException.class)
    public ResponseEntity<APIResponse> myTmdbException(TmdbException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<APIResponse> myIoException(IOException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

}
