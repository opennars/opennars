/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.io;

import com.google.common.io.Resources;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.main.Parameters;
import org.opennars.main.MiscFlags;
import org.opennars.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to read and parse the XML configuration file
 *
 * @author Robert Wünsche
 */
public class ConfigReader {

    public static List<Plugin> loadParamsFromFileAndReturnPlugins(final String filepath, final Reasoner reasoner, final Parameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        
        System.out.println("Got relative path for loading the config: " + filepath);
        List<Plugin> ret = new ArrayList<Plugin>();
        File file = new File(filepath);

        InputStream stream = null;
        // if this failed, then load from resources
        if(!file.exists()) {
            file = null;
            URL n = Resources.getResource("config/defaultConfig.xml");
            //System.out.println(n.toURI().toString());
            URLConnection connection = n.openConnection();
            stream = connection.getInputStream();
            System.out.println("Loading config " + "config/defaultConfig.xml" +" from resources");
        } else {
            System.out.println("Loading config " + file.getName() +" from file");
        }
        
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = stream != null ? documentBuilder.parse(stream) : documentBuilder.parse(file);
        final NodeList config = document.getElementsByTagName("config").item(0).getChildNodes();

        for (int iterationConfigIdx = 0; iterationConfigIdx < config.getLength(); iterationConfigIdx++) {
            final Node iConfig = config.item(iterationConfigIdx);

            if (iConfig.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            final String nodeName = iConfig.getNodeName();
            if( nodeName.equals("plugins") ) {
                final NodeList plugins = iConfig.getChildNodes();

                for (int iterationPluginIdx = 0; iterationPluginIdx < plugins.getLength(); iterationPluginIdx++) {
                    final Node iPlugin = plugins.item(iterationPluginIdx);

                    if (iPlugin.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String pluginClassPath = iPlugin.getAttributes().getNamedItem("classpath").getNodeValue();

                    final NodeList arguments = iPlugin.getChildNodes();

                    Plugin createdPlugin = createPluginByClassnameAndArguments(pluginClassPath, arguments, reasoner);
                    ret.add(createdPlugin);
                }
            }
            else {

                final String propertyName = iConfig.getAttributes().getNamedItem("name").getNodeValue();
                final String propertyValueAsString = iConfig.getAttributes().getNamedItem("value").getNodeValue();

                boolean wasConfigValueAssigned = false;

                try {
                    final Field fieldOfProperty = Parameters.class.getField(propertyName);

                    if (fieldOfProperty.getType() == int.class) {
                        fieldOfProperty.set(parameters, Integer.parseInt(propertyValueAsString));
                    } else if (fieldOfProperty.getType() == float.class) {
                        fieldOfProperty.set(parameters, Float.parseFloat(propertyValueAsString));
                    } else if (fieldOfProperty.getType() == double.class) {
                        fieldOfProperty.set(parameters, Double.parseDouble(propertyValueAsString));
                    } else if (fieldOfProperty.getType() == boolean.class) {
                        fieldOfProperty.set(parameters, Boolean.parseBoolean(propertyValueAsString));
                    } else {
                        throw new ParseException("Unknown type", 0);
                    }

                    wasConfigValueAssigned = true;
                } catch (NoSuchFieldException e) {
                    // ignore
                }

                if (!wasConfigValueAssigned) {
                    try {
                        final Field fieldOfProperty = MiscFlags.class.getDeclaredField(propertyName);

                        if (fieldOfProperty.getType() == int.class) {
                            fieldOfProperty.set(null, Integer.parseInt(propertyValueAsString));
                        } else if (fieldOfProperty.getType() == float.class) {
                            fieldOfProperty.set(null, Float.parseFloat(propertyValueAsString));
                        } else {
                            throw new ParseException("Unknown type", 0);
                        }

                        wasConfigValueAssigned = true;
                    } catch (NoSuchFieldException e) {
                        // ignore 
                    }
                }
            }
        }
        return ret;
    }

    private static Plugin createPluginByClassnameAndArguments(String pluginClassPath, NodeList arguments, Reasoner reasoner) throws ParseException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Class> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (int parameterIdx = 0; parameterIdx < arguments.getLength(); parameterIdx++) {
            final Node iParameter = arguments.item(parameterIdx);

            if (iParameter.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String typeString = null;
            String valueString = null;
            final boolean specialIsReasoner = iParameter.getAttributes().getNamedItem("isReasoner") != null;

            if (!specialIsReasoner) {
                typeString = iParameter.getAttributes().getNamedItem("type").getNodeValue();
                valueString = iParameter.getAttributes().getNamedItem("value").getNodeValue();
            }

            if (specialIsReasoner) {
                types.add(Reasoner.class);
                values.add(reasoner);
            }
            else if (typeString.equals("int.class")) {
                types.add(int.class);
                values.add(Integer.parseInt(valueString));
            }
            else if (typeString.equals("float.class")) {
                types.add(float.class);
                values.add(Float.parseFloat(valueString));
            }
            else if (typeString.equals("boolean.class")) {
                types.add(boolean.class);
                values.add(Boolean.parseBoolean(valueString));
            }
            else if (typeString.equals("String.class")) {
                types.add(java.lang.String.class);
                values.add(valueString);
            }
            else {
                throw new ParseException("Unknown type", 0);
            }
        }

        Class[] typesAsArr = types.toArray(new Class[types.size()]);
        Object[] valuesAsArr = values.toArray(new Object[values.size()]);

        Class c = Class.forName(pluginClassPath);

        Plugin createdPlugin = (Plugin)c.getConstructor(typesAsArr).newInstance(valuesAsArr);
        return createdPlugin;
    }
}
