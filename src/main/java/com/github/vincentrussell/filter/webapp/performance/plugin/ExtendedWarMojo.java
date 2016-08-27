package com.github.vincentrussell.filter.webapp.performance.plugin;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.compress.util.Compressor;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.artifact.JavaScopes;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Mojo( name = "war", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME )
public class ExtendedWarMojo extends WarMojo {

    public static final String JS_BUNDLES = "jsBundles";
    public static final String CSS_BUNDLES = "cssBundles";

    private static ArtifactHandler JAVA_ARTIFACT_HANDLER = new DefaultArtifactHandler("jar") {
        @Override
        public String getExtension() {
            return "jar";
        }

        @Override
        public String getType() {
            return "jar";
        }

        @Override
        public String getLanguage() {
            return "java";
        }

        @Override
        public boolean isAddedToClasspath() {
            return true;
        }
    };

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
    private MavenSession mavenSession;

    @Component
    private ArtifactResolver artifactResolver;


    @Component
    private RepositorySystem repoSystem = null;

    @Parameter( defaultValue = "${repositorySystemSession}", readonly = true )
    private RepositorySystemSession repoSession;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true )
    private List<RemoteRepository> remoteRepos;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        compressBundlesAndAddToWar();

        try {
            addWebAppPerformanceToolsDependency();
        } catch (ArtifactDescriptorException e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
        super.execute();
    }

    private void addWebAppPerformanceToolsDependency() throws ArtifactDescriptorException {
        final Artifact webappPerformanceToolsArtifact = new DefaultArtifact(groupId + ":webapp-performance-tools:" + version);
        final Dependency webappPerformanceToolsDependency = new Dependency(webappPerformanceToolsArtifact,JavaScopes.COMPILE);

        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( webappPerformanceToolsArtifact );
        for (RemoteRepository remoteRepository :remoteRepos) {
            descriptorRequest.addRepository(remoteRepository);
        }

        ArtifactDescriptorResult descriptorResult = repoSystem.readArtifactDescriptor(repoSession, descriptorRequest);

        List<Dependency> compileDependencies = Lists.newArrayList(Iterables.transform(
                Iterables.filter(Iterables.concat(descriptorResult.getDependencies(),
                        Arrays.asList(webappPerformanceToolsDependency)),
                        new Predicate<Dependency>() {
            @Override
            public boolean apply(Dependency dependency) {
                return JavaScopes.COMPILE.equals(dependency.getScope());
            }
        }), new Function<Dependency, Dependency>() {
            @Override
            public Dependency apply(@Nullable Dependency dependency) {
                ArtifactRequest artifactRequest = new ArtifactRequest();
                artifactRequest.setArtifact(dependency.getArtifact());
                artifactRequest.setRepositories(remoteRepos);
                try {
                    ArtifactResult artifactResult = repoSystem.resolveArtifact(repoSession, artifactRequest);
                    return dependency.setArtifact(artifactResult.getArtifact());
                } catch (ArtifactResolutionException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        }));

        mavenProject.getDependencies().addAll(Lists.transform(compileDependencies, new Function<Dependency, org.apache.maven.model.Dependency>() {
            @Override
            public org.apache.maven.model.Dependency apply(@Nullable Dependency dependency) {
                org.apache.maven.model.Dependency destinationDep = new org.apache.maven.model.Dependency();
                destinationDep.setArtifactId(dependency.getArtifact().getArtifactId());
                destinationDep.setGroupId(dependency.getArtifact().getGroupId());
                destinationDep.setVersion(dependency.getArtifact().getVersion());
                destinationDep.setScope(dependency.getScope());
                return destinationDep;
            }
        }));

        mavenProject.getArtifacts().addAll(Lists.transform(compileDependencies, new Function<Dependency, org.apache.maven.artifact.Artifact>() {
            @Override
            public org.apache.maven.artifact.Artifact apply(@Nullable Dependency dependency) {
                Artifact sourceArtifact = dependency.getArtifact();
                org.apache.maven.artifact.Artifact destinationArt = new org.apache.maven.artifact.DefaultArtifact(sourceArtifact.getGroupId(),
                        sourceArtifact.getArtifactId(), VersionRange.createFromVersion(sourceArtifact.getVersion()),
                        dependency.getScope(),sourceArtifact.getExtension(),sourceArtifact.getClassifier(),JAVA_ARTIFACT_HANDLER);
                destinationArt.setFile(sourceArtifact.getFile());
                return destinationArt;
            }

        }));
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
