package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.google.common.base.Splitter;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.Validate.notNull;

public class CacheFilter implements Filter {

    private static final List<String> DEFAULT_IMAGE_EXTENSIONS =
            Arrays.asList(new String[] { "gif", "jpg", "png", "ico" });
    private static final long SECONDS_IN_DAY = 60 * 60 * 24;
    private static final long TEN_YEARS_SECONDS = SECONDS_IN_DAY * 365 * 10;
    protected static final long TEN_YEARS_MILLIS = TEN_YEARS_SECONDS * 1000;
    protected static final String MAX_AGE = "public, max-age=" + TEN_YEARS_SECONDS;
    private boolean shouldProcessImages;

    private boolean shouldProcessCss;
    private boolean shouldProcessJs;
    private boolean isEnabled;
    private final AntPathMatcher _pathMatcher = new AntPathMatcher();
    private final Set<String> exclusions = new HashSet<>();
    private final Set<String> extensions = new HashSet<>(
            Arrays.asList(new String[] { "js", "css" }));

    @Override
    public void init(final FilterConfig filterConfig) {
        shouldProcessImages = getValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_PROCESS_IMAGES,Boolean.TRUE);
        shouldProcessCss = getValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_PROCESS_CSS,Boolean.TRUE);
        shouldProcessJs = getValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_PROCESS_JS,Boolean.TRUE);
        isEnabled = getValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_ENABLED,Boolean.TRUE);
        exclusions.addAll(getListValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_EXCLUSIONS));
        final List<String> suppliedExtensions = getListValueFromFilterConfig(filterConfig, ConfigurationProperties.CACHE_EXTENSIONS);

        if (suppliedExtensions.size() > 0) {
            extensions.addAll(suppliedExtensions);
        } else {
            extensions.addAll(DEFAULT_IMAGE_EXTENSIONS);
        }
    }

    public void setCacheFilterConfig(CacheFilterConfig filterConfig) {
        init(filterConfig);
    }

    private <T> T getValueFromFilterConfig(FilterConfig filterConfig, String parameter, Object defaultValue) {
        notNull(defaultValue);
        notNull(parameter);
        notNull(filterConfig);
        Object value = filterConfig.getInitParameter(parameter);
        if (value == null) {
            value = defaultValue;
        }

        if (Boolean.class.isInstance(defaultValue)) {
            return (T)Boolean.valueOf(value.toString());
        }

        throw new IllegalArgumentException("not sure how to process type:" + defaultValue.getClass().getSimpleName());
    }

    private List<String> getListValueFromFilterConfig(FilterConfig filterConfig, String parameter) {
        notNull(parameter);
        notNull(filterConfig);
        String value = filterConfig.getInitParameter(parameter);
        if (value == null) {
            return Collections.emptyList();
        }
        return Splitter.on(ConfigurationProperties.LIST_SEPARATOR).splitToList(value);
    }


    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;

        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath();

        if (isEnabled && isCacheable(uri,contextPath)) {
            response.setDateHeader("Expires", System.currentTimeMillis() + TEN_YEARS_MILLIS);
            response.setHeader("Cache-Control", MAX_AGE);
            if (uri.endsWith(".gz.css") || uri.endsWith(".gz.js")) {
                response.addHeader("Content-Encoding", "gzip");
                response.addHeader("Vary", "Accept-Encoding");
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isCacheable(final String uri, final String contextPath) {

        if (!uri.startsWith(contextPath + "/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX)) {
            return false;
        }

        if (isExcluded(uri)) {
            return false;
        }

        int extensionIndex = uri.lastIndexOf('.');
        if (extensionIndex == -1) {
            return false;
        }

        String extension = uri.substring(extensionIndex + 1).toLowerCase();
        if (!extensions.contains(extension)) {
            return false;
        }

        if (extension.equals("css")) {
            if (!shouldProcessCss) {
                return false;
            }
        }
        else if (extension.equals("js")) {
            if (!shouldProcessJs) {
                return false;
            }
        }
        else if (!shouldProcessImages) {
            return false;
        }

        return true;
    }

    private boolean isExcluded(final String uri) {
        String testedUri = uri.startsWith("/") ? uri.substring(1) : uri;
        for (String pattern : exclusions) {
            if (_pathMatcher.match(pattern, testedUri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

}