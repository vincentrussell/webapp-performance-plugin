package com.github.vincentrussell.filter.webapp.performance.compress.util;

import com.github.approval.Approvals;
import com.github.approval.reporters.Reporters;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.Thread.currentThread;

public class CompressorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File zeros_css;
    File comment_css;
    File float_js;
    File string_combo_js;

    @Before
    public void before() throws IOException {
        Approvals.setReporter(Reporters.console());
        zeros_css = copyFromResourcesToFile("css/zeros.css");
        comment_css = copyFromResourcesToFile("css/comment.css");
        float_js = copyFromResourcesToFile("js/float.js");
        string_combo_js = copyFromResourcesToFile("js/string_combo.js");
    }

    @Test
    public void compressCssOneFile() throws IOException {
        Compressor compressor = new Compressor.Builder()
                .build();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            compressor.compressCss(Arrays.asList(zeros_css),byteArrayOutputStream );
            Approvals.verify(byteArrayOutputStream.toString("UTF-8"), getApprovalPath("zeros_css"));
        }
    }

    @Test
    public void compressCssTwoFiles() throws IOException {
        Compressor compressor = new Compressor.Builder()
                .build();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            compressor.compressCss(Arrays.asList(zeros_css, comment_css),byteArrayOutputStream );
            Approvals.verify(byteArrayOutputStream.toString("UTF-8"), getApprovalPath("zeros_and_comment_css"));
        }
    }

    @Test
    public void compressJsOneFile() throws IOException {
        Compressor compressor = new Compressor.Builder()
                .build();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            compressor.compressJs(Arrays.asList(string_combo_js),byteArrayOutputStream );
            Approvals.verify(byteArrayOutputStream.toString("UTF-8"), getApprovalPath("string_js"));
        }
    }

    @Test
    public void compressJsTwoJsFiles() throws IOException {
        Compressor compressor = new Compressor.Builder()
                .build();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            compressor.compressJs(Arrays.asList(string_combo_js,float_js),byteArrayOutputStream );
            Approvals.verify(byteArrayOutputStream.toString("UTF-8"), getApprovalPath("string_float_js"));
        }
    }

    private Path getApprovalPath(String testName) {
        final String basePath = Paths.get("src", "test", "resources", "approvals", CompressorTest.class.getSimpleName()).toString();
        return Paths.get(basePath, testName);
    }

    private File copyFromResourcesToFile(String path) throws IOException {
        File file = temporaryFolder.newFile();
        try (InputStream inputStream = currentThread().getContextClassLoader().getResourceAsStream(path);
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream,fileOutputStream);
            return file;
        }
    }

}
