package nars.io.in;

import nars.io.TextPerception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Process input from a Reader interface, which also provides
 * access to URLs and InputStream's
 */
public class ReaderInput extends Input.BufferedInput {

    private final TextPerception perception;
    protected BufferedReader input;

    protected void setInput(BufferedReader input) {
        this.input = input;
    }

    public ReaderInput(TextPerception p) {
        this.perception = p;
    }

    public ReaderInput(TextPerception p, InputStream i) {
        this(p, new BufferedReader(new InputStreamReader(i)));
    }

    public ReaderInput(TextPerception p, URL u) throws IOException {
        this(p, u.openStream());
    }

    public ReaderInput(TextPerception p, BufferedReader input) {
        this(p);

        setInput(input);

    }

    @Override
    public void stop() {
        if (input != null) {
            try {
                input.close();
                input = null;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected String readAll() throws IOException {
        return input.lines().collect(Collectors.joining("\n"));
    }

}
