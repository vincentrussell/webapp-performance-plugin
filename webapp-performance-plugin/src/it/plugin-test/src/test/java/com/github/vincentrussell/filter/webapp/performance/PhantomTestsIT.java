package com.github.vincentrussell.filter.webapp.performance;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.*;

public class PhantomTestsIT {

    private static String PHANTOMJS_BINARY;
    private static String VERSION;

    WebDriver driver;
    String baseURL;

    Properties properties = new Properties();

    @BeforeClass
    public static void beforeTest() {
        PHANTOMJS_BINARY = System.getProperty("phantomjs.binary");
        VERSION = System.getProperty("project.version");

        assertNotNull(PHANTOMJS_BINARY);
        assertTrue(new File(PHANTOMJS_BINARY).exists());
    }

    @Before
    public void before() throws IOException {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        final String port = System.getProperty("jetty.port");

        if (port == null) {
            fail("System property 'jetty.port' is not set");
        }

        // Configure our WebDriver to support JavaScript and be able to find the PhantomJS binary
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("takesScreenshot", false);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                PHANTOMJS_BINARY
        );

        driver = new PhantomJSDriver(capabilities);
        baseURL = "http://localhost:" + port + "/test";

        // If the referenced JavaScript files fail to load, the test fails at this point
        driver.navigate().to(baseURL + "/index.jsp");

        File propertiesFile = FileSystems.getDefault().getPath("target/webapp-performance-plugin-test-"+VERSION+"/WEB-INF/classes/WebappPerformanceConfig.properties").toFile();
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
            properties.load(fileInputStream);
        }

    }


    @Test
    public void shouldHavePhantomJsBinary() {
        String binary = System.getProperty("phantomjs.binary");
        assertNotNull(binary);
        assertTrue(new File(binary).exists());
    }

    @Test
    public void bundleTagLibWorks() {
        String source = driver.getPageSource();
        assertEquals("<html><head></head><body>\n" +
                "<div class=\"ads-in-post hide_if_width_less_800\">\n" +
                "\n" +
                "<script type=\"text/javascript\" src=\"/test/_cf/"+properties.getProperty("hash")+"/jsBundles/bundle1.min.js\">\n" +
                "\n" +
                "\n" +
                "</div><h2>Hello World!</h2>\n" +
                "</body>\n" +
                "</html>\n" +
                "</script></div></body></html>",source);
    }

}
