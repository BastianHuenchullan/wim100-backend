package cl.duoc.wim100;

import cl.duoc.wim100.controller.EvaluacionController;
import cl.duoc.wim100.model.Evaluacion;
import cl.duoc.wim100.model.PuntajeCriterio;
import cl.duoc.wim100.repository.EvaluacionRepository;
import cl.duoc.wim100.repository.PuntajeCriterioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EvaluacionControllerTest {
    private EvaluacionRepository evaluaciones;
    private PuntajeCriterioRepository puntajes;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        evaluaciones = mock(EvaluacionRepository.class);
        puntajes = mock(PuntajeCriterioRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new EvaluacionController(evaluaciones, puntajes)).build();
    }

    private Evaluacion evaluacion() {
        Evaluacion e = new Evaluacion();
        e.setId(5L);
        e.setPostulanteId(1L);
        e.setEvaluadora("Ana Evaluadora");
        e.setEvaluadoraClave("ana evaluadora");
        e.setPuntajeTotal(80.0);
        e.setObservacionGeneral("Cumple los criterios de la evaluacion.");
        e.setFecha(1700000000000L);
        return e;
    }

    private PuntajeCriterio puntaje(long criterioId, int nota) {
        PuntajeCriterio p = new PuntajeCriterio();
        p.setId(7L);
        p.setEvaluacionId(5L);
        p.setCriterioId(criterioId);
        p.setPuntaje(nota);
        p.setJustificacion("Antecedentes suficientes.");
        return p;
    }

    private String solicitud(String fecha, String lista) {
        return """
                {"postulanteId":1,"evaluadora":"Ana Evaluadora","evaluadoraClave":"ana evaluadora",
                 "puntajeTotal":80.0,"observacionGeneral":"Cumple los criterios de la evaluacion.",
                 "fecha":%s,"puntajes":%s}
                """.formatted(fecha, lista);
    }

    private void prepararGuardado() {
        when(evaluaciones.findByPostulanteIdAndEvaluadoraClave(1L, "ana evaluadora"))
                .thenReturn(Optional.empty());
        when(evaluaciones.save(any(Evaluacion.class))).thenAnswer(i -> {
            Evaluacion e = i.getArgument(0);
            e.setId(5L);
            return e;
        });
        when(puntajes.findByEvaluacionId(5L)).thenReturn(List.of());
    }

    @Test
    void listarEvaluacionesConPuntajes() throws Exception {
        when(evaluaciones.findAll()).thenReturn(List.of(evaluacion()));
        when(puntajes.findByEvaluacionId(5L)).thenReturn(List.of(puntaje(1L, 4), puntaje(2L, 5)));
        mvc.perform(get("/api/evaluaciones")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].postulanteId").value(1))
                .andExpect(jsonPath("$[0].evaluadora").value("Ana Evaluadora"))
                .andExpect(jsonPath("$[0].evaluadoraClave").value("ana evaluadora"))
                .andExpect(jsonPath("$[0].puntajeTotal").value(80.0))
                .andExpect(jsonPath("$[0].observacionGeneral").value("Cumple los criterios de la evaluacion."))
                .andExpect(jsonPath("$[0].fecha").value(1700000000000L))
                .andExpect(jsonPath("$[0].puntajes.length()").value(2))
                .andExpect(jsonPath("$[0].puntajes[0].criterioId").value(1))
                .andExpect(jsonPath("$[0].puntajes[0].puntaje").value(4))
                .andExpect(jsonPath("$[0].puntajes[0].justificacion").value("Antecedentes suficientes."));
    }

    @Test
    void listarEvaluacionesVacias() throws Exception {
        when(evaluaciones.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/evaluaciones")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verifyNoInteractions(puntajes);
    }

    @Test
    void listarEvaluacionSinPuntajes() throws Exception {
        when(evaluaciones.findAll()).thenReturn(List.of(evaluacion()));
        when(puntajes.findByEvaluacionId(5L)).thenReturn(List.of());
        mvc.perform(get("/api/evaluaciones")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].puntajes").isEmpty());
    }

    @Test
    void crearEvaluacion() throws Exception {
        prepararGuardado();
        String lista = "[{\"criterioId\":1,\"puntaje\":4,\"justificacion\":\"Experiencia suficiente\"}]";
        mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("1700000000000", lista)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(5));
        ArgumentCaptor<Evaluacion> e = ArgumentCaptor.forClass(Evaluacion.class);
        verify(evaluaciones).save(e.capture());
        assertAll(() -> assertEquals(1L, e.getValue().getPostulanteId()),
                () -> assertEquals("Ana Evaluadora", e.getValue().getEvaluadora()),
                () -> assertEquals("ana evaluadora", e.getValue().getEvaluadoraClave()),
                () -> assertEquals(80.0, e.getValue().getPuntajeTotal()),
                () -> assertEquals("Cumple los criterios de la evaluacion.", e.getValue().getObservacionGeneral()),
                () -> assertEquals(1700000000000L, e.getValue().getFecha()));
        ArgumentCaptor<PuntajeCriterio> p = ArgumentCaptor.forClass(PuntajeCriterio.class);
        verify(puntajes).save(p.capture());
        assertAll(() -> assertEquals(5L, p.getValue().getEvaluacionId()),
                () -> assertEquals(1L, p.getValue().getCriterioId()),
                () -> assertEquals(4, p.getValue().getPuntaje()),
                () -> assertEquals("Experiencia suficiente", p.getValue().getJustificacion()));
    }

    @Test
    void actualizarEvaluacionSinDuplicar() throws Exception {
        Evaluacion anterior = evaluacion();
        when(evaluaciones.findByPostulanteIdAndEvaluadoraClave(1L, "ana evaluadora"))
                .thenReturn(Optional.of(anterior));
        when(evaluaciones.save(anterior)).thenReturn(anterior);
        List<PuntajeCriterio> anteriores = List.of(puntaje(1L, 2));
        when(puntajes.findByEvaluacionId(5L)).thenReturn(anteriores);
        mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("1800000000000", "[{\"criterioId\":1,\"puntaje\":5,\"justificacion\":\"Mejora comprobada\"}]")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(5));
        verify(evaluaciones).save(same(anterior));
        assertEquals(1800000000000L, anterior.getFecha());
        var orden = inOrder(puntajes);
        orden.verify(puntajes).findByEvaluacionId(5L);
        orden.verify(puntajes).deleteAll(anteriores);
        orden.verify(puntajes).save(argThat(p -> p.getPuntaje() == 5 && p.getEvaluacionId() == 5L));
    }

    @Test
    void asignarFechaAutomatica() throws Exception {
        prepararGuardado();
        long antes = System.currentTimeMillis();
        mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
                .content(solicitud("null", "[]"))).andExpect(status().isOk());
        ArgumentCaptor<Evaluacion> e = ArgumentCaptor.forClass(Evaluacion.class);
        verify(evaluaciones).save(e.capture());
        assertTrue(e.getValue().getFecha() >= antes);
        assertTrue(e.getValue().getFecha() <= System.currentTimeMillis());
    }

    @Test
    void guardarSinPuntajes() throws Exception {
        prepararGuardado();
        mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
                .content(solicitud("1700000000000", "null"))).andExpect(status().isOk());
        verify(puntajes, never()).save(any());
        verify(puntajes).deleteAll(List.of());
    }

    @Test
    void guardarListaVacia() throws Exception {
        prepararGuardado();
        mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
                .content(solicitud("1700000000000", "[]"))).andExpect(status().isOk());
        verify(puntajes, never()).save(any());
    }
}
