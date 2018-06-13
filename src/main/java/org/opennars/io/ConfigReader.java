package org.opennars.io;

import org.opennars.interfaces.Pluggable;
import org.opennars.main.NarParameters;
import org.opennars.main.Parameters;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class ConfigReader {
    public static void loadFrom(final String filepath, final Pluggable reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(new File(filepath), reasoner, parameters);
    }

    public static void loadFrom(final File file, final Pluggable reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(file, reasoner, parameters);
    }

    private static void loadFromImpl(final File file, final Pluggable reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(file);




        final NodeList config = document.getElementsByTagName("config").item(0).getChildNodes();

        for (int iterationConfigIdx = 0; iterationConfigIdx < config.getLength(); iterationConfigIdx++) {
            final Node iConfig = config.item(iterationConfigIdx);

            if (iConfig.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            final String nodeName = iConfig.getNodeName();
            if( nodeName.equals("plugins") ) {
                final NodeList plugins = iConfig.getChildNodes();

                for (int iterationPluginIdx = 0; iterationPluginIdx < config.getLength(); iterationPluginIdx++) {
                    final Node iPlugin = plugins.item(iterationPluginIdx);

                    if (iPlugin.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String pluginClassPath = iPlugin.getAttributes().getNamedItem("classpath").getNodeValue();
                    final String arg0 = iPlugin.getAttributes().getNamedItem("arg0").getNodeValue();
                    final String arg1 = iPlugin.getAttributes().getNamedItem("arg1").getNodeValue();
                    final String arg2 = iPlugin.getAttributes().getNamedItem("arg2").getNodeValue();
                    final String arg3 = iPlugin.getAttributes().getNamedItem("arg3").getNodeValue();
                    final String arg4 = iPlugin.getAttributes().getNamedItem("arg4").getNodeValue();

                    Plugin createdPlugin = createPluginByClassname(pluginClassPath, arg0, arg1, arg2, arg3, arg4);
                    reasoner.addPlugin(createdPlugin);
                }
            }
            else {

                final String propertyName = iConfig.getAttributes().getNamedItem("name").getNodeValue();
                final String propertyValueAsString = iConfig.getAttributes().getNamedItem("value").getNodeValue();

                boolean wasConfigValueAssigned = false;

                try {
                    final Field fieldOfProperty = NarParameters.class.getField(propertyName);

                    if (fieldOfProperty.getType() == int.class) {
                        fieldOfProperty.set(parameters, Integer.parseInt(propertyValueAsString));
                    } else if (fieldOfProperty.getType() == float.class) {
                        fieldOfProperty.set(parameters, Float.parseFloat(propertyValueAsString));
                    } else {
                        throw new ParseException("Unknown type", 0);
                    }

                    wasConfigValueAssigned = true;
                } catch (NoSuchFieldException e) {
                    // ignore
                }

                if (!wasConfigValueAssigned) {
                    try {
                        final Field fieldOfProperty = Parameters.class.getDeclaredField(propertyName);

                        if (fieldOfProperty.getType() == int.class) {
                            fieldOfProperty.set(parameters, Integer.parseInt(propertyValueAsString));
                        } else if (fieldOfProperty.getType() == float.class) {
                            fieldOfProperty.set(parameters, Float.parseFloat(propertyValueAsString));
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
    }

    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, SAXException, IllegalAccessException, ParseException, ClassNotFoundException {
        NarParameters params = new NarParameters();

        loadFrom("../opennars/src/main/config/defaultConfig.xml", null, params);
    }

    /**
     *
     * @param className classpath/classname of the class from which a instance should get constructed
     * @param arg0 argument which is passed to the ctor, can be null if no string should be passed to the ctor
     * @param arg1 argument which is passed to the ctor, can be null if no string should be passed to the ctor
     * @param arg2 argument which is passed to the ctor, can be null if no string should be passed to the ctor
     * @param arg3 argument which is passed to the ctor, can be null if no string should be passed to the ctor
     * @param arg4 argument which is passed to the ctor, can be null if no string should be passed to the ctor
     * @return constructed plugin/operator/etc
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private static Plugin createPluginByClassname(final String className, final String arg0, final String arg1, final String arg2, final String arg3, final String arg4) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Class c = Class.forName(className);

        Plugin createdPlugin;

        if (arg4 != null) {
            createdPlugin = (Plugin)c.getConstructor(String.class, String.class, String.class, String.class, String.class).newInstance(arg0, arg1, arg2, arg3, arg4);
        }
        else if (arg3 != null) {
            createdPlugin = (Plugin)c.getConstructor(String.class, String.class, String.class, String.class).newInstance(arg0, arg1, arg2, arg3);
        }
        else if (arg2 != null) {
            createdPlugin = (Plugin)c.getConstructor(String.class, String.class, String.class).newInstance(arg0, arg1, arg2);
        }
        else if (arg1 != null) {
            createdPlugin = (Plugin)c.getConstructor(String.class, String.class).newInstance(arg0, arg1);
        }
        else if (arg0 != null) {
            createdPlugin = (Plugin)c.getConstructor(String.class).newInstance(arg0);
        }
        else {
            createdPlugin = (Plugin)c.getConstructor().newInstance();
        }

        return createdPlugin;
    }
}
