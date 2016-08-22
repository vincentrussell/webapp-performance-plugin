package com.github.vincentrussell.filter.webapp.performance.plugin;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.compress.util.Compressor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Mojo( name = "war", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME )
public class ExtendedWarMojo extends WarMojo {

    public static final String JS_BUNDLES = "jsBundles";
    public static final String CSS_BUNDLES = "cssBundles";

    @Parameter(property = "bundles", required = true)
    protected Bundle[] bundles = new Bundle[0];
    @Parameter(defaultValue = "UTF-8", property = "charset", required = true)
    protected String charset;
    @Parameter(defaultValue = "-1", property = "linebreakPosition", required = true)
    protected int linebreakPosition;
    @Parameter(defaultValue = "false", property = "munge", required = true)
    protected boolean munge;
    @Parameter(defaultValue = "false", property = "verbose", required = true)
    protected boolean verbose;
    @Parameter(defaultValue = "false", property = "preserveAllSemiColons", required = true)
    protected boolean preserveAllSemiColons;
    @Parameter(defaultValue = "false", property = "disableOptimizations", required = true)
    protected boolean disableOptimizations;

    @Parameter( defaultValue = "${project.groupId}", required = true, readonly = true )
    private String groupId;

    @Parameter( defaultValue = "${project.artifactId}", required = true, readonly = true )
    private String artifactId;

    @Parameter( defaultValue = "${project.version}", required = true, readonly = true )
    private String version;

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${localRepository}", readonly = true )
    private ArtifactRepository localRepository;

    @Component
    private ArtifactFactory artifactFactory;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        compressBundlesAndAddToWar();

        addWebAppPerformanceToolsDependency();

        super.execute();
    }

    private void addWebAppPerformanceToolsDependency() {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId("webapp-performance-tools");
        dependency.setVersion(version);
        dependency.setScope("compile");

        Artifact artifact = this.artifactFactory.createDependencyArtifact(groupId,"webapp-performance-tools", VersionRange.createFromVersion(version),"jar",null,"compile");

        mavenProject.getDependencies().add(dependency);

        mavenProject.getDependencyArtifacts().add(artifact);
    }

    private void compressBundlesAndAddToWar() throws MojoExecutionException {
        String hash = getRandomHash();

        File rootDir = new File(getOutputDirectory(), getWarName());
        File cacheFilterDir = new File(rootDir, ConfigurationProperties.CACHE_FILTER_URI_PREFIX);
        File hashRootDir = new File(cacheFilterDir, hash);
        File cssCacheFilterDir = new File(hashRootDir, CSS_BUNDLES);
        File jsCacheFilterDir = new File(hashRootDir, JS_BUNDLES);
        File metaInfDir = new File(rootDir, "META-INF");
        File webInfDir = new File(rootDir, "WEB-INF");
        File webInfLibDir = new File(webInfDir, "lib");

        FileUtils.deleteQuietly(cacheFilterDir);

        cssCacheFilterDir.mkdirs();
        jsCacheFilterDir.mkdirs();
        metaInfDir.mkdirs();
        webInfLibDir.mkdirs();

        Compressor compressor = new Compressor.Builder()
                .setCharset(charset)
                .setLinebreakPosition(linebreakPosition)
                .setMunge(munge)
                .setVerbose(verbose)
                .setPreserveAllSemiColons(preserveAllSemiColons)
                .setDisableOptimizations(disableOptimizations)
                .build();

        Properties properties = new Properties();
        properties.put("hash",hash);
        Set<String> bundleNames = new HashSet<>();
        for (Bundle bundle : bundles) {
            String bundleName = ConfigurationProperties.normalizeBundleName(bundle.getName());

            if (bundleNames.contains(bundleName)) {
                throw new MojoExecutionException("bundle name: " + bundleName + " is already specified as a bundle.  Bundle names can not be repeated.");
            }

            if (JsBundle.class.isInstance(bundle)) {
                String fileName = bundleName + ".min.js";
                File outputFile = new File(jsCacheFilterDir, fileName);
                properties.put("bundle.js."+bundleName+".url","/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX
                        + "/" + hash+"/"+ JS_BUNDLES +"/" + fileName);
                JsBundle jsBundle = (JsBundle)bundle;
                List<File> files = getFilesFromBundle(jsBundle.getJavascriptFiles());
                try {
                    compressor.compressJs(files,new FileOutputStream(outputFile));
                } catch (IOException e) {
                    getLog().error(e.getMessage(),e);
                    throw new MojoExecutionException(e.getMessage(),e);
                }
            } else if (CssBundle.class.isInstance(bundle)) {
                String fileName = bundleName + ".min.css";
                File outputFile = new File(cssCacheFilterDir,fileName);
                properties.put("bundle.css."+bundleName+".url","/" + ConfigurationProperties.CACHE_FILTER_URI_PREFIX
                        + "/" + hash + "/"+ CSS_BUNDLES +"/" + fileName);
                CssBundle cssBundle = (CssBundle)bundle;
                List<File> files = getFilesFromBundle(cssBundle.getCssFiles());
                try {
                    compressor.compressCss(files,new FileOutputStream(outputFile));
                } catch (IOException e) {
                    getLog().error(e.getMessage(),e);
                    throw new MojoExecutionException(e.getMessage(),e);
                }
            } else {
                throw new MojoExecutionException("unknown bundle type: " + bundle.getClass().getName());
            }

            bundleNames.add(bundleName);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(metaInfDir, ConfigurationProperties.PROPERTIES_FILE_NAME))) {
            properties.store(fileOutputStream,ConfigurationProperties.PROPERTIES_FILE_NAME + " plugin save");
        } catch (IOException  e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }

    private List<File> getFilesFromBundle(String[] files) {
        if (files==null) {
            return null;
        }
        List<String> fileList = Arrays.asList(files);
        return Lists.transform(fileList, new Function<String, File>() {
            public File apply(String s) {
                File file = new File(s);
                if (!file.exists()) {
                    throw new RuntimeException(file.getAbsolutePath()+ " does not exist");
                }
                return file;
            }
        });
    }

    private String getRandomHash() {
        UUID uuid = UUID.randomUUID();
        return DigestUtils.md5Hex(uuid.toString());
    }

}
