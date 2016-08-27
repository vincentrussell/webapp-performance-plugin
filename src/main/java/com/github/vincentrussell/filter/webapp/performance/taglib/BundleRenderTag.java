package com.github.vincentrussell.filter.webapp.performance.taglib;

import netscape.javascript.JSException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

public class BundleRenderTag extends SimpleTagSupport {

    private static Properties properties = new Properties();

    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/WebappPerformanceConfig.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    private String bundleName;
    private String type;

    public void setBundleName(String bundleName) {
        notEmpty(bundleName,"bundleName must not be empty or null");
        this.bundleName = bundleName;
    }

    public void setType(String type) {
        notEmpty(bundleName,"type must not be empty or null");
        notEmpty(type);
        this.type = type;
    }

    public void setType(Type type) {
        notEmpty(bundleName,"type must not be empty or null");
        notNull(type);
        setType(type.toString().toLowerCase());
    }

    public void doTag() throws JspException, IOException  {
        notEmpty(type);
        notEmpty(bundleName);
        final PageContext pageContext = (PageContext) getJspContext();
        final String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        String value = properties.getProperty("bundle."+type+"."+bundleName+".url");
        if (value==null) {
            throw new JspException("can not find bundle "+bundleName+ " of type " + type);
        }
        getJspContext().getOut().println("<script type='text/javascript' src='"+contextPath+value+"'/>");
    }

    public enum Type {
        CSS,JS;
    }

}