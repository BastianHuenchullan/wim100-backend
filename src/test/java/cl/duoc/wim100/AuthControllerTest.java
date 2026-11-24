package cl.duoc.wim100;

import cl.duoc.wim100.controller.AuthController;
import cl.duoc.wim100.dto.LoginRequest;
import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.exception.CredencialesInvalidasException;
import cl.duoc.wim100.exception.GlobalExceptionHandler;
import cl.duoc.wim100.model.Rol;
import cl.duoc.wim100.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {
    private AuthService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(AuthService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void loginCorrecto() throws Exception {
        when(service.login(new LoginRequest("evaluadora1", "clave")))
                .thenReturn(new LoginResponse(2L, "evaluadora1", "Ana Evaluadora", Rol.EVALUADORA));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"evaluadora1\",\"password\":\"clave\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.usuario").value("evaluadora1"))
                .andExpect(jsonPath("$.nombre").value("Ana Evaluadora"))
                .andExpect(jsonPath("$.rol").value("EVALUADORA"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rechazarCredencialesIncorrectas() throws Exception {
        when(service.login(any())).thenThrow(new CredencialesInvalidasException("Credenciales incorrectas"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"evaluadora1\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Credenciales incorrectas"));
    }

    @Test
    void rechazarCamposVacios() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\" \",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.usuario").exists())
                .andExpect(jsonPath("$.errores.password").exists());
        verifyNoInteractions(service);
    }

    @Test
    void rechazarCamposAusentes() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rechazarJsonInvalido() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{invalido"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
