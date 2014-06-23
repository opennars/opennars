package nars.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import nars.web.HTTPServer;

/*
http://nlp.stanford.edu/software/corenlp.shtml#Download
*/
public class CoreNLPServer extends HTTPServer {
    private final Process p;
/*
#!/bin/sh
cd nlp
~/jdk/bin/java -classpath "./*" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "typedDependencies" -sentences newline edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz -


# FULL PARSER:
#!/bin/sh
#cd nlp
#~/jdk/bin/java -classpath "./*" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "penn,typedDependencies" -sentences newline edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz -
*/
    private final BufferedWriter pout;
    private final BufferedReader pin;

    public CoreNLPServer(String javaPath, String corenlpPath, int port) throws IOException, InterruptedException {
        super(port);
        
        String command=
                javaPath + "java -classpath ./* edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat typedDependencies -sentences newline edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz -";
        
        List<String> cmds = Arrays.asList(command.split(" "));
        
        ProcessBuilder pb = new ProcessBuilder(cmds)
                .directory(new File(corenlpPath));
        
        /*Map<String, String> env = pb.environment();
        env.put("VAR1", "myValue");
        env.remove("OTHERVAR");
        env.put("VAR2", env.get("VAR1") + "suffix");*/
                        
        pb.redirectErrorStream(true);
                
        p = pb.start();             
        pout = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        //Get all initial output of CoreNLP
        String line;
        while ((line = pin.readLine())!=null) {
            if (line.equals("Parsing file: -"))
                break;
        }
        
        System.out.println("Ready, on port " + port);
        
        p.waitFor();
        
    }   
    
    protected synchronized String _parse(String input) throws IOException {
        pout.write(input + "\n");
        pout.flush();
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = pin.readLine())!=null) {
            line = line.trim();
            if (line.length() == 0)
                break;
            response.append(line + "\n");              
        }
                
        return response.toString();    
    }
    
    public static void main(String[] args) throws Exception {
        
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("./nlp <port> <corenlp_Path> [java_Path]");
            return;
        }

        
        String javaPath = args.length == 3 ? args[2] : "";
        
        
        String corenlpPath = args[1]; //"/home/me/stanford-corenlp-full-2014-06-16";
        int port = Integer.parseInt(args[0]);

        new CoreNLPServer(javaPath, corenlpPath, port);
                
        /*
        Thread.sleep(5000);
        
        CoreNLPClient client = new CoreNLPClient("localhost", port);
        String response = client.parse("this is a sentence & it looks like this.");
        
        System.out.println(response);
        
        CoreNLPClient client2 = new CoreNLPClient("localhost", port);
        String response2 = client2.parse("another client writes a different sentence.");
        
        System.out.println(response2);          
                
        
        while (true) {
            Thread.sleep(5000);
        }
        */
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms) {
        
        //System.out.println(header);
        //System.out.println(parms);
        
        String msg = "";
        if (uri.equals("/parse")) {        
            try {
                msg = parms.getProperty("content");
                msg = _parse(msg);
            } catch (IOException ex) {
                msg = "ERROR\n" + ex.toString();
            }
        }
        
        
        Response r = new Response(HTTP_OK, MIME_PLAINTEXT, 
                new ByteArrayInputStream(msg.getBytes()));

        r.addHeader("Content-length", "" + (msg.length()));

        //System.out.println("Response: " + r);
        
        return r;
    }
    
}
