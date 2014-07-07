package nars.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author me
 */
public class CoreNLPClient {
    private final String host;
    private final int port;

    public CoreNLPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String parse(String text) throws IOException {
        
        String type = "application/x-www-form-urlencoded";
        String encodedData = URLEncoder.encode( text, "UTF8" ); 
        
        URL u = new URL("http://" + host + ":" + port + "/parse");
        
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty( "Content-Type", type );
        conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
        
        OutputStream os = conn.getOutputStream();
        os.write(encodedData.getBytes());
        os.flush();
        
        StringBuffer response = new StringBuffer();
        
        BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = is.readLine())!=null) {
            response.append(line + "\n");            
        }
        
        
        return response.toString();
    }
    
}
