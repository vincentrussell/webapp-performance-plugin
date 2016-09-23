package com.github.vincentrussell.filter.webapp.performance.filter;

import com.github.vincentrussell.filter.webapp.performance.taglib.BundleRenderTag;
import com.github.ziplet.filter.compression.CompressingFilter;
import com.mockrunner.mock.web.MockJspFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CompressingFilterTest {

    public static final String REGEX_ALL_PATHS = "\\/.*";
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    MockServletContext mockServletContext;
    MockPageContext mockPageContext;
    BundleRenderTag bundleRenderTag;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;

    MockJspWriter mockJspWriter;
    MockJspFragment mockJspFramgment;
    StringWriter stringWriter;
    CompressingFilter compressingFilter;
    MockFilterChain mockFilterChain;

    @Before
    public void before() {
        mockFilterChain = new MockFilterChain();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI("/theURI");
        mockHttpServletRequest.setContextPath("/website");
        mockHttpServletResponse = new MockHttpServletResponse();
        stringWriter = new StringWriter();
        mockServletContext = new MockServletContext();
        mockJspWriter = new MockJspWriter(stringWriter);
        mockPageContext = new MockPageContext(mockServletContext,mockHttpServletRequest,mockHttpServletResponse);
        mockJspFramgment = new MockJspFragment(mockPageContext);
        bundleRenderTag = new BundleRenderTag();
        bundleRenderTag.setJspContext(mockPageContext);
        bundleRenderTag.setJspBody(mockJspFramgment);
        compressingFilter = new CompressingFilter();
    }

    @Test
    public void compressesAcceptHeader() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Accept-encoding","gzip, deflate");
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig.Builder()
                .setIncludePathPatterns(Arrays.asList(REGEX_ALL_PATHS))
                .setServletContext(mockServletContext)
                .build();
        compressingFilter.init(compressingFilterConfig);
        compressingFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        assertIsCompressed(true);
    }

    @Test
    public void noCompressesMissingAcceptHeader() throws ServletException, IOException {
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig.Builder()
                .setIncludePathPatterns(Arrays.asList(REGEX_ALL_PATHS))
                .setServletContext(mockServletContext)
                .build();
        compressingFilter.init(compressingFilterConfig);
        compressingFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        assertIsCompressed(false);
    }

    @Test
    public void noCompressesNotMatchingContentType() throws ServletException, IOException {
        mockHttpServletRequest.setContentType("application/vnd.amazon.ebook");
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig.Builder()
                .setIncludePathPatterns(Arrays.asList(REGEX_ALL_PATHS))
                .setIncludeContentTypes(Arrays.asList("text/plain"))
                .setServletContext(mockServletContext)
                .build();
        compressingFilter.init(compressingFilterConfig);
        compressingFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        assertIsCompressed(false);
    }

    @Test
    public void noCompressesNotMatchingURI() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Accept-encoding","gzip, deflate");
        mockHttpServletRequest.setRequestURI("/otherUrl/whatever");
        CompressingFilterConfig compressingFilterConfig = new CompressingFilterConfig.Builder()
                .setIncludePathPatterns(Arrays.asList("\\/compressURL\\/.*"))
                .setServletContext(mockServletContext)
                .build();
        compressingFilter.init(compressingFilterConfig);
        compressingFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        assertIsCompressed(false);
    }

    private void assertIsCompressed(boolean isCompressed) {
        if (isCompressed) {
            assertEquals("com.github.ziplet.filter.compression.CompressingHttpServletResponse",mockFilterChain.getResponse().getClass().getName());
        } else {
            assertEquals(MockHttpServletResponse.class, mockFilterChain.getResponse().getClass());
        }
    }

}
