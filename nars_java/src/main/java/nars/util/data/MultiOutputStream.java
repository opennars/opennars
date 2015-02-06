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
import java.util.Iterator;
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

        this.out = new ArrayList<OutputStream>();

        for (Iterator<OutputStream> i = outStreams.iterator(); i.hasNext();) {
            OutputStream outputStream = i.next();

            if (outputStream == null) {
                throw new NullPointerException();
            }
            this.out.add(outputStream);
        }
    }

    @Override public void write(final int arg0) throws IOException {
        for (int i=0; i < out.size(); i++) {            
            out.get(i).write(arg0);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i=0; i < out.size(); i++) {            
            out.get(i).write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i=0; i < out.size(); i++) {            
            out.get(i).write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (int i=0; i < out.size(); i++) {            
            out.get(i).close();
        }
    }

    @Override
    public void flush() throws IOException {
        for (int i=0; i < out.size(); i++) {            
            out.get(i).flush();
        }
    }

}
