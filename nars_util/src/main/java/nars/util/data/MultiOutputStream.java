/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.data;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
     * @author
     * http://stackoverflow.com/questions/7987395/how-to-write-data-to-two-java-io-outputstream-objects-at-once
     */

public class MultiOutputStream extends OutputStream {

    private final List<OutputStream> out;

    
    public MultiOutputStream(OutputStream... o) {
        this(Lists.newArrayList(o));
    }
    
    public MultiOutputStream(Collection<OutputStream> outStreams) {

        out = new ArrayList<>();

        for (OutputStream outputStream : outStreams) {
            if (outputStream == null) {
                throw new NullPointerException();
            }
            out.add(outputStream);
        }
    }

    @Override public void write(int arg0) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(arg0);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream anOut : out) {
            anOut.close();
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream anOut : out) {
            anOut.flush();
        }
    }

}
