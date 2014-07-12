import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
 
 
public class NARSWebView extends Application {
    private Scene scene;
    @Override public void start(Stage stage) {
        Browser b = new Browser();
        
        stage.setTitle("Web View");
        scene = new Scene(b,900,600, Color.web("#666970"));
        stage.setScene(scene);
        //scene.getStylesheets().add("webviewsample/BrowserToolbar.css");        
        stage.show();


        
    }
 
    public static void main(String[] args){
        launch(args);
    }
}
class Browser extends Region {
 
    final public WebView browser = new WebView();
    final public WebEngine webEngine = browser.getEngine();
     
    public Browser() {
        getStyleClass().add("browser");
        
        webEngine.setOnError(new EventHandler<WebErrorEvent>() {
            @Override public void handle(WebErrorEvent t) {
                System.err.println(t);
            }
        });
        webEngine.setJavaScriptEnabled(true);
        
        //new File("nars_web/client/index.html")        
        webEngine.load("file:///home/me/share/nars/nars_web/client/index.html");
        
        //http://docs.oracle.com/javase/8/javafx/api/javafx/scene/web/WebEngine.html
        
        webEngine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 



        getChildren().add(browser);
 
    }
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
 
    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }
 
    @Override protected double computePrefWidth(double height) {
        return 900;
    }
 
    @Override protected double computePrefHeight(double width) {
        return 600;
    }
}