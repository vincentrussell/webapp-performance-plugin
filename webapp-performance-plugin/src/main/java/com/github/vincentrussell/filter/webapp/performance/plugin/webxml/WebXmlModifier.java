package com.github.vincentrussell.filter.webapp.performance.plugin.webxml;

import com.github.vincentrussell.filter.webapp.performance.ConfigurationProperties;
import com.github.vincentrussell.filter.webapp.performance.filter.CacheFilter;
import com.github.vincentrussell.filter.webapp.performance.filter.CompressingFilterConfig;
import com.github.vincentrussell.filter.webapp.performance.filter.CacheFilterConfig;
import com.github.ziplet.filter.compression.CompressingFilter;
import com.google.common.base.Joiner;
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
import java.util.*;

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
    private final CacheFilterConfig cacheFilterConfig;
    private final CompressingFilterConfig compressingFilterConfig;

    public WebXmlModifier(InputStream inputStream) throws IOException {
        this(inputStream,null,null);
    }

    public WebXmlModifier(InputStream inputStream, CacheFilterConfig cacheFilterConfig, CompressingFilterConfig compressingFilterConfig) throws IOException {
        this.cacheFilterConfig = cacheFilterConfig;
        this.compressingFilterConfig = compressingFilterConfig;
        notNull(inputStream,"input stream cannot be null");
        this.inputStream = inputStream;
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setSAXHandlerFactory(FACTORY);
            document = saxBuilder.build(inputStream);
            validateWebXml();
            addCacheFilter();
            addCompressingFilter();
        } catch (JDOMException | IllegalStateException | IOException e) {
            throw new IOException(e);
        }
    }

    private void validateWebXml() {
        if (document.getRootElement()!=null && !"web-app".equals(document.getRootElement().getName())) {
            throw new IllegalStateException("first web-app child element missing.  This is not a valid web.xml document!");
        }
    }

    private void addCompressingFilter() {

        if (compressingFilterConfig==null || !compressingFilterConfig.isConfigured()) {
            return;
        }

        String filterName = "compressingFilter";

        Element rootNode = document.getRootElement();

        List<Element> newElementList = new ArrayList<>();

        List<Element> filterMappingChildren = rootNode.getChildren("filter-mapping");
        List<Element> contextParamChildren = rootNode.getChildren("context-param");
        List<Element> rootChildren = rootNode.getChildren();
        if (filterMappingChildren.size() > 0) {
            int lastFilterMapping = rootChildren.indexOf(filterMappingChildren.get(filterMappingChildren.size()-1));
            addElementsAfterIndex(newElementList, rootChildren, getCompressingFilterElements(filterName), lastFilterMapping);
        } else if (contextParamChildren.size() > 0) {
            int index = rootChildren.indexOf(contextParamChildren.get(contextParamChildren.size()-1));
            addElementsAfterIndex(newElementList, rootChildren, getCompressingFilterElements(filterName), index);
        } else {
            addElementsAfterIndex(newElementList, rootChildren, getCompressingFilterElements(filterName), -1);
        }
        rootChildren.clear();
        rootChildren.addAll(newElementList);
    }

    private void addCacheFilter()  {

        if (cacheFilterConfig ==null || !cacheFilterConfig.isConfigured()) {
            return;
        }

        String filterName = "cacheFilter";

        Element rootNode = document.getRootElement();

        List<Element> newElementList = new ArrayList<>();

        List<Element> contextParamChildren = rootNode.getChildren("context-param");
        List<Element> rootChildren = rootNode.getChildren();
        if (contextParamChildren.size() > 0) {
            int index = rootChildren.indexOf(contextParamChildren.get(contextParamChildren.size()-1));
            addElementsAfterIndex(newElementList, rootChildren, getCacheFilterElements(filterName), index);
        } else {
            addElementsAfterIndex(newElementList, rootChildren, getCacheFilterElements(filterName), -1);
        }
        rootChildren.clear();
        rootChildren.addAll(newElementList);

    }

    private void addElementsAfterIndex(List<Element> newElementList, List<Element> rootChildren, List<Element> elementsToAdd, int index) {
        int rootChildSize = rootChildren.size();
        int endIndex = index == -1 ? 0 : index + 1;
        newElementList.addAll(rootChildren.subList(0, endIndex));
        newElementList.addAll(elementsToAdd);
        newElementList.addAll(rootChildren.subList(endIndex,rootChildSize));
    }


    private List<Element> getCompressingFilterElements(String filterName) {
        List<Element> elements = new LinkedList<>();
        elements.add(element("filter",
                getCompressingFilterWithInitParams(filterName)));

        for (String urlPattern : compressingFilterConfig.getUrlPatterns()) {
            elements.add(element("filter-mapping",
                    element("filter-name",filterName),
                    element("url-pattern",urlPattern)));
        }

        return elements;

    }


    private List<Element> getCacheFilterElements(String filterName) {
        return Arrays.asList(element("filter",
                getCacheFilterWithInitParams(filterName)),
                element("filter-mapping",
                        element("filter-name",filterName),
                        element("url-pattern","/*")));

    }

    private Element[] getCacheFilterWithInitParams(String filterName) {
        List<Element> elements = new ArrayList<>();
        elements.add(element("filter-name",filterName));
        elements.add(element("filter-class",CacheFilter.class.getName()));
        if (cacheFilterConfig !=null) {
            setInitParamValue(elements,ConfigurationProperties.CACHE_PROCESS_CSS, cacheFilterConfig.isShouldProcessCss());
            setInitParamValue(elements, ConfigurationProperties.CACHE_PROCESS_IMAGES, cacheFilterConfig.isShouldProcessImages());
            setInitParamValue(elements,ConfigurationProperties.CACHE_PROCESS_JS, cacheFilterConfig.isShouldProcessJs());
            setInitParamValue(elements,ConfigurationProperties.CACHE_ENABLED, cacheFilterConfig.isEnabled());
            setInitParamValue(elements,ConfigurationProperties.CACHE_EXCLUSIONS, cacheFilterConfig.getExclusions());
            setInitParamValue(elements,ConfigurationProperties.CACHE_EXTENSIONS, cacheFilterConfig.getExtensions());
        }
        return elements.toArray(new Element[elements.size()]);
    }

    private Element[] getCompressingFilterWithInitParams(String filterName) {
        List<Element> elements = new ArrayList<>();
        elements.add(element("filter-name",filterName));
        elements.add(element("filter-class",CompressingFilter.class.getName()));
        if (compressingFilterConfig!=null) {

            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_INCLUDE_PATH_PATTERNS, compressingFilterConfig.getIncludePathPatterns());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_EXCLUDE_PATH_PATTERNS, compressingFilterConfig.getExcludePathPatterns());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_INCLUDE_CONTENT_TYPES, compressingFilterConfig.getIncludeContentTypes());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_EXCLUDE_CONTENT_TYPES, compressingFilterConfig.getExcludeContentTypes());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_INCLUDE_USER_AGENT_PATTERNS, compressingFilterConfig.getIncludeUserAgentPatterns());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_EXCLUDE_USER_AGENT_PATTERNS, compressingFilterConfig.getExcludeUserAgentPatterns());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_NO_VARY_HEADER_PATTERNS, compressingFilterConfig.getNoVaryHeaderPatterns());
            setInitParamValue(elements, ConfigurationProperties.COMPRESSION_COMPRESSION_THRESHOLD, compressingFilterConfig.getCompressionThreshold());
            setInitParamValue(elements,ConfigurationProperties.COMPRESSION_STATS_ENABLED,compressingFilterConfig.isStatsEnabled());
            setInitParamValue(elements,ConfigurationProperties.COMPRESSION_DEBUG,compressingFilterConfig.isDebug());

        }
        return elements.toArray(new Element[elements.size()]);
    }

    private void setInitParamValue(List<Element> elements, String paramName, Collection<String> collectionValue) {
        if (collectionValue==null || collectionValue.size() == 0) {
            return;
        }
        elements.add(element("init-param",
                element("param-name",paramName),
                element("param-value",Joiner.on(ConfigurationProperties.LIST_SEPARATOR).join(collectionValue))));
    }

    private void setInitParamValue(List<Element> elements, String paramName, Object value) {
        if (value==null) {
            return;
        }
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
