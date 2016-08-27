package com.github.vincentrussell.filter.webapp.performance;

public class ConfigurationProperties {

    public static final String LIST_SEPARATOR = ",";
    public static final String EXTENSIONS = "extensions";
    public static final String EXCLUSIONS = "exclusions";
    public static final String ENABLED = "enabled";
    public static final String PROCESS_JS = "processJs";
    public static final String PROCESS_CSS = "processCSS";
    public static final String PROCESS_IMAGES = "processImages";
    public static final String PROPERTIES_FILE_NAME = "WebappPerformanceConfig.properties";
    public static final String CACHE_FILTER_URI_PREFIX = "_cf";

    public static String normalizeBundleName(String bundleName) {
        if (bundleName == null) {
            return null;
        }
        return bundleName.toLowerCase().replaceAll("^/", "").replaceAll("\\s+", "-");
    }
}
