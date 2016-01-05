package nars.audio.granular.depr.io;

import nars.audio.granular.depr.Wave;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author David Nadeau
 */
public enum WaveToText {
    ;

    @SuppressWarnings("HardcodedFileSeparator")
    public static void writeTxt(Wave w) {
        List<String> l = w.getData();

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(System
                    .getProperty("user.dir") + "/src/music/" + w
                    .getName() + ".txt"));

            for (String s : l) {
                out.write(s);
                out.newLine();
            }

            out.close();
        } catch (IOException e) {
        }

    }

}
