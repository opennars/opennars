//package automenta.vivisect.javafx;
//
//import javafx.application.Application;
//import javafx.beans.binding.Bindings;
//import javafx.beans.binding.StringExpression;
//import javafx.collections.FXCollections;
//import javafx.event.ActionEvent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.KeyCodeCombination;
//import javafx.scene.input.KeyCombination;
//import javafx.scene.layout.BorderPane;
//import javafx.stage.DirectoryChooser;
//import javafx.stage.Stage;
//import org.eclipse.fx.code.editor.SourceFileInput;
//import org.eclipse.fx.code.editor.StringInput;
//import org.eclipse.fx.code.editor.fx.TextEditor;
//import org.eclipse.fx.code.editor.fx.services.internal.DefaultSourceViewerConfiguration;
//import org.eclipse.fx.code.editor.services.InputDocument;
//import org.eclipse.fx.ui.controls.filesystem.FileItem;
//import org.eclipse.fx.ui.controls.filesystem.ResourceEvent;
//import org.eclipse.fx.ui.controls.filesystem.ResourceItem;
//import org.eclipse.fx.ui.controls.filesystem.ResourceTreeView;
//import org.eclipse.jface.text.presentation.PresentationReconciler;
//import org.eclipse.jface.text.rules.*;
//
//import java.io.File;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//
///** http://tomsondev.bestsolution.at/2015/07/24/developing-a-source-code-editor-in-javafx-without-e4-and-osgi/ */
//public class DartEditorSample extends Application {
//
//    public static class TagRule extends MultiLineRule {
//        public TagRule(IToken token) {
//            super("<", ">", token);
//        }
//        protected boolean sequenceDetected(
//                ICharacterScanner scanner,
//                char[] sequence,
//                boolean eofAllowed) {
//            int c = scanner.read();
//            if (sequence[0] == '<') {
//                if (c == '?') {
//                    // processing instruction - abort
//                    scanner.unread();
//                    return false;
//                }
//                if (c == '!') {
//                    scanner.unread();
//                    // comment - abort
//                    return false;
//                }
//            } else if (sequence[0] == '>') {
//                scanner.unread();
//            }
//            return super.sequenceDetected(scanner, sequence, eofAllowed);
//        }
//    }
//
//    public static class XMLPartitionScanner extends RuleBasedPartitionScanner {
//        public final static String XML_COMMENT = "__xml_comment";
//        public final static String XML_TAG = "__xml_tag";
//        public XMLPartitionScanner() {
//            IToken xmlComment = new Token(XML_COMMENT);
//            IToken tag = new Token(XML_TAG);
//            IPredicateRule[] rules = new IPredicateRule[2];
//            rules[0] = new MultiLineRule("<!--", "-->", xmlComment);
//            rules[1] = new TagRule(tag);
//            setPredicateRules(rules);
//        }
//    }
//
//    public static class DefaultEditor extends TextEditor {
//        public DefaultEditor(StringInput input) {
//            setInput(input);
//            setDocument(new InputDocument(input));
//
//            setPartitioner(new FastPartitioner(new XMLPartitionScanner(),
//                    new String[] { XMLPartitionScanner.XML_COMMENT, XMLPartitionScanner.XML_TAG }
//            ));
//
//            //setPartitioner(new FastPartitioner(
////                    new FastJavaLikePartitionScanner(
////                            //public FastJavaLikePartitionScanner(
////                            // java.lang.String singleLineCommentKey,
////                            // java.lang.String multiLineCommentKey,
////                            // java.lang.String javaDocKey,
////                            // java.lang.String characterKey,
////                            // java.lang.String stringKey)
////                            "//", "/*", "/**", "\'", "\""
////                    ),
////                    "text/html"
////
////            ));
//            //setPartitioner(new DartPartitioner());
//            setSourceViewerConfiguration(
//                    new DefaultSourceViewerConfiguration(input,
//                            new PresentationReconciler(), null, null, null)
//                            //new DartPresentationReconciler(), null, null, null)
//            );
//        }
//    }
//
//    private TabPane tabFolder;
//    private ResourceTreeView viewer;
//
//    static class EditorData {
//        final Path path;
//        final DefaultEditor editor;
//
//        public EditorData(Path path, DefaultEditor editor) {
//            this.path = path;
//            this.editor = editor;
//        }
//    }
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        BorderPane p = new BorderPane();
//        p.setTop(createMenuBar());
//
//        viewer = new ResourceTreeView();
//        viewer.addEventHandler(ResourceEvent.openResourceEvent(), this::handleOpenResource);
//        p.setLeft(viewer);
//
//        tabFolder = new TabPane();
//        p.setCenter(tabFolder);
//
//        Scene s = new Scene(p, 800, 600);
//        //s.getStylesheets().add(getClass().getResource("default.css").toExternalForm());
//
//        primaryStage.setScene(s);
//        primaryStage.show();
//    }
//
//    private MenuBar createMenuBar() {
//        MenuBar bar = new MenuBar();
//
//        Menu fileMenu = new Menu("File");
//
//        MenuItem rootDirectory = new MenuItem("Select root folder ...");
//        rootDirectory.setOnAction(this::handleSelectRootFolder);
//
//        MenuItem saveFile = new MenuItem("Save");
//        saveFile.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
//        saveFile.setOnAction(this::handleSave);
//
//
//        fileMenu.getItems().addAll(rootDirectory, saveFile);
//
//        bar.getMenus().add(fileMenu);
//
//        return bar;
//    }
//
//    private void handleSelectRootFolder(ActionEvent e) {
//        DirectoryChooser chooser = new DirectoryChooser();
//        File directory = chooser.showDialog(viewer.getScene().getWindow());
//        if( directory != null ) {
//            viewer.setRootDirectories(
//                    FXCollections.observableArrayList(ResourceItem.createObservedPath(Paths.get(directory.getAbsolutePath()))));
//        }
//    }
//
//    private void handleSave(ActionEvent e) {
//        Tab t = tabFolder.getSelectionModel().getSelectedItem();
//        if( t != null ) {
//            ((EditorData)t.getUserData()).editor.save();
//        }
//    }
//
//    private void handleOpenResource(ResourceEvent<ResourceItem> e) {
//        e.getResourceItems()
//            .stream()
//            .filter( r -> r instanceof FileItem)
//            .map( r -> (FileItem)r)
//            //.filter( r -> r.getName().endsWith(".dart"))
//            .forEach(this::handle);
//    }
//
//    private void handle(FileItem item) {
//        Path path = (Path) item.getNativeResourceObject();
//
//        Tab tab = tabFolder.getTabs().stream().filter( t -> ((EditorData)t.getUserData()).path.equals(path) ).findFirst().orElseGet(() -> {
//            return createAndAttachTab(path, item);
//        });
//        tabFolder.getSelectionModel().select(tab);
//    }
//
//    private Tab createAndAttachTab(Path path, FileItem item) {
//        BorderPane p = new BorderPane();
//        DefaultEditor editor = new DefaultEditor(new SourceFileInput(path, StandardCharsets.UTF_8));
//        editor.initUI(p);
//
//        //ReadOnlyBooleanProperty modifiedProperty = editor.modifiedProperty();
//        StringExpression titleText = Bindings.createStringBinding(() ->
//        /*{
//            return modifiedProperty.get() ? "*" : "";
//        }, modifiedProperty).concat*/(item.getName()));
//
//        Tab t = new Tab();
//        t.textProperty().bind(titleText);
//        t.setContent(p);
//        t.setUserData(new EditorData(path, editor));
//        tabFolder.getTabs().add(t);
//        return t;
//    }
//
//    public static void main(String[] args) {
//        Application.launch(args);
//    }
// }