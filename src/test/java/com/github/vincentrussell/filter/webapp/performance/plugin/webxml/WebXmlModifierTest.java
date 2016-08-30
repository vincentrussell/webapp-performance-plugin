package com.github.vincentrussell.filter.webapp.performance.plugin.webxml;

import com.github.approval.Approvals;
import com.github.approval.reporters.Reporters;
import com.github.vincentrussell.filter.webapp.performance.filter.FilterCacheConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public void webXmlWithContextParamAndOtherStuffAndFilterConfig() throws IOException {
        FilterCacheConfig filterCacheConfig = new FilterCacheConfig();
        filterCacheConfig.addExclusion("/exclusion1","/exclusion2");
        filterCacheConfig.addExtension("bat","exe");
        inputStreamTest("webXmlWithContextParamAndOtherStuffAndFilterConfig",filterCacheConfig);
    }


    @Test
    public void webXmlWithContextParamAndOtherStuff() throws IOException {
       inputStreamTest("webXmlWithContextParamAndOtherStuff",null);
    }

    @Test
    public void webXmlEmpty() throws IOException {
        inputStreamTest("webXmlEmpty",null);
    }

    @Test
    public void webXmlWithContextParamAndEnd() throws IOException {
        inputStreamTest("webXmlWithContextParamAndEnd",null);
    }

    @Test
    public void noContextParamAtAll() throws IOException {
        inputStreamTest("noContextParamAtAll",null);
    }

    private void inputStreamTest(String fileName, FilterCacheConfig filterCacheConfig) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/"+fileName+".xml")) {
            WebXmlModifier webXmlModifier = new WebXmlModifier(inputStream,filterCacheConfig);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                webXmlModifier.writeToOutputStream(outputStream);
                Approvals.verify(outputStream.toString("UTF-8"), getApprovalPath(fileName));
            }
        }
    }

}
