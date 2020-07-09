package com.github.natanbc.ocmips;

import li.cil.oc.api.machine.ExecutionResult;

public class StopExecution extends RuntimeException {
    private final ExecutionResult reason;

    public StopExecution(ExecutionResult reason) {
        this.reason = reason;
    }

    public ExecutionResult getReason() {
        return reason;
    }
}
