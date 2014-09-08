package nars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utilities {
    static public String readStringFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        byte[] byteChunk = new byte[4096];
        
        try {
            int n;
            
            while ( (n = inputStream.read(byteChunk)) > 0 ) {
                byteOutputStream.write(byteChunk, 0, n);
            }
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
        return byteOutputStream.toString();
    }
}
