package nars.io.in;

import com.google.common.io.Files;
import nars.io.TextPerception;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
    Given a filepath, loads the file as task input
 */
public class FileInput extends TextInput {


    public FileInput(TextPerception p, File input) throws IOException {
        super(p, load(input));
    }

    public static String load(String path) throws IOException {
        return load(new File(path));
    }

    private static String load(File file) throws IOException {
        return Files.toString(file, Charset.defaultCharset());
    }


}
