package com.github.vincentrussell.filter.webapp.performance;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PhantomTestsIT {

    private static String PHANTOMJS_BINARY;

    WebDriver driver;
    String baseURL;

    @BeforeClass
    public static void beforeTest() {
        PHANTOMJS_BINARY = System.getProperty("phantomjs.binary");

        assertNotNull(PHANTOMJS_BINARY);
        assertTrue(new File(PHANTOMJS_BINARY).exists());
    }

    @Before
    public void before() {
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
        baseURL = "http://localhost:" + port;

        // If the referenced JavaScript files fail to load, the test fails at this point
        driver.navigate().to(baseURL + "/index.jsp");


    }


    @Test
    public void shouldHavePhantomJsBinary() {
        String binary = System.getProperty("phantomjs.binary");
        assertNotNull(binary);
        assertTrue(new File(binary).exists());
        assertTrue(false);
    }

}
