package com.github.vincentrussell.filter.webapp.performance.plugin.webxml;

import com.github.approval.Approvals;
import com.github.approval.reporters.Reporters;
import com.github.vincentrussell.filter.webapp.performance.filter.CompressingFilterConfig;
import com.github.vincentrussell.filter.webapp.performance.filter.CacheFilterConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class WebXmlModifierTest {

    @Before
    public void setUp() {
        Approvals.setReporter(Reporters.console());
    }


    private Path getApprovalPath(String testName) {
        final String basePath = Paths.get("src", "test", "resources", "approvals", this.getClass().getSimpleName()).toString();
        return Paths.get(basePath, testName);
    }

    @Test(expected = NullPointerException.class)
    public void nullInputStream() throws IOException {
        new WebXmlModifier(null);
    }

    @Test(expected = IOException.class)
    public void invalidXml() throws IOException {
        new WebXmlModifier(new ByteArrayInputStream("dfasdfsf".getBytes()));
    }


    @Test(expected = IOException.class)
    public void xmlValidButNotWebXml() throws IOException {
        new WebXmlModifier(new ByteArrayInputStream("<web-app-invalid></web-app-invalid>".getBytes()));
    }


    @Test
    public void webXmlWithContextParamAndOtherStuffCacheFilterConfigWithOtherOptions() throws IOException {
        CacheFilterConfig cacheFilterConfig = new CacheFilterConfig();
        cacheFilterConfig.addExclusion("/exclusion1","/exclusion2");
        cacheFilterConfig.addExtension("bat","exe");
        inputStreamTest("webXmlWithContextParamAndOtherStuff.xml","webXmlWithContextParamAndOtherStuffCacheFilterConfigWithOtherOptions", cacheFilterConfig,null);
    }


    @Test
    public void webXmlWithContextParamAndOtherStuffCacheFilterConfig() throws IOException {
       inputStreamTest("webXmlWithContextParamAndOtherStuff.xml","webXmlWithCowebXmlWithContextParamAndOtherStuffCacheFilterConfigntextParamAndOtherStuff",new CacheFilterConfig.Builder().setEnabled(true).build(),null);
    }

    @Test
    public void webXmlEmptyCacheFilterConfig() throws IOException {
        inputStreamTest("webXmlEmpty.xml","webXmlEmptyCacheFilterConfig",new CacheFilterConfig.Builder().setEnabled(true).build(),null);
    }

    @Test
    public void webXmlWithContextParamAndEndCacheFilterConfig() throws IOException {
        inputStreamTest("webXmlWithContextParamAndEnd.xml","webXmlWithContextParamAndEndCacheFilterConfig",new CacheFilterConfig.Builder().setEnabled(true).build(),null);
    }

    @Test
    public void noContextParamAtAllCacheFilterConfig() throws IOException {
        inputStreamTest("noContextParamAtAll.xml","noContextParamAtAllCacheFilterConfig",new CacheFilterConfig.Builder().setEnabled(true).build(),null);
    }

    @Test
    public void webXmlWithContextParamAndOtherStuffCompressingConfig() throws IOException {
        inputStreamTest("webXmlWithContextParamAndOtherStuff.xml","webXmlWithContextParamAndOtherStuffCompressingConfig",null,new CompressingFilterConfig.Builder().setDebug(true).build());
    }

    @Test
    public void webXmlEmptyCompressingConfig() throws IOException {
        inputStreamTest("webXmlEmpty.xml","webXmlEmptyCompressingConfig",null,new CompressingFilterConfig.Builder().setDebug(true).build());
    }

    @Test
    public void webXmlWithContextParamAndEndCompressingConfig() throws IOException {
        inputStreamTest("webXmlWithContextParamAndEnd.xml","webXmlWithContextParamAndEndCompressingConfig",null,new CompressingFilterConfig.Builder().setDebug(true).build());
    }

    @Test
    public void noContextParamAtAllCompressingConfig() throws IOException {
        inputStreamTest("noContextParamAtAll.xml","noContextParamAtAllCompressingConfig",null,new CompressingFilterConfig.Builder().setDebug(true).build());
    }


    @Test
    public void webXmlWithContextParamAndOtherStuffCompressingConfigWithOtherOptions() throws IOException {
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig.Builder()
                .setCompressionThreshold(90210)
                .setIncludePathPatterns(Arrays.asList("\\/hello/.*","\\/hello/.*"))
                .setIncludeContentTypes(Arrays.asList("plain/text","plain/html"))
                .setDebug(true)
                .setStatsEnabled(true)
                .build();
        inputStreamTest("webXmlWithContextParamAndOtherStuff.xml","webXmlWithContextParamAndOtherStuffCompressingConfigWithOtherOptions",null,compressingFilterConfig);
    }

    @Test
    public void webXmlEmptyNullFilters() throws IOException {
        inputStreamTest("webXmlEmpty.xml","webXmlEmptyNullFilters",null,null);
    }

    @Test
    public void webXmlEmptyNotConfiguredFilters() throws IOException {
        inputStreamTest("webXmlEmpty.xml","webXmlEmptyNotConfiguredFilters",new CacheFilterConfig.Builder().build(),new CompressingFilterConfig.Builder().build());
    }

    private void inputStreamTest(String fileName, String approvalTestName, CacheFilterConfig cacheFilterConfig, CompressingFilterConfig compressingFilterConfig) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/webXmlModifierTest/"+fileName)) {
            WebXmlModifier webXmlModifier = new WebXmlModifier(inputStream, cacheFilterConfig, compressingFilterConfig);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                webXmlModifier.writeToOutputStream(outputStream);
                Approvals.verify(outputStream.toString("UTF-8"), getApprovalPath(approvalTestName));
            }
        }
    }

}
