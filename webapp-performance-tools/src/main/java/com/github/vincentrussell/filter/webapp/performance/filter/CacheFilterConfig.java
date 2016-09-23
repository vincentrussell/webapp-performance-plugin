package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.util.IsTrueBuilder;
import com.google.common.base.Joiner;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

public class CacheFilterConfig extends AbstractFilterConfig {

    private Boolean shouldProcessImages = null;
    private Boolean shouldProcessCss = null;
    private Boolean shouldProcessJs = null;
    private Boolean enabled = null;
    private final Set<String> exclusions = new LinkedHashSet<>();
    private final Set<String> extensions = new LinkedHashSet<>();


    @Override
    public String getFilterName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInitParameter(String name) {
        switch (name) {
            case ConfigurationProperties.CACHE_PROCESS_IMAGES:
                return getBooleanValue(shouldProcessImages);
            case ConfigurationProperties.CACHE_PROCESS_CSS:
                return getBooleanValue(shouldProcessCss);
            case ConfigurationProperties.CACHE_PROCESS_JS:
                return getBooleanValue(shouldProcessJs);
            case ConfigurationProperties.CACHE_ENABLED:
                return getBooleanValue(enabled);
            case ConfigurationProperties.CACHE_EXCLUSIONS:
                return Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(exclusions);
            case ConfigurationProperties.CACHE_EXTENSIONS:
                return Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(extensions);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Enumeration getInitParameterNames() {
        throw new UnsupportedOperationException();
    }

    public Boolean isShouldProcessImages() {
        return shouldProcessImages;
    }

    public void setShouldProcessImages(Boolean shouldProcessImages) {
        this.shouldProcessImages = shouldProcessImages;
    }

    public Boolean isShouldProcessCss() {
        return shouldProcessCss;
    }

    public void setShouldProcessCss(Boolean shouldProcessCss) {
        this.shouldProcessCss = shouldProcessCss;
    }

    public Boolean isShouldProcessJs() {
        return shouldProcessJs;
    }

    public void setShouldProcessJs(Boolean shouldProcessJs) {
        this.shouldProcessJs = shouldProcessJs;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void addExclusion(String... exclusion) {
        exclusions.addAll(Arrays.asList(exclusion));
    }

    public void addExtension(String... extension) {
        extensions.addAll(Arrays.asList(extension));
    }

    public void setExclusions(String... exclusion) {
        exclusions.clear();
        exclusions.addAll(Arrays.asList(exclusion));
    }

    public void setExtensions(String... extension) {
        extensions.clear();
        extensions.addAll(Arrays.asList(extension));
    }

    public Set<String> getExclusions() {
        return Collections.unmodifiableSet(exclusions);
    }

    public Set<String> getExtensions() {
        return Collections.unmodifiableSet(extensions);
    }

    @Override
    public boolean isConfigured() {
        return !new IsTrueBuilder()
                .append(shouldProcessImages == null)
                .append(shouldProcessCss == null)
                .append(shouldProcessJs == null)
                .append(enabled == null)
                .append(exclusions.size() == 0)
                .append(extensions.size() == 0)
                .isTrue();
    }

    public static class Builder {
        private Boolean shouldProcessImages = null;
        private Boolean shouldProcessCss = null;
        private Boolean shouldProcessJs = null;
        private Boolean enabled = null;
        private final Set<String> exclusions = new HashSet<>();
        private final Set<String> extensions = new HashSet<>();

        public Builder setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setShouldProcessImages(Boolean shouldProcessImages) {
            this.shouldProcessImages = shouldProcessImages;
            return this;
        }

        public Builder setShouldProcessCss(Boolean shouldProcessCss) {
            this.shouldProcessCss = shouldProcessCss;
            return this;
        }

        public Builder setShouldProcessJs(Boolean shouldProcessJs) {
            this.shouldProcessJs = shouldProcessJs;
            return this;
        }

        public Builder addExclusion(String... exclusion) {
            this.exclusions.addAll(Arrays.asList(exclusion));
            return this;
        }

        public Builder addExtension(String... extension) {
            this.extensions.addAll(Arrays.asList(extension));
            return this;
        }

        public CacheFilterConfig build() {
            CacheFilterConfig cacheFilterConfig = new CacheFilterConfig();
            cacheFilterConfig.setEnabled(this.enabled);
            cacheFilterConfig.setShouldProcessCss(this.shouldProcessCss);
            cacheFilterConfig.setShouldProcessImages(this.shouldProcessImages);
            cacheFilterConfig.setShouldProcessJs(this.shouldProcessJs);
            cacheFilterConfig.addExclusion(this.exclusions.toArray(new String[exclusions.size()]));
            cacheFilterConfig.addExtension(this.extensions.toArray(new String[extensions.size()]));
            return cacheFilterConfig;
        }

    }
}
