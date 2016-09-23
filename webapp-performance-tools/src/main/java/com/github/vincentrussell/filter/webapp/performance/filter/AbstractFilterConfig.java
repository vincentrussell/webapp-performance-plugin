package com.github.vincentrussell.filter.webapp.performance.filter;

import javax.servlet.FilterConfig;

public abstract class AbstractFilterConfig implements FilterConfig {

    protected String getBooleanValue(Boolean value) {
        if (value==null) {
            return null;
        }
        return Boolean.valueOf(value).toString();
    }

    protected String getIntegerValue(Integer value) {
        if (value==null) {
            return null;
        }
        return Integer.valueOf(value).toString();
    }

    abstract boolean isConfigured();
}
