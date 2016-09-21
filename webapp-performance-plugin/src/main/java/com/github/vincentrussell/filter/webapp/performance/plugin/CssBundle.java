package com.github.vincentrussell.filter.webapp.performance.plugin;

import org.apache.maven.plugins.annotations.Parameter;

public class CssBundle extends Bundle {

    @Parameter(property = "cssFiles", required = true)
    protected String[] cssFiles = new String[0];

    public String[] getCssFiles() {
        return cssFiles;
    }

    public void setCssFiles(String[] cssFiles) {
        this.cssFiles = cssFiles;
    }
}
