package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class CacheFilterTest {

    public static final String CONTEXT_PATH = "/contextPath";
    CacheFilter cacheFilter;
    HttpServletRequest httpServletRequest;
    HttpServletResponse httpServletResponse;
    FilterChain filterChain;

    @Before
    public void before() {
        cacheFilter = new CacheFilter();
        cacheFilter.init(new CacheFilterConfig());
        httpServletRequest = mock(HttpServletRequest.class);
        httpServletResponse = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        Mockito.when(httpServletRequest.getContextPath()).thenReturn(CONTEXT_PATH);
        Mockito.reset(httpServletResponse);
    }


    @Test
    public void nonCacheableURL() throws IOException, ServletException {
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn("/test/whatever.html");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(never());
        assertEncryptionHeadersAreAdded(never());
        assertFilterChainIsCalled(times(1));
    }


    @Test
    public void cacheableUrl() throws IOException, ServletException {
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(CONTEXT_PATH + "/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.js");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(times(1));
        assertEncryptionHeadersAreAdded(never());
        assertFilterChainIsCalled(times(1));
    }

    @Test
    public void cacheableUrlNotEnabled() throws IOException, ServletException {
        cacheFilter = new CacheFilter();
        cacheFilter.init(new CacheFilterConfig.Builder()
            .setEnabled(false)
            .build());
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.js");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(never());
        assertEncryptionHeadersAreAdded(never());
        assertFilterChainIsCalled(times(1));
    }

    @Test
    public void exclusions() throws IOException, ServletException {
        cacheFilter = new CacheFilter();
        cacheFilter.init(new CacheFilterConfig.Builder()
                .addExclusion("*/test2/whatever32.js,*/test/whatever.js")
                .build());
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.js");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(never());
        assertEncryptionHeadersAreAdded(never());
        assertFilterChainIsCalled(times(1));
    }

    @Test
    public void extensions() throws IOException, ServletException {
        cacheFilter = new CacheFilter();
        cacheFilter.init(new CacheFilterConfig.Builder()
                .addExtension("text,txt,html")
                .build());
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(CONTEXT_PATH + "/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.text");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(times(1));
        assertEncryptionHeadersAreAdded(never());
        assertFilterChainIsCalled(times(1));
    }

    @Test
    public void cacheableUrlWithEncryptionForCss() throws IOException, ServletException {
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(CONTEXT_PATH + "/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.gz.css");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(times(1));
        assertFilterChainIsCalled(times(1));
        assertEncryptionHeadersAreAdded(times(1));
    }

    @Test
    public void cacheableUrlWithEncryptionForJs() throws IOException, ServletException {
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn(CONTEXT_PATH + "/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX+"/test/whatever.gz.js");
        cacheFilter.doFilter(httpServletRequest,httpServletResponse,filterChain);
        assertCacheHeadersAreAdded(times(1));
        assertFilterChainIsCalled(times(1));
        assertEncryptionHeadersAreAdded(times(1));
    }

    private void assertFilterChainIsCalled(VerificationMode mode) throws IOException, ServletException {
        verify(filterChain, mode).doFilter(httpServletRequest,httpServletResponse);
    }

    private void assertCacheHeadersAreAdded(VerificationMode mode) {
        verify(httpServletResponse, mode).setDateHeader(eq("Expires"), longThat(new ArgumentMatcher<Long>() {
            long currentTime = System.currentTimeMillis() + CacheFilter.TEN_YEARS_MILLIS;
            @Override
            public boolean matches(Object o) {
                Duration duration = new Duration((long)o,currentTime);
                return !duration.isLongerThan(new Duration(2 * 1000));
            }
        }));
        verify(httpServletResponse,mode).setHeader(eq("Cache-Control"), eq(CacheFilter.MAX_AGE));
    }

    private void assertEncryptionHeadersAreAdded(VerificationMode mode) throws IOException, ServletException {
        verify(httpServletResponse,mode).addHeader(eq("Content-Encoding"), eq("gzip"));
        verify(httpServletResponse,mode).addHeader(eq("Vary"), eq("Accept-Encoding"));
    }


}
