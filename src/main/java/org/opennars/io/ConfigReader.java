package org.opennars.io;

import org.opennars.main.NarParameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    public static void loadFrom(String filepath, NarParameters parameters) throws IOException {
        loadFromImpl(new FileInputStream(filepath), parameters);
    }

    public static void loadFrom(FileInputStream fileStream, NarParameters parameters) throws IOException {
        loadFromImpl(fileStream, parameters);
    }

    private static void loadFromImpl(FileInputStream fileStream, NarParameters parameters) throws IOException {
        Properties prop = new Properties();
        prop.load(fileStream);

        // transfer
        parameters.CONCEPT_BAG_SIZE = Integer.parseInt(prop.get("conceptBagSize").toString());
        parameters.DECISION_THRESHOLD = Float.parseFloat(prop.get("decisionThreshold").toString());
        parameters.CONCEPT_BAG_LEVELS = Integer.parseInt(prop.get("conceptBagLevels").toString());
        parameters.NOVELTY_HORIZON = Integer.parseInt(prop.get("noveltyHorizon").toString());
    }
}
