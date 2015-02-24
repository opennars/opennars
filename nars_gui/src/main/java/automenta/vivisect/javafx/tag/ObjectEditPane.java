/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;


import com.sun.javafx.scene.web.skin.HTMLEditorSkin;
import com.sun.webkit.WebPage;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

/**
 *
 * @author me
 */
public class ObjectEditPane extends BorderPane implements TaggerPane.TagReceiver {

    private final HTMLEditor editor;
    private final Pane menuTop;
    private final TaggerPane tagger;
    private final SplitPane editCenter;
    private final ScrollPane editPane;
    final static Font BigFont = new Font(Font.getDefault().getSize()*1.5f);
    private final HTMLEditorSkin editorSkin;
    private WebView webview;
    private WebPage webpage;

    public Document htmlToNode(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public ObjectEditPane(Object n) {
        super();                       
        
        editor = new HTMLEditor();        
        editorSkin = ((HTMLEditorSkin)editor.getSkin());
        
        try {
            Field f = editorSkin.getClass().getDeclaredField("webView"); f.setAccessible(true);
            webview = (WebView)f.get(editorSkin);
            Field g = editorSkin.getClass().getDeclaredField("webPage"); g.setAccessible(true);
            webpage = (WebPage)g.get(editorSkin);
        }
        catch (Exception e) { 
            System.err.println(e);
        }
        
        editor.setHtmlText(toHTML(n));
        


        String name = n.toString();
        if (name == null) name = "";
        
        
        /*
        Button minifyButton = new Button("v");
        minifyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
               //ObjectEditPane.this.getCenter().setVisible();
                ObjectEditPane.this.setCenter(editCenter);
                menuTop.getChildren().remove(minifyButton);
                ObjectEditPane.this.autosize();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ObjectEditPane.this.requestParentLayout();
                        ObjectEditPane.this.layout();
                        ObjectEditPane.this.resize(ObjectEditPane.this.getWidth(), ObjectEditPane.this.getHeight());
                    }                    
                });
            }
        });*/
        
        
        ToggleButton focusButton = new ToggleButton();
        AwesomeDude.setIcon(focusButton, AwesomeIcon.STAR);
                
        
        
        TextField nameArea = new TextField(name);    
        nameArea.setPromptText("Title");
        nameArea.setFont(BigFont);
        focusButton.setFont(BigFont);
        
        menuTop = new BorderPane(nameArea, null, null, null, focusButton);
        
        
        
        
        

        SaveObjectPane menuBottom = new SaveObjectPane() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onSave(Scope scope, Object subject) {
            }            
        };
        
        
        menuTop.setPadding(DefaultPadding);
        menuBottom.setPadding(DefaultPadding);
        editor.setPadding(DefaultPadding);
        
        tagger = new TaggerPane(this);
        
        setTop(menuTop);                
        setBottom(menuBottom);        
        
        
        editPane = new ScrollPane(editor);        
        editPane.setFitToWidth(true);
        editPane.setFitToHeight(true);

        
        editCenter = new SplitPane();
        editCenter.getItems().addAll(tagger, editPane );
        editCenter.setDividerPositions(0.3f);
 
        
        
        setCenter(editCenter);

        focusButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                if (focusButton.isSelected()) {
                    tagger.setVisible(true);            
                    editCenter.getItems().clear();
                    editCenter.getItems().addAll(tagger, editPane );
                    editCenter.setDividerPositions(0.3);
                }
                else {
                    disableTagger();
                }
            }

        });
        disableTagger();
        focusButton.setSelected(false);        
        
                
        
    }
    
    private void disableTagger() {
        tagger.setVisible(false);
        editCenter.getItems().clear();
        editCenter.getItems().addAll(editPane );
        editCenter.setDividerPositions(0);
    }
    
    private static final Insets DefaultPadding = new Insets(4,4,4,4);
    

    public static String toHTML(Object n) {
        StringBuilder sb = new StringBuilder();
        /*
        for (final Map.Entry<String, Object> e : n.value.entries()) {
            sb.append(newTagHTML(e.getKey(), e.getValue()));
            
        }
        */
        
        /*sb.append("<hr/>");
        
        String raw = n.toStringDetailed();
        sb.append(raw);*/
        
        return sb.toString();
    }
    
    public static Object fromHTML(String id, String name, String html) {
        /*NObject n = new NObject(name, id);
        return n;*/
        return "x";
    }

    public static String newTagHTML(String key, Object value) {
        String s = "&nbsp;<span><span contenteditable='false' style='-webkit-user-modify: read-only; border: 1px solid gray'>";
        s += key + " = " + value.toString();
        s += "</span></span>&nbsp;";
        return s;
    }

    @Override
    public void onTagSelected(String s) {
        String html = editor.getHtmlText();
        
        html += newTagHTML(s,1.0); 
        
        editor.setHtmlText(html);
        
        /*
        try {
            webview.getEngine().getDocument().getImplementation();
            Text text = webview.getEngine().getDocument().createTextNode(s);
            webview.getEngine().getDocument().appendChild(text);
            //webview.getEngine().getDocument().appendChild(htmlToNode("<b>" + s + "</b>").getDocumentElement().);
        }
        catch(Exception e) { 
            System.err.println(e);
            e.printStackTrace();
        }
        */
    }

}
