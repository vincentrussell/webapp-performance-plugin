package com.github.vincentrussell.filter.webapp.performance.taglib;

import com.mockrunner.mock.web.MockJspFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.*;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class BundleRenderTagTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    MockServletContext mockServletContext;
    MockPageContext mockPageContext;
    BundleRenderTag bundleRenderTag;
    MockHttpServletRequest  mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;

    MockJspWriter mockJspWriter;
    MockJspFragment mockJspFramgment;
    StringWriter stringWriter;

    @Before
    public void before() {
        mockHttpServletRequest = new MockHttpServletRequest();
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
    }

    @Test
    public void emptyBundleName() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name must not be empty or null");
        bundleRenderTag.setName("");
    }

    @Test
    public void nullBundleName() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("name must not be empty or null");
        bundleRenderTag.setName(null);
    }

    @Test
    public void emptyType() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("type must not be empty or null");
        bundleRenderTag.setType("");
    }

    @Test
    public void nullType() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("type must not be empty or null");
        bundleRenderTag.setType((String)null);
    }

    @Test
    public void nullType2() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("type must not be empty or null");
        bundleRenderTag.setType((BundleRenderTag.Type)null);
    }

    @Test
    public void typeCss() throws IOException, JspException {
        bundleRenderTag.setName("bundle2");
        bundleRenderTag.setType(BundleRenderTag.Type.CSS);
        bundleRenderTag.doTag();
        assertEquals("<script type='text/javascript' src='/website/_cf/07b835d14e6cd84cbeafc91f9f57993c/cssBundles/bundle2.min.css'/>\n",mockHttpServletResponse.getContentAsString());
    }

    @Test
    public void typeJs() throws IOException, JspException {
        bundleRenderTag.setName("bundle1");
        bundleRenderTag.setType(BundleRenderTag.Type.JS);
        bundleRenderTag.doTag();
        assertEquals("<script type='text/javascript' src='/website/_cf/07b835d14e6cd84cbeafc91f9f57993c/jsBundles/bundle1.min.js'/>\n",mockHttpServletResponse.getContentAsString());
    }

}
