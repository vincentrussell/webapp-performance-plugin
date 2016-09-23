package com.github.vincentrussell.filter.webapp.performance.filter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheFilterConfigTest {

    @Test
    public void isConfiguredFalseWhenNotTouched() {
        assertFalse(new CacheFilterConfig().isConfigured());
        assertFalse(new CacheFilterConfig.Builder().build().isConfigured());
    }

    @Test
    public void isConfiguredTrueWhenTouched() {
        assertTrue(new CacheFilterConfig.Builder()
                .setEnabled(true)
                .build().isConfigured());
    }

}
