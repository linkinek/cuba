/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.02.2009 16:19:06
 *
 * $Id$
 */
package com.haulmont.cuba.gui.config;

import com.haulmont.cuba.core.global.ScriptingProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import com.haulmont.cuba.gui.NoSuchScreenException;

/**
 * GenericUI class holding information about all registered screens.
 * <br>Reference can be obtained via {@link com.haulmont.cuba.gui.AppConfig#getWindowConfig()}
 */
public class WindowConfig
{
    protected Map<String, WindowInfo> screens = new HashMap<String, WindowInfo>();

    private static Log log = LogFactory.getLog(WindowConfig.class);

    public void loadConfig(Element rootElem) {
        for (Element element : (List<Element>) rootElem.elements("include")) {
            String fileName = element.attributeValue("file");
            if (!StringUtils.isBlank(fileName)) {
                String incXml = ScriptingProvider.getResourceAsString(fileName);
                if (incXml == null) {
                    log.warn("File " + fileName + " not found, ignore it");
                    continue;
                }
                loadConfig(incXml);
            }
        }
        for (Element element : (List<Element>) rootElem.elements("screen")) {
            String id = element.attributeValue("id");
            if (StringUtils.isBlank(id)) {
                log.warn("Invalid window config: 'id' attribute not defined");
                continue;
            }
            WindowInfo windowInfo = new WindowInfo(id, element);
            screens.put(id, windowInfo);
        }
    }

    public void loadConfig(InputStream stream) {
        Document doc;
        try {
            SAXReader reader = new SAXReader();
            doc = reader.read(stream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        loadConfig(doc.getRootElement());
    }

    public void loadConfig(String xml) {
        Document doc;
        try {
            SAXReader reader = new SAXReader();
            doc = reader.read(new StringReader(xml));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        loadConfig(doc.getRootElement());
    }

    /**
     * Get screen information by screen ID.
     * Can be overridden for specific client type.
     * @param id screen ID as set up in <code>screen-config.xml</code>
     * @throws NoSuchScreenException if the screen with specified ID is not registered
     */
    public WindowInfo getWindowInfo(String id) {
        WindowInfo windowInfo = screens.get(id);
        if (windowInfo == null)
            throw new NoSuchScreenException("Screen '" + id + "' is not defined");
        return windowInfo;
    }

    /**
     * All registered screens
     */
    public Collection<WindowInfo> getWindows() {
        return screens.values();
    }
}
