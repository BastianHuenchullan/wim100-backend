package cl.duoc.wim100;

import cl.duoc.wim100.dto.PostulanteRequest;
import cl.duoc.wim100.exception.CorreoDuplicadoException;
import cl.duoc.wim100.exception.RecursoNoEncontradoException;
import cl.duoc.wim100.model.EstadoPostulante;
import cl.duoc.wim100.model.Postulante;
import cl.duoc.wim100.repository.PostulanteRepository;
import cl.duoc.wim100.service.PostulanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostulanteServiceTest {
    private PostulanteRepository repository;
    private PostulanteService service;

    @BeforeEach
    void preparar() {
        repository = mock(PostulanteRepository.class);
        service = new PostulanteService(repository);
    }

    @Test
    void listarPostulantes() {
        List<Postulante> lista = List.of(DatosPrueba.postulante());
        when(repository.findAllByOrderByFechaRegistroDesc()).thenReturn(lista);
        assertSame(lista, service.listar());
    }

    @Test
    void listarSinPostulantes() {
        when(repository.findAllByOrderByFechaRegistroDesc()).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void obtenerPostulante() {
        Postulante p = DatosPrueba.postulante();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        assertSame(p, service.obtener(1L));
    }

    @Test
    void rechazarPostulanteInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void crearPostulante() {
        when(repository.save(any(Postulante.class))).thenAnswer(i -> {
            Postulante p = i.getArgument(0);
            p.setId(10L);
            return p;
        });
        long antes = System.currentTimeMillis();
        Postulante p = service.crear(DatosPrueba.solicitud());
        assertAll(() -> assertEquals(10L, p.getId()),
                () -> assertEquals(EstadoPostulante.POSTULADA, p.getEstado()),
                () -> assertEquals("Evaluadora Demo", p.getRegistradaPor()),
                () -> assertTrue(p.getFechaRegistro() >= antes),
                () -> assertTrue(p.getFechaRegistro() <= System.currentTimeMillis()));
        verify(repository).save(p);
    }

    @Test
    void limpiarDatosAlCrear() {
        PostulanteRequest r = new PostulanteRequest("  Ana   Perez  ", " ANA@EXAMPLE.COM ",
                " 912345678 ", " Blue Team ", 3, " Experiencia ficticia suficientemente extensa para probar. ",
                " https://www.linkedin.com/in/ana-demo ", "cv.pdf", " Evaluadora Demo ");
        when(repository.save(any(Postulante.class))).thenAnswer(i -> i.getArgument(0));
        Postulante p = service.crear(r);
        assertAll(() -> assertEquals("Ana Perez", p.getNombre()),
                () -> assertEquals("ana@example.com", p.getCorreo()),
                () -> assertEquals("912345678", p.getTelefono()),
                () -> assertEquals("Blue Team", p.getArea()),
                () -> assertEquals(3, p.getAniosExperiencia()),
                () -> assertEquals(r.resumenExperiencia().trim(), p.getResumenExperiencia()),
                () -> assertEquals(r.linkedinUrl().trim(), p.getLinkedinUrl()),
                () -> assertEquals("cv.pdf", p.getCvNombre()),
                () -> assertEquals("Evaluadora Demo", p.getRegistradaPor()));
        verify(repository).existsByCorreoIgnoreCase("ANA@EXAMPLE.COM");
    }

    @Test
    void rechazarCorreoDuplicadoAlCrear() {
        when(repository.existsByCorreoIgnoreCase("ana@example.com")).thenReturn(true);
        assertThrows(CorreoDuplicadoException.class, () -> service.crear(DatosPrueba.solicitud()));
        verify(repository, never()).save(any());
    }

    @Test
    void crearSinNombreDeCv() {
        PostulanteRequest r = DatosPrueba.solicitud();
        when(repository.save(any(Postulante.class))).thenAnswer(i -> i.getArgument(0));
        Postulante p = service.crear(new PostulanteRequest(r.nombre(), r.correo(), r.telefono(), r.area(),
                r.aniosExperiencia(), r.resumenExperiencia(), r.linkedinUrl(), null, r.registradaPor()));
        assertNull(p.getCvNombre());
    }

    @Test
    void actualizarPostulante() {
        Postulante p = DatosPrueba.postulante();
        p.setNombre("Nombre anterior");
        p.setEstado(EstadoPostulante.EVALUADA);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(p)).thenReturn(p);
        Postulante actualizado = service.actualizar(1L, DatosPrueba.solicitud());
        assertAll(() -> assertEquals("Ana Perez", actualizado.getNombre()),
                () -> assertEquals(1L, actualizado.getId()),
                () -> assertEquals(EstadoPostulante.EVALUADA, actualizado.getEstado()),
                () -> assertEquals(1700000000000L, actualizado.getFechaRegistro()),
                () -> assertEquals("Evaluadora Demo", actualizado.getRegistradaPor()));
        verify(repository).existsByCorreoIgnoreCaseAndIdNot("ana@example.com", 1L);
    }

    @Test
    void rechazarCorreoDuplicadoAlEditar() {
        Postulante p = DatosPrueba.postulante();
        p.setNombre("Nombre anterior");
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.existsByCorreoIgnoreCaseAndIdNot("ana@example.com", 1L)).thenReturn(true);
        assertThrows(CorreoDuplicadoException.class, () -> service.actualizar(1L, DatosPrueba.solicitud()));
        assertEquals("Nombre anterior", p.getNombre());
        verify(repository, never()).save(any());
    }

    @Test
    void rechazarEdicionInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(99L, DatosPrueba.solicitud()));
        verify(repository, never()).save(any());
    }

    @Test
    void cambiarEstado() {
        Postulante p = DatosPrueba.postulante();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(p)).thenReturn(p);
        assertEquals(EstadoPostulante.SELECCIONADA,
                service.cambiarEstado(1L, EstadoPostulante.SELECCIONADA).getEstado());
        verify(repository).save(p);
    }

    @Test
    void rechazarCambioDeEstadoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.cambiarEstado(99L, EstadoPostulante.EVALUADA));
        verify(repository, never()).save(any());
    }

    @Test
    void eliminarPostulante() {
        Postulante p = DatosPrueba.postulante();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        service.eliminar(1L);
        verify(repository).delete(p);
    }

    @Test
    void rechazarEliminacionInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
        verify(repository, never()).delete(any());
    }
}
