package com.github.vincentrussell.filter.webapp.performance;

public class ConfigurationProperties {

    public static final String LIST_SEPARATOR = ",";
    public static final String CACHE_EXTENSIONS = "extensions";
    public static final String CACHE_EXCLUSIONS = "exclusions";
    public static final String CACHE_ENABLED = "enabled";
    public static final String CACHE_PROCESS_JS = "processJs";
    public static final String CACHE_PROCESS_CSS = "processCSS";
    public static final String CACHE_PROCESS_IMAGES = "processImages";

    public static final String COMPRESSION_INCLUDE_PATH_PATTERNS = "includePathPatterns";
    public static final String COMPRESSION_EXCLUDE_PATH_PATTERNS =  "excludePathPatterns";
    public static final String COMPRESSION_INCLUDE_CONTENT_TYPES =  "includeContentTypes";
    public static final String COMPRESSION_EXCLUDE_CONTENT_TYPES =  "excludeContentTypes";
    public static final String COMPRESSION_INCLUDE_USER_AGENT_PATTERNS =  "includeUserAgentPatterns";
    public static final String COMPRESSION_EXCLUDE_USER_AGENT_PATTERNS =  "excludeUserAgentPatterns";
    public static final String COMPRESSION_USER_AGENT_PATTERNS =  "userAgentPatterns";
    public static final String COMPRESSION_NO_VARY_HEADER_PATTERNS =  "noVaryHeaderPatterns";
    public static final String COMPRESSION_PATH_PATTERNS =  "pathPatterns";
    public static final String COMPRESSION_COMPRESSION_THRESHOLD =  "compressionThreshold";
    public static final String COMPRESSION_STATS_ENABLED =  "statsEnabled";
    public static final String COMPRESSION_DEBUG =  "debug";
    public static final String COMPRESSION_URL_PATTERNS =  "urlPatterns";

    public static final String PROPERTIES_FILE_NAME = "WebappPerformanceConfig.properties";
    public static final String CACHE_FILTER_URI_PREFIX = "_cf";

    public static String normalizeBundleName(String bundleName) {
        if (bundleName == null) {
            return null;
        }
        return bundleName.toLowerCase().replaceAll("^/", "").replaceAll("\\s+", "-");
    }
}
