package nars.guifx;

import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.util.ArrayList;

 
public class HTMLBrowser extends Application {
    
    int minTextLength = 3;
    @SuppressWarnings("HardcodedFileSeparator")
    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
 
        WebView view = new WebView();
        WebEngine engine = view.getEngine();
        engine.load("http://news.google.com");
        root.getChildren().add(view);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        
          engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
              if (newState == State.SUCCEEDED) {

                  /*
                  Document doc = engine.getDocument();
                  try {
                      Transformer transformer = TransformerFactory.newInstance().newTransformer();
                      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");


                      transformer.transform(new DOMSource(doc),
                              new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
                  } catch (Exception ex) {
                      ex.printStackTrace();
                  }*/


                      String detect =
                      "function deepText(node){"+
                          "var A= [];"+
                          "if(node){"+
                          "node= node.firstChild;"+
                          "while(node!= null){"+
                          "if ((node.nodeType== 3) && (node.textContent.trim().length > 0)) A[A.length]=node;"+
                          "else A = A.concat(deepText(node));"+
                          "node= node.nextSibling;"+
                          "} } return A; } deepText(document.body).map(function(e) { "+
                      "var rr = e.parentNode.getClientRects(); var rect = rr[0]; if (rr.length > 1) for (var i=1; i < rr.length; i++) { var ss = rr[i]; if (ss.left < rect.left) rect.left = ss.left; if (ss.top < rect.top) rect.top = ss.top; if (ss.right > rect.right) rect.right= ss.right; if (ss.bottom > rect.bottom) rect.bottom= ss.bottom; }  return [ e.textContent.trim(), rect!=null ? rect.right - rect.left : 0, rect!=null ? rect.bottom - rect.top : 0 ];  }); ";
                      //wholeText,length,data,previousSibling,parentNode,lastChild,baseURI,firstChild,nodeValue,textContent,nodeType,nodeName,prefix,childNodes,nextSibling,ownerDocument,namespaceURI,localName,parentElement


                      JSObject result = (JSObject) engine.executeScript(detect);
                      int length = (int)result.getMember("length");
                      for (int i = 0; i < length; i++) {
                          JSObject r = (JSObject)result.getSlot(i);

                          System.out.println(r);

                          String text = r.getSlot(0).toString();
                          int width = (Integer)r.getSlot(1);
                          int height = (Integer)r.getSlot(2);

                          if (text.length() < minTextLength)
                              continue;

                          System.out.println(text + ' ' + width + 'x' + height + '=' + (width*height) + " @ " + ((width*height)/text.length()) + "/c"  );

                      }


              }
          });


        
    }
 
    
    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}