package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.util.IsTrueBuilder;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

public class CompressingFilterConfig extends AbstractFilterConfig {

    static final Set<String> DEFAULT_INCLUDE_CONTENT_TYPES = new LinkedHashSet<>(Arrays.asList("text/html", "text/xml", "text/plain"));
    static final Set<String> DEFAULT_URL_PATTERNS = new LinkedHashSet<>(Arrays.asList("/*"));

    private Set<String> includePathPatterns = new LinkedHashSet<>();
    private Set<String> excludePathPatterns = new LinkedHashSet<>();
    private Set<String> includeContentTypes = new LinkedHashSet<>();
    private Set<String> excludeContentTypes = new LinkedHashSet<>();
    private Set<String> includeUserAgentPatterns = new LinkedHashSet<>();
    private Set<String> excludeUserAgentPatterns = new LinkedHashSet<>();
    private Set<String> noVaryHeaderPatterns = new LinkedHashSet<>();
    private Set<String> urlPatterns = new LinkedHashSet<>();
    private Integer compressionThreshold = null;
    private Boolean statsEnabled = null;
    private Boolean debug = null;
    private ServletContext servletContext;

    @Override
    public String getFilterName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        switch (name) {
            case ConfigurationProperties.COMPRESSION_INCLUDE_PATH_PATTERNS:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(includePathPatterns));
            case ConfigurationProperties.COMPRESSION_EXCLUDE_PATH_PATTERNS:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(excludePathPatterns));
            case ConfigurationProperties.COMPRESSION_INCLUDE_CONTENT_TYPES:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(getIncludeContentTypes()));
            case ConfigurationProperties.COMPRESSION_EXCLUDE_CONTENT_TYPES:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(excludeContentTypes));
            case ConfigurationProperties.COMPRESSION_INCLUDE_USER_AGENT_PATTERNS:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(includeUserAgentPatterns));
            case ConfigurationProperties.COMPRESSION_EXCLUDE_USER_AGENT_PATTERNS:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(excludeUserAgentPatterns));
            case ConfigurationProperties.COMPRESSION_NO_VARY_HEADER_PATTERNS:
                return normalize(Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(noVaryHeaderPatterns));
            case ConfigurationProperties.COMPRESSION_COMPRESSION_THRESHOLD:
                return getIntegerValue(compressionThreshold);
            case ConfigurationProperties.COMPRESSION_DEBUG:
                return getBooleanValue(debug);
            case ConfigurationProperties.COMPRESSION_STATS_ENABLED:
                return getBooleanValue(statsEnabled);
            default:
                throw new IllegalArgumentException();
        }
    }

    private String normalize(String join) {
        if (StringUtils.isEmpty(join)) {
            return null;
        }
        return join;
    }

    @Override
    public Enumeration getInitParameterNames() {
        throw new UnsupportedOperationException();
    }

    public void setIncludePathPatterns(Collection<String> includePathPatterns) {
        this.includePathPatterns.clear();
        this.includePathPatterns.addAll(includePathPatterns);
    }

    public void setExcludePathPatterns(Collection<String> excludePathPatterns) {
        this.excludePathPatterns.clear();
        this.excludePathPatterns.addAll(excludePathPatterns);
    }

    public void setIncludeContentTypes(Collection<String> includeContentTypes) {
        this.includeContentTypes.clear();
        this.includeContentTypes.addAll(includeContentTypes);
    }

    public void setExcludeContentTypes(Collection<String> excludeContentTypes) {
        this.excludeContentTypes.clear();
        this.excludeContentTypes.addAll(excludeContentTypes);
    }

    public void setIncludeUserAgentPatterns(Collection<String> includeUserAgentPatterns) {
        this.includeUserAgentPatterns.clear();
        this.includeUserAgentPatterns.addAll(includeUserAgentPatterns);
    }

    public void setExcludeUserAgentPatterns(Collection<String> excludeUserAgentPatterns) {
        this.excludeUserAgentPatterns.clear();
        this.excludeUserAgentPatterns.addAll(excludeUserAgentPatterns);
    }

    public void setNoVaryHeaderPatterns(Collection<String> noVaryHeaderPatterns) {
        this.noVaryHeaderPatterns.clear();
        this.noVaryHeaderPatterns.addAll(noVaryHeaderPatterns);
    }

    public void setCompressionThreshold(Integer compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    public void setStatsEnabled(Boolean statsEnabled) {
        this.statsEnabled = statsEnabled;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void setUrlPatterns(Collection<String> urlPatterns) {
        this.urlPatterns.clear();
        this.urlPatterns.addAll(urlPatterns);
    }

    public Set<String> getIncludePathPatterns() {
        return includePathPatterns;
    }

    public Set<String> getExcludePathPatterns() {
        return excludePathPatterns;
    }

    public Set<String> getIncludeContentTypes() {
        if (includeContentTypes.size()==0) {
            return DEFAULT_INCLUDE_CONTENT_TYPES;
        }
        return includeContentTypes;
    }

    public Set<String> getExcludeContentTypes() {
        return excludeContentTypes;
    }

    public Set<String> getIncludeUserAgentPatterns() {
        return includeUserAgentPatterns;
    }

    public Set<String> getExcludeUserAgentPatterns() {
        return excludeUserAgentPatterns;
    }

    public Set<String> getNoVaryHeaderPatterns() {
        return noVaryHeaderPatterns;
    }

    public Set<String> getUrlPatterns() {
        if (urlPatterns.size()==0) {
            return DEFAULT_URL_PATTERNS;
        }
        return urlPatterns;
    }

    public Integer getCompressionThreshold() {
        return compressionThreshold;
    }

    public Boolean isStatsEnabled() {
        return statsEnabled;
    }

    public Boolean isDebug() {
        return debug;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public boolean isConfigured() {
        return !new IsTrueBuilder()
                .append(includePathPatterns.size() == 0)
                .append(excludePathPatterns.size() == 0)
                .append(includeContentTypes.size() == 0)
                .append(excludeContentTypes.size() == 0)
                .append(includeUserAgentPatterns.size() == 0)
                .append(excludeUserAgentPatterns.size() == 0)
                .append(noVaryHeaderPatterns.size() == 0)
                .append(urlPatterns.size() == 0)
                .append(compressionThreshold == null)
                .append(statsEnabled == null)
                .append(debug == null)
                .isTrue();
    }

    public static class Builder {
        private Set<String> includePathPatterns = new LinkedHashSet<>();
        private Set<String> excludePathPatterns = new LinkedHashSet<>();
        private Set<String> includeContentTypes = new LinkedHashSet<>();
        private Set<String> excludeContentTypes = new LinkedHashSet<>();
        private Set<String> includeUserAgentPatterns = new LinkedHashSet<>();
        private Set<String> excludeUserAgentPatterns = new LinkedHashSet<>();
        private final Set<String> noVaryHeaderPatterns = new LinkedHashSet<>();
        private Integer compressionThreshold = null;
        private Boolean statsEnabled = null;
        private Boolean debug = null;
        private Set<String> urlPatterns = new LinkedHashSet<>();
        private ServletContext servletContext;

        public Builder setIncludePathPatterns(Collection<String> includePathPatterns) {
            this.includePathPatterns.clear();
            this.includePathPatterns.addAll(includePathPatterns);
            return this;
        }

        public Builder setExcludePathPatterns(Collection<String> excludePathPatterns) {
            this.excludePathPatterns.clear();
            this.excludePathPatterns.addAll(excludePathPatterns);
            return this;
        }

        public Builder setIncludeContentTypes(Collection includeContentTypes) {
            this.includeContentTypes.clear();
            this.includeContentTypes.addAll(includeContentTypes);
            return this;
        }

        public Builder setExcludeContentTypes(Collection excludeContentTypes) {
            this.excludeContentTypes.clear();
            this.excludeContentTypes.addAll(excludeContentTypes);
            return this;
        }

        public Builder setIncludeUserAgentPatterns(Collection includeUserAgentPatterns) {
            this.includeUserAgentPatterns.clear();
            this.includeUserAgentPatterns.addAll(includeUserAgentPatterns);
            return this;
        }

        public Builder setExcludeUserAgentPatterns(Collection excludeUserAgentPatterns) {
            this.excludeUserAgentPatterns.clear();
            this.excludeUserAgentPatterns.addAll(excludeUserAgentPatterns);
            return this;
        }

        public Builder setCompressionThreshold(Integer compressionThreshold) {
            this.compressionThreshold = compressionThreshold;
            return this;
        }

        public Builder setStatsEnabled(Boolean statsEnabled) {
            this.statsEnabled = statsEnabled;
            return this;
        }

        public Builder setDebug(Boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setUrlPatterns(Collection<String> urlPatterns) {
            this.urlPatterns.clear();
            this.urlPatterns.addAll(urlPatterns);
            return this;
        }

        public Builder setServletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }

        public CompressingFilterConfig build() {
            CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig();
            compressingFilterConfig.setIncludePathPatterns(includePathPatterns);
            compressingFilterConfig.setExcludePathPatterns(excludePathPatterns);
            compressingFilterConfig.setIncludeContentTypes(includeContentTypes);
            compressingFilterConfig.setExcludeContentTypes(excludeContentTypes);
            compressingFilterConfig.setIncludeUserAgentPatterns(includeUserAgentPatterns);
            compressingFilterConfig.setExcludeUserAgentPatterns(excludeUserAgentPatterns);
            compressingFilterConfig.setNoVaryHeaderPatterns(noVaryHeaderPatterns);
            compressingFilterConfig.setCompressionThreshold(compressionThreshold);
            compressingFilterConfig.setStatsEnabled(statsEnabled);
            compressingFilterConfig.setDebug(debug);
            compressingFilterConfig.setUrlPatterns(urlPatterns);
            compressingFilterConfig.setServletContext(servletContext);
            return compressingFilterConfig;
        }

    }
}
