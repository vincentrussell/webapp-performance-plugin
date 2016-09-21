package com.github.vincentrussell.filter.webapp.performance.compress.util;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.*;
import java.util.Collection;

import static org.apache.commons.lang3.Validate.notNull;

public class Compressor {

    private String charset = "UTF-8";
    private int linebreakPosition = -1;
    private boolean munge = false;
    private boolean verbose  = false;
    private boolean preserveAllSemiColons = false;
    private boolean disableOptimizations = false;


    public void compressJs(Collection<File> files, OutputStream outputStream) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
            for (File file : files) {
                JavaScriptCompressor compressor = new JavaScriptCompressor(new FileReader(file),new DefaultErrorReporter(file));
                compressor.compress(writer, linebreakPosition,munge,verbose,preserveAllSemiColons,disableOptimizations);
            }
        }

    }

    public void compressCss(Collection<File> files, OutputStream outputStream) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
            for (File file : files) {
                CssCompressor compressor = new CssCompressor(new FileReader(file));
                compressor.compress(writer, linebreakPosition);
            }
        }
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setLinebreakPosition(int linebreakPosition) {
        this.linebreakPosition = linebreakPosition;
    }

    public void setMunge(boolean munge) {
        this.munge = munge;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    public void setDisableOptimizations(boolean disableOptimizations) {
        this.disableOptimizations = disableOptimizations;
    }

    public static class Builder {
        private String charset = "UTF-8";
        private int linebreakPosition = -1 ;
        private boolean munge = false;
        private boolean verbose  = false;
        private boolean preserveAllSemiColons = false;
        private boolean disableOptimizations = false;

        public Builder setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder setLinebreakPosition(int linebreakPosition) {
            this.linebreakPosition = linebreakPosition;
            return this;
        }

        public Builder setMunge(boolean munge) {
            this.munge = munge;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder setPreserveAllSemiColons(boolean preserveAllSemiColons) {
            this.preserveAllSemiColons = preserveAllSemiColons;
            return this;
        }

        public Builder setDisableOptimizations(boolean disableOptimizations) {
            this.disableOptimizations = disableOptimizations;
            return this;
        }

        public Compressor build() {
            Compressor compressor = new Compressor();
            compressor.setCharset(charset);
            compressor.setLinebreakPosition(linebreakPosition);
            compressor.setMunge(munge);
            compressor.setVerbose(verbose);
            compressor.setPreserveAllSemiColons(preserveAllSemiColons);
            compressor.setDisableOptimizations(disableOptimizations);
            return compressor;
        }
    }

    private static class DefaultErrorReporter implements ErrorReporter {

        private final File file;

        private DefaultErrorReporter(File file) {
            notNull(file);
            this.file = file;
        }

        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            System.err.println("\n[WARNING] in " + file.getAbsolutePath());
            if (line < 0) {
                System.err.println("  " + message);
            } else {
                System.err.println("  " + line + ':' + lineOffset + ':' + message);
            }
        }

        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            System.err.println("[ERROR] in " + file.getAbsolutePath());
            if (line < 0) {
                System.err.println("  " + message);
            } else {
                System.err.println("  " + line + ':' + lineOffset + ':' + message);
            }
        }

        public EvaluatorException runtimeError(String message, String sourceName,
                                               int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }

}
