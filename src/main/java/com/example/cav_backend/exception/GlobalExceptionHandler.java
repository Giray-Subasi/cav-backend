package com.example.cav_backend.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ProfileNotFoundException.class)
        public ResponseEntity<ApiError> handleProfileNotFound(
                        ProfileNotFoundException e) {

                ApiError error = new ApiError(
                                404,
                                "PROFILE_NOT_FOUND",
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<ApiError> handleInvalidRequest(
                        InvalidRequestException e) {

                ApiError error = new ApiError(
                                400,
                                "BAD_REQUEST",
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(
                        IllegalArgumentException e) {

                ApiError error = new ApiError(
                                409,
                                "CONFLICT",
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(
                        IllegalStateException e) {

                ApiError error = new ApiError(
                                409,
                                "INVALID_PROFILE_STATE",
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(
                        MethodArgumentNotValidException e) {

                String message = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(
                                                error -> error.getField()
                                                                + ": "
                                                                + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));

                ApiError error = new ApiError(
                                400,
                                "VALIDATION_ERROR",
                                message);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatch(
                        MethodArgumentTypeMismatchException e) {

                String message = "Invalid value '"
                                + e.getValue()
                                + "' for parameter '"
                                + e.getName()
                                + "'";

                ApiError error = new ApiError(
                                400,
                                "BAD_REQUEST",
                                message);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleMessageNotReadable(
                        HttpMessageNotReadableException e) {

                String message = "Invalid request body";

                InvalidFormatException invalidFormatException = findInvalidFormatException(e);

                if (invalidFormatException != null) {

                        String fieldName = "unknown";

                        if (!invalidFormatException.getPath().isEmpty()) {

                                String detectedField = invalidFormatException
                                                .getPath()
                                                .get(
                                                                invalidFormatException
                                                                                .getPath()
                                                                                .size() - 1)
                                                .getPropertyName();

                                if (detectedField != null) {
                                        fieldName = detectedField;
                                }
                        }

                        message = "Invalid value '"
                                        + invalidFormatException.getValue()
                                        + "' for field '"
                                        + fieldName
                                        + "'";
                }

                ApiError error = new ApiError(
                                400,
                                "BAD_REQUEST",
                                message);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        private InvalidFormatException findInvalidFormatException(
                        Throwable throwable) {

                Throwable current = throwable;

                while (current != null) {

                        if (current instanceof InvalidFormatException invalidFormatException) {
                                return invalidFormatException;
                        }

                        current = current.getCause();
                }

                return null;
        }
}