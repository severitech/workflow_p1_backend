package com.workflow.exception;

/** Excepción de negocio del sistema de workflow */
public class WorkflowException extends RuntimeException {

    public WorkflowException(String mensaje) {
        super(mensaje);
    }

    public WorkflowException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
