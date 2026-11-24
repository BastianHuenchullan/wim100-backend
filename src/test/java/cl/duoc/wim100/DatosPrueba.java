package cl.duoc.wim100;

import cl.duoc.wim100.dto.PostulanteRequest;
import cl.duoc.wim100.model.EstadoPostulante;
import cl.duoc.wim100.model.Postulante;
import cl.duoc.wim100.model.Rol;
import cl.duoc.wim100.model.Usuario;

final class DatosPrueba {
    private DatosPrueba() {}

    static PostulanteRequest solicitud() {
        return new PostulanteRequest("Ana Perez", "ana@example.com", "912345678",
                "Blue Team", 3, "Experiencia ficticia en deteccion y respuesta a incidentes.",
                "https://www.linkedin.com/in/ana-demo", "cv.pdf", "Evaluadora Demo");
    }

    static Postulante postulante() {
        PostulanteRequest datos = solicitud();
        Postulante p = new Postulante();
        p.setId(1L);
        p.setNombre(datos.nombre());
        p.setCorreo(datos.correo());
        p.setTelefono(datos.telefono());
        p.setArea(datos.area());
        p.setAniosExperiencia(datos.aniosExperiencia());
        p.setResumenExperiencia(datos.resumenExperiencia());
        p.setLinkedinUrl(datos.linkedinUrl());
        p.setCvNombre(datos.cvNombre());
        p.setEstado(EstadoPostulante.POSTULADA);
        p.setFechaRegistro(1700000000000L);
        p.setRegistradaPor(datos.registradaPor());
        return p;
    }

    static Usuario usuario() {
        Usuario u = new Usuario("evaluadora1", "hash-prueba", "Ana Evaluadora", Rol.EVALUADORA);
        u.setId(2L);
        return u;
    }

    static String jsonPostulante() {
        return """
                {"nombre":"Ana Perez","correo":"ana@example.com","telefono":"912345678",
                 "area":"Blue Team","aniosExperiencia":3,
                 "resumenExperiencia":"Experiencia ficticia en deteccion y respuesta a incidentes.",
                 "linkedinUrl":"https://www.linkedin.com/in/ana-demo",
                 "cvNombre":"cv.pdf","registradaPor":"Evaluadora Demo"}
                """;
    }
}
