package com.openclassrooms.mddapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResponseStatusException_uses_reason_or_default_message() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiErrorResponse> withReason = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Not here"),
                req);
        Assertions.assertThat(withReason.getStatusCode().value()).isEqualTo(404);
        Assertions.assertThat(withReason.getBody()).isNotNull();
        Assertions.assertThat(withReason.getBody().message()).isEqualTo("Not here");

        ResponseEntity<ApiErrorResponse> withoutReason = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, (String) null),
                req);
        Assertions.assertThat(withoutReason.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(withoutReason.getBody()).isNotNull();
        Assertions.assertThat(withoutReason.getBody().message()).isEqualTo("Request failed");
    }

    @Test
    void handleIllegalArgument_uses_message_or_default() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiErrorResponse> withMsg = handler.handleIllegalArgument(new IllegalArgumentException("bad"),
                req);
        Assertions.assertThat(withMsg.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(withMsg.getBody()).isNotNull();
        Assertions.assertThat(withMsg.getBody().message()).isEqualTo("bad");

        ResponseEntity<ApiErrorResponse> blankMsg = handler.handleIllegalArgument(new IllegalArgumentException(" "),
                req);
        Assertions.assertThat(blankMsg.getBody()).isNotNull();
        Assertions.assertThat(blankMsg.getBody().message()).isEqualTo("Invalid request");
    }

    @Test
    void handleTypeMismatch_returns_400() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        MethodArgumentTypeMismatchException ex = Mockito.mock(MethodArgumentTypeMismatchException.class);
        Mockito.when(ex.getName()).thenReturn("id");

        ResponseEntity<ApiErrorResponse> response = handler.handleTypeMismatch(ex, req);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).contains("id");
    }

    @Test
    void handleNotReadable_returns_400() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        ResponseEntity<ApiErrorResponse> response = handler.handleNotReadable(ex, req);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Malformed JSON request");
    }

    @Test
    void handleAccessDenied_returns_403() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("x"), req);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(403);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Forbidden");
    }

    @Test
    void handleDataIntegrityViolation_returns_409() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        DataIntegrityViolationException ex = new DataIntegrityViolationException("x", new RuntimeException("cause"));
        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrityViolation(ex, req);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(409);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Conflict");
    }

    @Test
    void handleMethodArgumentNotValid_returns_field_errors_map() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "email", "invalid"));

        Method m = Dummy.class.getDeclaredMethod("dummy", String.class);
        MethodParameter param = new MethodParameter(m, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentNotValid(ex, req);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().fieldErrors()).containsEntry("email", "invalid");
    }

    @Test
    void handleUnhandled_returns_500() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnhandled(new RuntimeException("boom"), req);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }

    private static class Dummy {
        /**
         * Méthode factice destinée à être utilisée comme référence pour éviter les
         * warnings
         * de "unused" lors de l'utilisation de MethodHandles.lookup() ou pour
         * satisfaire
         * des contraintes de compilation/réflexion.
         *
         * @param email Paramètre non utilisé (présent pour correspondre à une signature
         *              attendue)
         * @throws UnsupportedOperationException si cette méthode est appelée (ne
         *                                       devrait jamais arriver en usage normal)
         */
        @SuppressWarnings("unused")
        static void dummy(String email) {
            throw new UnsupportedOperationException(
                    "Cette méthode est une implémentation factice et ne doit pas être appelée");
        }
    }
}
