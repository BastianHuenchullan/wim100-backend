package cl.duoc.wim100;

import cl.duoc.wim100.config.DataLoader;
import cl.duoc.wim100.config.PasswordConfig;
import cl.duoc.wim100.model.EstadoPostulante;
import cl.duoc.wim100.model.Postulante;
import cl.duoc.wim100.model.Rol;
import cl.duoc.wim100.model.Usuario;
import cl.duoc.wim100.repository.PostulanteRepository;
import cl.duoc.wim100.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataLoaderTest {
    private UsuarioRepository usuarios;
    private PostulanteRepository postulantes;
    private PasswordEncoder encoder;
    private DataLoader loader;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        postulantes = mock(PostulanteRepository.class);
        encoder = new PasswordConfig().passwordEncoder();
        loader = new DataLoader(usuarios, postulantes, encoder);
    }

    @Test
    void cargarDatosIniciales() {
        loader.run();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Usuario>> usuariosGuardados = ArgumentCaptor.forClass(Iterable.class);
        verify(usuarios).saveAll(usuariosGuardados.capture());
        List<Usuario> lista = new ArrayList<>();
        usuariosGuardados.getValue().forEach(lista::add);
        assertEquals(3, lista.size());
        assertEquals(List.of("evaluadora1", "evaluadora2", "direccion"),
                lista.stream().map(Usuario::getUsuario).toList());
        assertEquals(List.of(Rol.EVALUADORA, Rol.EVALUADORA, Rol.DIRECCION),
                lista.stream().map(Usuario::getRol).toList());
        for (Usuario u : lista) {
            assertFalse(u.getNombre().isBlank());
            assertNotEquals("wim2026", u.getPassword());
            assertTrue(encoder.matches("wim2026", u.getPassword()));
        }

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Postulante>> datos = ArgumentCaptor.forClass(Iterable.class);
        verify(postulantes).saveAll(datos.capture());
        List<Postulante> candidatas = new ArrayList<>();
        datos.getValue().forEach(candidatas::add);
        assertEquals(3, candidatas.size());
        assertEquals(3, candidatas.stream().map(Postulante::getCorreo).distinct().count());
        for (Postulante p : candidatas) {
            assertAll(() -> assertFalse(p.getNombre().isBlank()),
                    () -> assertTrue(p.getCorreo().contains("@")),
                    () -> assertFalse(p.getTelefono().isBlank()),
                    () -> assertFalse(p.getArea().isBlank()),
                    () -> assertTrue(p.getAniosExperiencia() >= 0),
                    () -> assertTrue(p.getResumenExperiencia().length() >= 30),
                    () -> assertTrue(p.getLinkedinUrl().startsWith("https://")),
                    () -> assertTrue(p.getCvNombre().endsWith(".pdf")),
                    () -> assertEquals(EstadoPostulante.POSTULADA, p.getEstado()),
                    () -> assertNotNull(p.getFechaRegistro()),
                    () -> assertEquals("Carga inicial", p.getRegistradaPor()));
        }
    }

    @Test
    void noDuplicarDatosExistentes() {
        when(usuarios.count()).thenReturn(3L);
        when(postulantes.count()).thenReturn(3L);
        loader.run();
        verify(usuarios, never()).saveAll(any());
        verify(postulantes, never()).saveAll(any());
    }

    @Test
    void cargarSoloUsuariosFaltantes() {
        when(postulantes.count()).thenReturn(3L);
        loader.run();
        verify(usuarios).saveAll(any());
        verify(postulantes, never()).saveAll(any());
    }

    @Test
    void cargarSoloPostulantesFaltantes() {
        when(usuarios.count()).thenReturn(3L);
        loader.run();
        verify(usuarios, never()).saveAll(any());
        verify(postulantes).saveAll(any());
    }
}
