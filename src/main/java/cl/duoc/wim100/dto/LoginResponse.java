package cl.duoc.wim100.dto;

import cl.duoc.wim100.model.Rol;

public record LoginResponse(Long id, String usuario, String nombre, Rol rol) {}