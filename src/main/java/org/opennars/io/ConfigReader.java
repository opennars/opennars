package org.opennars.io;

import org.opennars.interfaces.Pluggable;
import org.opennars.main.NarParameters;
import org.opennars.main.Parameters;
import org.opennars.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    public static void loadFrom(final String filepath, final Pluggable pluggable, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(new File(filepath), pluggable, parameters);
    }

    public static void loadFrom(final File file, final Pluggable pluggable, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        loadFromImpl(file, pluggable, parameters);
    }

    private static void loadFromImpl(final File file, final Pluggable pluggable, final NarParameters parameters) throws IOException, IllegalAccessException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(file);

        final Element config = document.getElementById("config");

        // transfer
        Field[] allFields = NarParameters.class.getDeclaredFields();
        for (final Field iField : allFields) {
            String propertyValueAsString = config.getElementsByTagName(iField.getName()).item(0).getNodeValue();
            if (iField.getType() == Integer.class) {
                iField.set(parameters, Integer.parseInt(propertyValueAsString));
            }
            else if (iField.getType() == Float.class) {
                iField.set(parameters, Float.parseFloat(propertyValueAsString));
            }
            else {
                throw new ParseException("Unknown type", 0);
            }
        }


        allFields = Parameters.class.getFields();
        for (Field iField : allFields) {
            String propertyValueAsString = config.getElementsByTagName(iField.getName()).item(0).getNodeValue();
            if (iField.getType() == Integer.class) {
                iField.set(Parameters.class, Integer.parseInt(propertyValueAsString));
            }
            else if (iField.getType() == Float.class) {
                iField.set(Parameters.class, Float.parseFloat(propertyValueAsString));
            }
            else {
                throw new ParseException("Unknown type", 0);
            }
        }

        // create plugins
        final Element plugins = document.getElementById("plugins");

        final NodeList pluginList = plugins.getElementsByTagName("plugin");
        for (int pluginIdx = 0; pluginIdx < pluginList.getLength(); pluginIdx++) {
            Node plugin = pluginList.item(pluginIdx);

            String pluginClassPath = plugin.getNodeValue();
            Plugin createdPlugin = createPluginByClassname(pluginClassPath);
            pluggable.addPlugin(createdPlugin);
        }
    }

    private static Plugin createPluginByClassname(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Class c = Class.forName(className);
        Plugin createdPlugin = (Plugin)c.getConstructor().newInstance();
        return createdPlugin;
    }
}
