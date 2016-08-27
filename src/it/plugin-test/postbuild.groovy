import java.util.jar.JarFile;
import static org.junit.Assert.*

final File warFile = new File(basedir, "target/webapp-performance-plugin-test-1.0-SNAPSHOT.war")
assertTrue("war file doesn't exist",warFile.exists())


final JarFile jarFile = new JarFile(warFile)

assertNotNull("webapp-performance-tools jar is not found as in lib directory for war",jarFile.getEntry("WEB-INF/lib/webapp-performance-tools-1.0-SNAPSHOT.jar"))
assertNotNull("yuicompressor-2.4.8.jar is not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/yuicompressor-2.4.8.jar"))
assertNotNull("commons-lang3-3.4.jar is not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/commons-lang3-3.4.jar"))
assertNotNull("spring-core-4.3.2.RELEASE.jaris not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/spring-core-4.3.2.RELEASE.jar"))

final InputStream inputStream = jarFile.getInputStream(jarFile.getEntry("META-INF/WebappPerformanceConfig.properties"))

final Properties properties = new Properties()
properties.load(inputStream)

final String hash = properties.get("hash")
assertNotNull("can't find hash in properties file",hash);
assertNotNull("can't find bundle.css.bundle2.url in properties file",properties.get("bundle.css.bundle2.url"))
assertNotNull("can't find bundle.js.bundle1.url in properties file",properties.get("bundle.js.bundle1.url"))


assertNotNull("bundle 2 not found in war for war",jarFile.getEntry("_cf/$hash/cssBundles/bundle2.min.css"))
assertNotNull("bundle 1 not found in war for war",jarFile.getEntry("_cf/$hash/jsBundles/bundle1.min.js"))

inputStream.close();

jarFile.close();