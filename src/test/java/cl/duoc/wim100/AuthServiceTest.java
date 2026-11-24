package cl.duoc.wim100;

import cl.duoc.wim100.dto.LoginRequest;
import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.exception.CredencialesInvalidasException;
import cl.duoc.wim100.model.Rol;
import cl.duoc.wim100.model.Usuario;
import cl.duoc.wim100.repository.UsuarioRepository;
import cl.duoc.wim100.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UsuarioRepository usuarios;
    private PasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new AuthService(usuarios, encoder);
    }

    @Test
    void loginCorrecto() {
        Usuario u = DatosPrueba.usuario();
        when(usuarios.findByUsuario("evaluadora1")).thenReturn(Optional.of(u));
        when(encoder.matches("clave", "hash-prueba")).thenReturn(true);
        LoginResponse respuesta = service.login(new LoginRequest("evaluadora1", "clave"));
        assertAll(() -> assertEquals(u.getId(), respuesta.id()),
                () -> assertEquals(u.getUsuario(), respuesta.usuario()),
                () -> assertEquals(u.getNombre(), respuesta.nombre()),
                () -> assertEquals(Rol.EVALUADORA, respuesta.rol()));
        verify(encoder).matches("clave", "hash-prueba");
    }

    @Test
    void limpiarNombreDeUsuario() {
        when(usuarios.findByUsuario("evaluadora1")).thenReturn(Optional.of(DatosPrueba.usuario()));
        when(encoder.matches("clave", "hash-prueba")).thenReturn(true);
        service.login(new LoginRequest("  EVALUADORA1  ", "clave"));
        verify(usuarios).findByUsuario("evaluadora1");
    }

    @Test
    void rechazarPasswordIncorrecta() {
        when(usuarios.findByUsuario("evaluadora1")).thenReturn(Optional.of(DatosPrueba.usuario()));
        when(encoder.matches("incorrecta", "hash-prueba")).thenReturn(false);
        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("evaluadora1", "incorrecta")));
        assertFalse(error.getMessage().isBlank());
        verify(usuarios, never()).save(any());
    }

    @Test
    void rechazarUsuarioInexistente() {
        when(usuarios.findByUsuario("nadie")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie", "clave")));
        verifyNoInteractions(encoder);
    }

    @Test
    void devolverRolDireccion() {
        Usuario u = new Usuario("direccion", "hash", "Direccion Demo", Rol.DIRECCION);
        u.setId(3L);
        when(usuarios.findByUsuario("direccion")).thenReturn(Optional.of(u));
        when(encoder.matches("clave", "hash")).thenReturn(true);
        assertEquals(Rol.DIRECCION, service.login(new LoginRequest("direccion", "clave")).rol());
    }
}
