package com.github.vincentrussell.filter.webapp.performance.filter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompressingFilterConfigTest {

    @Test
    public void isConfiguredFalseWhenNotTouched() {
        assertFalse(new CompressingFilterConfig().isConfigured());
        assertFalse(new CompressingFilterConfig.Builder().build().isConfigured());
    }

    @Test
    public void isConfiguredTrueWhenTouched() {
        assertTrue(new CompressingFilterConfig.Builder()
                .setDebug(true)
                .build().isConfigured());
    }

    @Test
    public void defaultIncludeContentTypes() {
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig();
        assertEquals(CompressingFilterConfig.DEFAULT_INCLUDE_CONTENT_TYPES,compressingFilterConfig.getIncludeContentTypes());
    }

    @Test
    public void defaultUrlPatters() {
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig();
        assertEquals(CompressingFilterConfig.DEFAULT_URL_PATTERNS,compressingFilterConfig.getUrlPatterns());
    }

}
