package cl.duoc.wim100;

import cl.duoc.wim100.controller.PostulanteController;
import cl.duoc.wim100.exception.CorreoDuplicadoException;
import cl.duoc.wim100.exception.GlobalExceptionHandler;
import cl.duoc.wim100.exception.RecursoNoEncontradoException;
import cl.duoc.wim100.model.EstadoPostulante;
import cl.duoc.wim100.model.Postulante;
import cl.duoc.wim100.service.PostulanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostulanteControllerTest {
    private PostulanteService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(PostulanteService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new PostulanteController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void listarPostulantes() throws Exception {
        when(service.listar()).thenReturn(List.of(DatosPrueba.postulante()));
        mvc.perform(get("/api/postulantes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].correo").value("ana@example.com"))
                .andExpect(jsonPath("$[0].estado").value("POSTULADA"));
    }

    @Test
    void listarSinPostulantes() throws Exception {
        when(service.listar()).thenReturn(List.of());
        mvc.perform(get("/api/postulantes")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void obtenerPostulante() throws Exception {
        when(service.obtener(1L)).thenReturn(DatosPrueba.postulante());
        mvc.perform(get("/api/postulantes/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Perez"))
                .andExpect(jsonPath("$.telefono").value("912345678"))
                .andExpect(jsonPath("$.area").value("Blue Team"))
                .andExpect(jsonPath("$.aniosExperiencia").value(3))
                .andExpect(jsonPath("$.cvNombre").value("cv.pdf"));
    }

    @Test
    void informarPostulanteInexistente() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));
        mvc.perform(get("/api/postulantes/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void crearPostulante() throws Exception {
        when(service.crear(DatosPrueba.solicitud())).thenReturn(DatosPrueba.postulante());
        mvc.perform(post("/api/postulantes").contentType(MediaType.APPLICATION_JSON)
                        .content(DatosPrueba.jsonPostulante()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        verify(service).crear(DatosPrueba.solicitud());
    }

    @Test
    void rechazarCorreoDuplicado() throws Exception {
        when(service.crear(any())).thenThrow(new CorreoDuplicadoException("Correo repetido"));
        mvc.perform(post("/api/postulantes").contentType(MediaType.APPLICATION_JSON)
                        .content(DatosPrueba.jsonPostulante()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value("Correo repetido"));
    }

    @Test
    void rechazarDatosInvalidos() throws Exception {
        mvc.perform(post("/api/postulantes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.aniosExperiencia").exists());
        verifyNoInteractions(service);
    }

    @Test
    void actualizarPostulante() throws Exception {
        when(service.actualizar(1L, DatosPrueba.solicitud())).thenReturn(DatosPrueba.postulante());
        mvc.perform(put("/api/postulantes/1").contentType(MediaType.APPLICATION_JSON)
                        .content(DatosPrueba.jsonPostulante()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ana Perez"));
        verify(service).actualizar(1L, DatosPrueba.solicitud());
    }

    @Test
    void rechazarEdicionDuplicada() throws Exception {
        when(service.actualizar(eq(1L), any())).thenThrow(new CorreoDuplicadoException("Correo repetido"));
        mvc.perform(put("/api/postulantes/1").contentType(MediaType.APPLICATION_JSON)
                        .content(DatosPrueba.jsonPostulante())).andExpect(status().isConflict());
    }

    @Test
    void rechazarEdicionInexistente() throws Exception {
        when(service.actualizar(eq(99L), any())).thenThrow(new RecursoNoEncontradoException("No existe"));
        mvc.perform(put("/api/postulantes/99").contentType(MediaType.APPLICATION_JSON)
                        .content(DatosPrueba.jsonPostulante())).andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstado() throws Exception {
        Postulante p = DatosPrueba.postulante();
        p.setEstado(EstadoPostulante.SELECCIONADA);
        when(service.cambiarEstado(1L, EstadoPostulante.SELECCIONADA)).thenReturn(p);
        mvc.perform(patch("/api/postulantes/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"SELECCIONADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("SELECCIONADA"));
        verify(service).cambiarEstado(1L, EstadoPostulante.SELECCIONADA);
    }

    @Test
    void rechazarEstadoAusente() throws Exception {
        mvc.perform(patch("/api/postulantes/1/estado").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.estado").exists());
        verifyNoInteractions(service);
    }

    @Test
    void rechazarEstadoDesconocido() throws Exception {
        mvc.perform(patch("/api/postulantes/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INVENTADO\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void eliminarPostulante() throws Exception {
        mvc.perform(delete("/api/postulantes/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).eliminar(1L);
    }

    @Test
    void rechazarEliminacionInexistente() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe")).when(service).eliminar(99L);
        mvc.perform(delete("/api/postulantes/99")).andExpect(status().isNotFound());
    }
}
