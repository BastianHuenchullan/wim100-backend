package cl.duoc.wim100;

import cl.duoc.wim100.dto.ErrorResponse;
import cl.duoc.wim100.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private void comprobar(ResponseEntity<ErrorResponse> respuesta, int status, String mensaje) {
        assertEquals(status, respuesta.getStatusCode().value());
        ErrorResponse cuerpo = respuesta.getBody();
        assertNotNull(cuerpo);
        assertEquals(status, cuerpo.status());
        assertEquals(mensaje, cuerpo.mensaje());
        assertTrue(cuerpo.errores().isEmpty());
    }

    @Test
    void responderRegistroInexistente() {
        comprobar(handler.noEncontrado(new RecursoNoEncontradoException("No existe")), 404, "No existe");
    }

    @Test
    void responderCorreoDuplicado() {
        comprobar(handler.duplicado(new CorreoDuplicadoException("Correo repetido")), 409, "Correo repetido");
    }

    @Test
    void responderCredencialesIncorrectas() {
        comprobar(handler.credenciales(new CredencialesInvalidasException("Acceso rechazado")), 401, "Acceso rechazado");
    }

    @Test
    void responderErroresPorCampo() {
        var errores = new BeanPropertyBindingResult(new Object(), "solicitud");
        errores.addError(new FieldError("solicitud", "nombre", "El nombre es obligatorio"));
        errores.addError(new FieldError("solicitud", "correo", "Correo invalido"));
        var error = new MethodArgumentNotValidException(mock(MethodParameter.class), errores);
        var respuesta = handler.validacion(error);
        assertEquals(400, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals(400, respuesta.getBody().status());
        assertFalse(respuesta.getBody().mensaje().isBlank());
        assertEquals(2, respuesta.getBody().errores().size());
        assertEquals("El nombre es obligatorio", respuesta.getBody().errores().get("nombre"));
        assertEquals("Correo invalido", respuesta.getBody().errores().get("correo"));
    }

    @Test
    void responderValidacionSinDetalle() {
        var errores = new BeanPropertyBindingResult(new Object(), "solicitud");
        var error = new MethodArgumentNotValidException(mock(MethodParameter.class), errores);
        var respuesta = handler.validacion(error);
        assertEquals(400, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().errores().isEmpty());
    }
}
