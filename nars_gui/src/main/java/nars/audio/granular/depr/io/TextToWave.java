package nars.audio.granular.depr.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author David Nadeau
 */
public enum TextToWave {
    ;

    public static ArrayList readTxt(String filePath) throws
            IOException {
        ArrayList l = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            l = new ArrayList();
            String line = br.readLine();

            while (line != null) {
                l.add(line);
                line = br.readLine();
            }
        }
        return l;
    }

    public static int[] parseHeader(ArrayList<String> l) {
        int[] headers = new int[5];
        String row = l.get(0);
        headers[0] = Integer.parseInt(row.substring(row.indexOf('\t') + 1, row
                .length()));
        row = l.get(1);
        headers[1] = Integer.parseInt(row.substring(row.indexOf('\t') + 1, row
                .length()));
        row = l.get(2);
        headers[2] = Integer.parseInt(row.substring(row.indexOf('\t') + 1, row
                .length()));
        row = l.get(3);
        headers[3] = Integer.parseInt(row.substring(row.indexOf('\t') + 1, row
                .length()));
        headers[4] = 440;
        return headers;
    }

}
