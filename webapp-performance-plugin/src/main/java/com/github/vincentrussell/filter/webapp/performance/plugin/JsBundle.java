package com.github.vincentrussell.filter.webapp.performance.plugin;

import org.apache.maven.plugins.annotations.Parameter;

public class JsBundle extends Bundle {

    @Parameter(property = "javascriptFiles", required = true)
    protected String[] javascriptFiles = new String[0];

    public String[] getJavascriptFiles() {
        return javascriptFiles;
    }

    public void setJavascriptFiles(String[] javascriptFiles) {
        this.javascriptFiles = javascriptFiles;
    }
}
