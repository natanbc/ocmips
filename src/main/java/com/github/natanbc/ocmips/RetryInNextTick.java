package com.github.natanbc.ocmips;

import li.cil.oc.api.machine.LimitReachedException;

public class RetryInNextTick extends RuntimeException {
    private final boolean isLimitReached;

    public RetryInNextTick(Throwable cause) {
        super(cause);
        this.isLimitReached = cause instanceof LimitReachedException;
    }

    public boolean isLimitReached() {
        return isLimitReached;
    }
}
