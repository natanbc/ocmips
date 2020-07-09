package com.github.natanbc.mipscpu;

public class MipsException extends Exception {
    public MipsException(String s) {
        super(s);
    }

    public MipsException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
