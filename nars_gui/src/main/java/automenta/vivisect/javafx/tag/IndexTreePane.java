/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;

import java.util.ArrayList;




/**
 *
 * @author me
 */
public class IndexTreePane extends BorderPane  {
    private final TreeView<Object> tv;
    //http://docs.oracle.com/javafx/2/ui_controls/tree-view.htm
    private final TreeItem root;
    private final TaggerPane.TagReceiver tagger;
    private final SubjectSelect subjectSelect;

    public IndexTreePane(TaggerPane.TagReceiver tagger) {
        super();
        
        this.tagger = tagger;
        
        root = new TreeItem();        
                
        tv = new TreeView(root);
        
        tv.setCellFactory(new Callback<TreeView<Object>,TreeCell<Object>>(){
            @Override
            public TreeCell<Object> call(TreeView<Object> p) {
                return new TextFieldTreeCellImpl();
            }
        });
        
        tv.setShowRoot(false);
        tv.setEditable(false);

        tv.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override public void handle(MouseEvent mouseEvent)    {            
                if(mouseEvent.getClickCount() == 2) {
                    TreeItem<Object> selected = tv.getSelectionModel().getSelectedItem();
                    if (selected!=null) {
                        Object item = selected.getValue();
                        onDoubleClick(item);
                    }
                }
            }
        });
        
        visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (isVisible()) {
                    addHandlers();
                    update();
                }
                else {
                    //core.off(SaveEvent.class, IndexTreePane.this);
                    //core.off(NetworkUpdateEvent.class, IndexTreePane.this);
                }
            }
        });
    

         

        ArrayList users = new ArrayList();
        users.add("Anonymous");
        
        FlowPane menup = new FlowPane();
        subjectSelect = new SubjectSelect(users);
        subjectSelect.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override public void changed(ObservableValue ov, Object t, Object t1) {
                update();
            }
        });
        
        menup.getChildren().add(subjectSelect);
        setTop(menup);
        
        
        ScrollPane sp = new ScrollPane(tv);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);        
        setCenter(sp);
        
        addHandlers();
        update();
        
    }

    protected void addHandlers() {
        //core.on(SaveEvent.class, IndexTreePane.this);
        //core.on(NetworkUpdateEvent.class, IndexTreePane.this);
    }
    
    public void event(Object event) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                update();
            }            
        });
    }             
    
    
    protected void update() {
                
//        root.getChildren().clear();
//        for (Object t : core.getTagRoots()) {
//            root.getChildren().add(newTagTree((NTag)t));
//        }
    }
    
    protected TreeItem newTagTree(/*final NTag t*/) {
//        Object subjectFilter = subjectSelect.getSelectionModel().getSelectedItem();
//
//        TreeItem<Object> i = new TreeItem(t);
//
//        //add instances of the tag
//        addAll(i.getChildren(), transform(core.tagged(t.id, subjectFilter!=null ? subjectFilter.author : null), new Function<Object,TreeItem<Object>>() {
//            @Override public TreeItem apply(final Object f) {
//                return newInstanceItem(f);
//            }
//        }));
//
//        return i;
        return null;
    }
    
    protected TreeItem<Object> newInstanceItem(Object f) {
        TreeItem<Object> t = new TreeItem<Object>(f);        
        return t;
    }
    
    protected void onDoubleClick(Object item) {
//        if (tagger!=null) {
//            tagger.onTagSelected(item.id);
//        }
    }
    

    private final class TextFieldTreeCellImpl extends TreeCell<Object> {
 
 
        public TextFieldTreeCellImpl() {
            super();
        }
 
 
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            setItem(item);
            
            if (item!=null) {
                setText(null);
                
                BorderPane g = new BorderPane();
                                
                Label tl = new Label(item.toString());
                Hyperlink tb = new Hyperlink("[+]");                
                g.setTop(new FlowPane(tl, tb));
                
                BorderPane content = new BorderPane();
                
                tb.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent t) {                        
                        if (g.getBottom()==null) {

                            Slider slider = new Slider();
                            slider.setMin(0);
                            slider.setMax(100);
                            slider.setValue(40);
                            slider.setShowTickLabels(true);
                            slider.setShowTickMarks(true);
                            slider.setMajorTickUnit(50);
                            slider.setMinorTickCount(5);
                            slider.setBlockIncrement(10);

                            content.setCenter(slider);
                            g.setBottom(content);
                        }
                        else {
                            g.setBottom(null);
                        }
                    }
                });
                
                setGraphic(g);
            }
            

        }
 
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
     
}
