package com.workflow.exception;

/** Excepción cuando un usuario no tiene permiso suficiente sobre un recurso */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
