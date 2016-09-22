package com.github.vincentrussell.filter.webapp.performance.taglib;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;

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
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(ConfigurationProperties.PROPERTIES_FILE_NAME);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    private String name;
    private String type;

    public void setName(String name) {
        notEmpty(name,"name must not be empty or null");
        this.name = name;
    }

    public void setType(String type) {
        notEmpty(name,"type must not be empty or null");
        notEmpty(type);
        this.type = type;
    }

    public void setType(Type type) {
        notEmpty(name,"type must not be empty or null");
        notNull(type);
        setType(type.toString().toLowerCase());
    }

    public void doTag() throws JspException, IOException  {
        notEmpty(type);
        notEmpty(name);
        final PageContext pageContext = (PageContext) getJspContext();
        final String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        String value = properties.getProperty("bundle."+type+"."+ name +".url");
        if (value==null) {
            throw new JspException("can not find bundle "+ name + " of type " + type);
        }
        getJspContext().getOut().println("<script type='text/javascript' src='"+contextPath+value+"'/>");
    }

    public enum Type {
        CSS,JS;
    }

}