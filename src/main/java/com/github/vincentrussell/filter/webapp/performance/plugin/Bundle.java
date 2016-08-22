package com.github.vincentrussell.filter.webapp.performance.plugin;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class Bundle {

    @Parameter(property = "name", required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
