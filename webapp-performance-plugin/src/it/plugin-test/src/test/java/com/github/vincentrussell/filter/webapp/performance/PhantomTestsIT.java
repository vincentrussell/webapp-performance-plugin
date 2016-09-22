package com.github.vincentrussell.filter.webapp.performance;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import static org.apache.commons.lang3.Validate.notNull;
import static org.junit.Assert.*;

public class PhantomTestsIT {

    private static String PHANTOMJS_BINARY;
    private static String VERSION;

    private static final long SECONDS_IN_DAY = 60 * 60 * 24;
    private static final long TEN_YEARS_SECONDS = SECONDS_IN_DAY * 365 * 10;
    private static final long TEN_YEARS_MILLIS = TEN_YEARS_SECONDS * 1000;
    private final static DateTimeFormatter FMT = DateTimeFormat.forPattern("EEE, dd MMM YYYY HH:mm:ss zzz");

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

    @Test
    public void cacheControlIndexJsp() throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(baseURL + "/index.jsp");
        HttpResponse response = client.execute(request);
        assertEquals(200,response.getStatusLine().getStatusCode());
    }

    @Test
    public void jsBundleCached() throws IOException {
        testCachebasedOnUrl(properties.getProperty("bundle.js.bundle1.url"));
    }

    @Test
    public void cssBundleCached() throws IOException {
        testCachebasedOnUrl(properties.getProperty("bundle.css.bundle2.url"));
    }

    private void testCachebasedOnUrl(String url) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(baseURL + url);
        HttpResponse response = client.execute(request);
        assertEquals(Arrays.asList(new TestableHeader("Cache-Control","public, max-age="+TEN_YEARS_SECONDS+"")), Arrays.asList(new TestableHeader(response.getHeaders("Cache-Control")[0])));
        Date resultDate = FMT.parseDateTime(new TestableHeader(response.getHeaders("Expires")[0]).getValue()).toDate();
        Date tenYearsFutureDate = new Date(System.currentTimeMillis() + TEN_YEARS_MILLIS);
        int minutes = new Period(new DateTime(resultDate), new DateTime(tenYearsFutureDate)).getMinutes();
        assertTrue(minutes <= 1);
    }


    private static class TestableHeader implements Header {

        private final Header header;

        private TestableHeader(Header header) {
            notNull(header);
            this.header = header;
        }

        public TestableHeader(String name, String value) {
            this(new BasicHeader(name,value));
        }

        @Override
        public String getName() {
            return header.getName();
        }

        @Override
        public String getValue() {
            return header.getValue();
        }

        @Override
        public HeaderElement[] getElements() throws ParseException {
            return header.getElements();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Header rhs = (Header) obj;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(getValue(), rhs.getValue())
                    .append(getElements(), rhs.getElements())
                    .isEquals();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).
                    append("name", getName()).
                    append("value", getValue()).
                    append("elements", getElements()).
                    toString();
        }
    }

}
