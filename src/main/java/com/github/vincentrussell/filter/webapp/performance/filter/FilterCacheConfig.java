package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.google.common.base.Joiner;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

public class FilterCacheConfig implements FilterConfig {

    private boolean shouldProcessImages = true;
    private boolean shouldProcessCss = true;
    private boolean shouldProcessJs = true;
    private boolean enabled = true;
    private final Set<String> exclusions = new HashSet<>();
    private final Set<String> extensions = new HashSet<>();


    public String getFilterName() {
        throw new UnsupportedOperationException();
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public String getInitParameter(String name) {
        switch (name) {
            case ConfigurationProperties.PROCESS_IMAGES:
                return Boolean.valueOf(shouldProcessImages).toString();
            case ConfigurationProperties.PROCESS_CSS:
                return Boolean.valueOf(shouldProcessCss).toString();
            case ConfigurationProperties.PROCESS_JS:
                return Boolean.valueOf(shouldProcessJs).toString();
            case ConfigurationProperties.ENABLED:
                return Boolean.valueOf(enabled).toString();
            case ConfigurationProperties.EXCLUSIONS:
                return Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(exclusions);
            case ConfigurationProperties.EXTENSIONS:
                return Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(extensions);
            default:
                throw new IllegalArgumentException();
        }
    }

    public Enumeration getInitParameterNames() {
        throw new UnsupportedOperationException();
    }

    public boolean isShouldProcessImages() {
        return shouldProcessImages;
    }

    public void setShouldProcessImages(boolean shouldProcessImages) {
        this.shouldProcessImages = shouldProcessImages;
    }

    public boolean isShouldProcessCss() {
        return shouldProcessCss;
    }

    public void setShouldProcessCss(boolean shouldProcessCss) {
        this.shouldProcessCss = shouldProcessCss;
    }

    public boolean isShouldProcessJs() {
        return shouldProcessJs;
    }

    public void setShouldProcessJs(boolean shouldProcessJs) {
        this.shouldProcessJs = shouldProcessJs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
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

    public static class Builder {
        private boolean shouldProcessImages = true;
        private boolean shouldProcessCss = true;
        private boolean shouldProcessJs = true;
        private boolean enabled = true;
        private final Set<String> exclusions = new HashSet<>();
        private final Set<String> extensions = new HashSet<>();

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setShouldProcessImages(boolean shouldProcessImages) {
            this.shouldProcessImages = shouldProcessImages;
            return this;
        }

        public Builder setShouldProcessCss(boolean shouldProcessCss) {
            this.shouldProcessCss = shouldProcessCss;
            return this;
        }

        public Builder setShouldProcessJs(boolean shouldProcessJs) {
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

        public FilterCacheConfig build() {
            FilterCacheConfig filterCacheConfig = new FilterCacheConfig();
            filterCacheConfig.setEnabled(this.enabled);
            filterCacheConfig.setShouldProcessCss(this.shouldProcessCss);
            filterCacheConfig.setShouldProcessImages(this.shouldProcessImages);
            filterCacheConfig.setShouldProcessJs(this.shouldProcessJs);
            filterCacheConfig.addExclusion(this.exclusions.toArray(new String[exclusions.size()]));
            filterCacheConfig.addExtension(this.extensions.toArray(new String[extensions.size()]));
            return filterCacheConfig;
        }

    }
}
