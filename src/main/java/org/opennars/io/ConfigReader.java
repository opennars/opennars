package org.opennars.io;

import org.opennars.interfaces.pub.Reasoner;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader {
    public static void loadFrom(final String filepath, final Reasoner reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(new File(filepath), reasoner, parameters);
    }

    public static void loadFrom(final File file, final Reasoner reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(file, reasoner, parameters);
    }

    private static void loadFromImpl(final File file, final Reasoner reasoner, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
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

                    final NodeList arguments = iPlugin.getChildNodes();

                    Plugin createdPlugin = createPluginByClassnameAndArguments(pluginClassPath, arguments, reasoner);
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
            else if (typeString.equals("java.lang.String.class")) {
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

    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, SAXException, IllegalAccessException, ParseException, ClassNotFoundException {
        NarParameters params = new NarParameters();

        loadFrom("../opennars/src/main/config/defaultConfig.xml", null, params);
    }
}
