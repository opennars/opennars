package nars.web;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.codeserver.Options;

import java.io.IOException;

/**
 * Created by me on 7/22/15.
 */
public class GWTTest {

    public static void main(String[] args) throws IOException, UnableToCompleteException {
        //http://www.gwtproject.org/articles/superdevmode.html
        Options o = new Options();
        o.parseArgs(
                new String[] {  "nars.web.client"                }
        );

        com.google.gwt.dev.codeserver.CodeServer.start( o );
    }
}
