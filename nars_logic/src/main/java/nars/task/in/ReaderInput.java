package nars.task.in;

import nars.NAR;
import nars.task.flow.TaskQueue;

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
public class ReaderInput extends TaskQueue {

    public final NAR nar;
    protected BufferedReader input;

    protected void setInput(BufferedReader input) {
        this.input = input;
    }

    public ReaderInput(NAR nar) {
        this.nar = nar;
    }

    public ReaderInput(NAR nar, InputStream i) {

        this(nar, new BufferedReader(new InputStreamReader(i)));
    }

    public ReaderInput(NAR nar, URL u) throws IOException {
        this(nar, u.openStream());
    }

    public ReaderInput(NAR nar, BufferedReader input) {
        this(nar);

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

    protected String readAll() {
        return input.lines().collect(Collectors.joining("\n"));
    }

}
