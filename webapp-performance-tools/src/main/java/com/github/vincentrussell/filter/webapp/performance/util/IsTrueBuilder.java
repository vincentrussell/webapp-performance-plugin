package com.github.vincentrussell.filter.webapp.performance.util;

public class IsTrueBuilder {

    private boolean isTrue = true;

    public IsTrueBuilder append(boolean value) {
        isTrue &= (value == true);
        return this;
    }

    public boolean isTrue() {
        return this.isTrue;
    }
}
