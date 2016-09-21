import java.util.jar.JarFile;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*

final File warFile = new File(new File(basedir,"target"), "webapp-performance-plugin-test-1.0-SNAPSHOT.war")
assertTrue("war file doesn't exist",warFile.exists())


final JarFile jarFile = new JarFile(warFile)

assertNotNull("webapp-performance-tools jar is not found as in lib directory for war",jarFile.getEntry("WEB-INF/lib/webapp-performance-tools-1.0-SNAPSHOT.jar"))
assertNotNull("yuicompressor-2.4.8.jar is not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/yuicompressor-2.4.8.jar"))
assertNotNull("commons-lang3-3.4.jar is not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/commons-lang3-3.4.jar"))
assertNotNull("spring-core-4.3.2.RELEASE.jaris not found in lib directory for war (dependency of webapp-performance-tools)",jarFile.getEntry("WEB-INF/lib/spring-core-4.3.2.RELEASE.jar"))

final InputStream propertiesFileInputStream = jarFile.getInputStream(jarFile.getEntry("META-INF/WebappPerformanceConfig.properties"))
final InputStream webXMLInputStream = jarFile.getInputStream(jarFile.getEntry("WEB-INF/web.xml"))

try {

    StringWriter webXmlStringWriter = new StringWriter();

    IOUtils.copy(webXMLInputStream, webXmlStringWriter);

    def expectedWebXML = """
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2_3.dtd">
<web-app>
  <filter>
    <filter-name>cacheFilter</filter-name>
    <filter-class>com.github.vincentrussell.filter.webapp.performance.filter.CacheFilter</filter-class>
    <init-param>
      <param-name>processCSS</param-name>
      <param-value>true</param-value>
    </init-param>
    <init-param>
      <param-name>processImages</param-name>
      <param-value>false</param-value>
    </init-param>
    <init-param>
      <param-name>processJs</param-name>
      <param-value>true</param-value>
    </init-param>
    <init-param>
      <param-name>enabled</param-name>
      <param-value>true</param-value>
    </init-param>
    <init-param>
      <param-name>exclusions</param-name>
      <param-value>/exclusion1,/exclusion2</param-value>
    </init-param>
    <init-param>
      <param-name>extensions</param-name>
      <param-value>exe,bat</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>cacheFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  <display-name>Archetype Created Web Application</display-name>
</web-app>
""".trim();

    def actualWebXml = webXmlStringWriter.toString().trim();

    assertEquals(expectedWebXML, actualWebXml);

    final Properties properties = new Properties()
    properties.load(propertiesFileInputStream)

    final String hash = properties.get("hash")
    assertNotNull("can't find hash in properties file", hash);
    assertNotNull("can't find bundle.css.bundle2.url in properties file", properties.get("bundle.css.bundle2.url"))
    assertNotNull("can't find bundle.js.bundle1.url in properties file", properties.get("bundle.js.bundle1.url"))


    assertNotNull("bundle 2 not found in war for war", jarFile.getEntry("_cf/$hash/cssBundles/bundle2.min.css"))
    assertNotNull("bundle 1 not found in war for war", jarFile.getEntry("_cf/$hash/jsBundles/bundle1.min.js"))

} finally {
    IOUtils.closeQuietly(propertiesFileInputStream);
    IOUtils.closeQuietly(webXMLInputStream);
    IOUtils.closeQuietly(jarFile);
}


final File failsafeTestFile = new File(new File(new File(basedir,"target"),"failsafe-reports"), "com.github.vincentrussell.filter.webapp.performance.PhantomTestsIT.txt")

final InputStream failsafeInputStream = new FileInputStream(failsafeTestFile)

try {
    String testResults = IOUtils.toString(failsafeInputStream)
    assertFalse("PhantomTestsIT test failed",testResults.contains("FAILURE"))

} finally {
    IOUtils.closeQuietly(failsafeInputStream);
}