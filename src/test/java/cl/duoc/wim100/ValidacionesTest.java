package cl.duoc.wim100;

import cl.duoc.wim100.dto.EstadoRequest;
import cl.duoc.wim100.dto.LoginRequest;
import cl.duoc.wim100.dto.PostulanteRequest;
import cl.duoc.wim100.model.EstadoPostulante;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionesTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void preparar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    private Set<String> errores(Object solicitud) {
        return validator.validate(solicitud).stream()
                .map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private PostulanteRequest cambiar(String campo, Object valor) {
        PostulanteRequest r = DatosPrueba.solicitud();
        return new PostulanteRequest(
                campo.equals("nombre") ? (String) valor : r.nombre(),
                campo.equals("correo") ? (String) valor : r.correo(),
                campo.equals("telefono") ? (String) valor : r.telefono(),
                campo.equals("area") ? (String) valor : r.area(),
                campo.equals("aniosExperiencia") ? (Integer) valor : r.aniosExperiencia(),
                campo.equals("resumenExperiencia") ? (String) valor : r.resumenExperiencia(),
                campo.equals("linkedinUrl") ? (String) valor : r.linkedinUrl(),
                campo.equals("cvNombre") ? (String) valor : r.cvNombre(),
                campo.equals("registradaPor") ? (String) valor : r.registradaPor());
    }

    @Test
    void aceptarPostulanteValida() {
        assertTrue(errores(DatosPrueba.solicitud()).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nombre", "correo", "telefono", "area", "resumenExperiencia", "linkedinUrl", "registradaPor"})
    void rechazarCamposObligatorios(String campo) {
        assertTrue(errores(cambiar(campo, "")).contains(campo));
        assertTrue(errores(cambiar(campo, "   ")).contains(campo));
        assertTrue(errores(cambiar(campo, null)).contains(campo));
    }

    @Test
    void validarLargoDelNombre() {
        assertTrue(errores(cambiar("nombre", "AB")).contains("nombre"));
        assertTrue(errores(cambiar("nombre", "A".repeat(61))).contains("nombre"));
        assertTrue(errores(cambiar("nombre", "Ana")).isEmpty());
        assertTrue(errores(cambiar("nombre", "A".repeat(60))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba", "ana@", "@example.com"})
    void rechazarCorreoInvalido(String correo) {
        assertTrue(errores(cambiar("correo", correo)).contains("correo"));
    }

    @Test
    void validarLargoDelTelefono() {
        assertTrue(errores(cambiar("telefono", "1".repeat(21))).contains("telefono"));
        assertTrue(errores(cambiar("telefono", "1".repeat(20))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 51})
    void rechazarExperienciaFueraDeRango(int anios) {
        assertTrue(errores(cambiar("aniosExperiencia", anios)).contains("aniosExperiencia"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 50})
    void aceptarLimitesDeExperiencia(int anios) {
        assertTrue(errores(cambiar("aniosExperiencia", anios)).isEmpty());
    }

    @Test
    void exigirExperiencia() {
        assertTrue(errores(cambiar("aniosExperiencia", null)).contains("aniosExperiencia"));
    }

    @Test
    void validarLargoDelResumen() {
        assertTrue(errores(cambiar("resumenExperiencia", "A".repeat(29))).contains("resumenExperiencia"));
        assertTrue(errores(cambiar("resumenExperiencia", "A".repeat(501))).contains("resumenExperiencia"));
        assertTrue(errores(cambiar("resumenExperiencia", "A".repeat(30))).isEmpty());
        assertTrue(errores(cambiar("resumenExperiencia", "A".repeat(500))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/perfil", "example.com/perfil", "ftp://example.com"})
    void exigirEnlaceHttps(String enlace) {
        assertTrue(errores(cambiar("linkedinUrl", enlace)).contains("linkedinUrl"));
    }

    @Test
    void aceptarCvOpcional() {
        assertTrue(errores(cambiar("cvNombre", null)).isEmpty());
    }

    @Test
    void aceptarLoginCompleto() {
        assertTrue(errores(new LoginRequest("evaluadora1", "clave")).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void exigirDatosDeLogin(String valor) {
        assertTrue(errores(new LoginRequest(valor, "clave")).contains("usuario"));
        assertTrue(errores(new LoginRequest("evaluadora1", valor)).contains("password"));
    }

    @Test
    void exigirEstado() {
        assertEquals(Set.of("estado"), errores(new EstadoRequest(null)));
    }

    @Test
    void aceptarEstadosDefinidos() {
        for (EstadoPostulante estado : EstadoPostulante.values()) {
            assertTrue(errores(new EstadoRequest(estado)).isEmpty());
        }
    }
}
