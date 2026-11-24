package cl.duoc.wim100;

import cl.duoc.wim100.config.PasswordConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {
    private final PasswordEncoder encoder = new PasswordConfig().passwordEncoder();

    @Test
    void protegerPassword() {
        String hash = encoder.encode("clave-prueba");
        assertNotEquals("clave-prueba", hash);
        assertTrue(encoder.matches("clave-prueba", hash));
    }

    @Test
    void rechazarPasswordIncorrecta() {
        String hash = encoder.encode("clave-prueba");
        assertFalse(encoder.matches("otra-clave", hash));
    }

    @Test
    void generarHashesDistintos() {
        String primero = encoder.encode("clave-prueba");
        String segundo = encoder.encode("clave-prueba");
        assertNotEquals(primero, segundo);
        assertTrue(encoder.matches("clave-prueba", primero));
        assertTrue(encoder.matches("clave-prueba", segundo));
    }
}
