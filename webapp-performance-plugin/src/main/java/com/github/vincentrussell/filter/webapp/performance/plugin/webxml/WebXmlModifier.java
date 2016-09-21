package com.github.vincentrussell.filter.webapp.performance.plugin.webxml;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.filter.CacheFilter;
import com.github.vincentrussell.filter.webapp.performance.filter.FilterCacheConfig;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.JDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXHandler;
import org.jdom2.input.sax.SAXHandlerFactory;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

public class WebXmlModifier {

    private static final SAXHandlerFactory FACTORY = new SAXHandlerFactory() {
        @Override
        public SAXHandler createSAXHandler(JDOMFactory factory) {
            return new SAXHandler() {
                @Override
                public void startElement(
                        String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
                    super.startElement("", localName, qName, atts);
                }
                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {
                    return;
                }
            };
        }
    };


    private final InputStream inputStream;
    private final Document document;
    private final FilterCacheConfig filterCacheConfig;

    public WebXmlModifier(InputStream inputStream) throws IOException {
        this(inputStream,null);
    }

    public WebXmlModifier(InputStream inputStream, FilterCacheConfig filterCacheConfig) throws IOException {
        this.filterCacheConfig = filterCacheConfig;
        notNull(inputStream,"input stream cannot be null");
        this.inputStream = inputStream;
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setSAXHandlerFactory(FACTORY);
            document = saxBuilder.build(inputStream);
            addCacheFilter();
        } catch (JDOMException | IllegalStateException | IOException e) {
            throw new IOException(e);
        }
    }

    private void addCacheFilter()  {

        String cacheFilter = "cacheFilter";

        if (document.getRootElement()!=null && !"web-app".equals(document.getRootElement().getName())) {
            throw new IllegalStateException("first web-app child element missing.  This is not a valid web.xml document!");
        }

        Element rootNode = document.getRootElement();

        List<Element> newElementList = new ArrayList<>();

        List<Element> list = rootNode.getChildren("context-param");
        List<Element> rootChildren = rootNode.getChildren();
        if (list.size() > 0) {
            int index = rootChildren.indexOf(list.get(list.size()-1));
            addElementsAfterIndex(cacheFilter, newElementList, rootChildren, index);
        } else {
            addElementsAfterIndex(cacheFilter, newElementList, rootChildren, -1);
        }
        rootChildren.clear();
        rootChildren.addAll(newElementList);

    }

    private void addElementsAfterIndex(String cacheFilter, List<Element> newElementList, List<Element> rootChildren, int index) {
        int rootChildSize = rootChildren.size();
        int endIndex = index == -1 ? 0 : index + 1;
        newElementList.addAll(rootChildren.subList(0, endIndex));
        newElementList.addAll(getCacheFilterElements(cacheFilter));
        newElementList.addAll(rootChildren.subList(endIndex,rootChildSize));
    }

    private List<Element> getCacheFilterElements(String cacheFilter) {
        return Arrays.asList(element("filter",
                getFilterWithInitParams(cacheFilter)),
                element("filter-mapping",
                        element("filter-name",cacheFilter),
                        element("url-pattern","/*")));

    }

    private Element[] getFilterWithInitParams(String cacheFilter) {
        List<Element> elements = new ArrayList<>();
        elements.add(element("filter-name",cacheFilter));
        elements.add(element("filter-class",CacheFilter.class.getName()));
        if (filterCacheConfig!=null) {
            setInitParamValue(elements,ConfigurationProperties.PROCESS_CSS,filterCacheConfig.isShouldProcessCss());
            setInitParamValue(elements, ConfigurationProperties.PROCESS_IMAGES,filterCacheConfig.isShouldProcessImages());
            setInitParamValue(elements,ConfigurationProperties.PROCESS_JS,filterCacheConfig.isShouldProcessJs());
            setInitParamValue(elements,ConfigurationProperties.ENABLED,filterCacheConfig.isEnabled());
            if (filterCacheConfig.getExclusions()!=null && filterCacheConfig.getExclusions().size() >0) {
                setInitParamValue(elements,ConfigurationProperties.EXCLUSIONS, Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(filterCacheConfig.getExclusions()));
            }
            if (filterCacheConfig.getExtensions()!=null && filterCacheConfig.getExtensions().size() >0) {
                setInitParamValue(elements,ConfigurationProperties.EXTENSIONS, Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(filterCacheConfig.getExtensions()));
            }
        }
        return elements.toArray(new Element[elements.size()]);
    }

    private void setInitParamValue(List<Element> elements, String paramName, Object value) {
        elements.add(element("init-param",
                element("param-name",paramName),
                element("param-value",value.toString())));
    }


    private Element element(String key) {
        return element(key, Collections.emptyList().toArray(new Element[0]));
    }

    private Element element(String key, Element... elements) {
        Element rootNode = new Element(key);
        for (Element node : elements) {
            rootNode.addContent(node);
        }
        return  rootNode;
    }

    private Element element(String key, String value) {
        Element node = new Element(key);
        node.setText(value);
        return node;
    }

    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();

            // display nice nice
            Format format = Format.getPrettyFormat();
            format.setLineSeparator(LineSeparator.UNIX);
            xmlOutput.setFormat(format);
            xmlOutput.output(document, outputStream);
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

}
