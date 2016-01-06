//package nars.io.nlp;
//
//import com.google.gson.Gson;
//import nars.core.NAR;
//import nars.build.Default;
//import nars.gui.NARSwing;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.*;
//import java.util.ArrayList;
//
//public class NlpStoryGui extends JFrame {
//    private class PreviousSceneActionListener implements ActionListener {
//        public PreviousSceneActionListener(NlpStoryGui nlpGui) {
//            this.nlpGui = nlpGui;
//        }
//        
//        private NlpStoryGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            nlpGui.previousScenePressed();
//        }
//    }
//    
//    private class NextSceneActionListener implements ActionListener {
//        public NextSceneActionListener(NlpStoryGui nlpGui) {
//            this.nlpGui = nlpGui;
//        }
//        
//        private NlpStoryGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            nlpGui.nextScenePressed();
//        }
//    }
//    
//    private class LearnActionListener implements ActionListener {
//        public LearnActionListener(NlpStoryGui nlpGui) {
//            this.nlpGui = nlpGui;
//        }
//        
//        private NlpStoryGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            nlpGui.learnPressed();
//        }
//    }
//    
//    private class LoadActionListener implements ActionListener {
//        public LoadActionListener(NlpStoryGui nlpGui) {
//            this.nlpGui = nlpGui;
//        }
//        
//        private NlpStoryGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            nlpGui.loadPressed();
//        }
//    }
//    
//    private class SaveActionListener implements ActionListener {
//        public SaveActionListener(NlpStoryGui nlpGui) {
//            this.nlpGui = nlpGui;
//        }
//        
//        private NlpStoryGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            nlpGui.savePressed();
//        }
//    }
//    
//    public NlpStoryGui(NAR nar) {
//        super("NLP");
//        
//        this.nar = nar;
//        
//        init();
//        
//        actionPanelContext = ActionPanelContext.createPanelContext(this);
//        
//        JPanel storagePanel = new JPanel();
//        JButton loadButton = new JButton("load");
//        loadButton.addActionListener(new LoadActionListener(this));
//        JButton saveButton = new JButton("save");
//        saveButton.addActionListener(new SaveActionListener(this));
//        
//        storagePanel.add(loadButton, BorderLayout.WEST);
//        storagePanel.add(saveButton, BorderLayout.EAST);
//        
//        
//        JPanel nextPreviousPanel = new JPanel();
//        BorderLayout nextPreviousBorderLayout = new BorderLayout();
//        nextPreviousPanel.setLayout(nextPreviousBorderLayout);
//        
//        JButton previousButton = new JButton("<");
//        previousButton.addActionListener(new PreviousSceneActionListener(this));
//        JButton nextButton = new JButton(">");
//        nextButton.addActionListener(new NextSceneActionListener(this));
//        
//        nextPreviousPanel.add(previousButton, BorderLayout.LINE_START);
//        nextPreviousPanel.add(labelCurrentStory, BorderLayout.CENTER);
//        nextPreviousPanel.add(nextButton, BorderLayout.LINE_END);
//        
//        JButton resetStoryButton = new JButton("reset story");
//        JButton learnButton = new JButton("learn story");
//        learnButton.addActionListener(new LearnActionListener(this));
//        
//        setSize(300,300);
//        setLocation(300,300);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new GridLayout(7, 1, 0, 15));
//        
//        add(storagePanel);
//        add(nextPreviousPanel);
//        add(actionPanelContext.panel);
//        add(new JLabel("Text"));
//        add(textTextArea);
//        
//        add(resetStoryButton);
//        add(learnButton);
//        
//        pack();
//        setVisible(true);
//    }
//    
//    
//    public static void main(String[] args) {
//        NAR nar = new NAR(new Default().setSubconceptBagSize(1000));
//        
//        new NARSwing(nar);
//        
//        NlpStoryGui g = new NlpStoryGui(nar);
//    }
//    
//    public void nextScenePressed() {
//        storeScene();
//        
//        if (currentSceneIndex >= scenes.scenes.size()-1) {
//            scenes.scenes.add(new Scene());
//            currentSceneIndex = scenes.scenes.size()-1;
//        }
//        else {
//            currentSceneIndex++;
//        }
//        
//        updateGui();
//    }
//    
//    public void previousScenePressed() {
//        if (currentSceneIndex == 0) {
//            return;
//        }
//        // else
//        
//        storeScene();
//        currentSceneIndex--;
//        updateGui();
//    }
//    
//    public void learnPressed() {
//        storeScene();
//        
//        learnStory(scenes.scenes);
//        
//        // flush and update GUI
//        flushStory();
//        updateGui();
//    }
//    
//    public void savePressed() {
//        int returnVal = fc.showSaveDialog(NlpStoryGui.this);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fc.getSelectedFile();
//            String filepath = selectedFile.getAbsolutePath();
//            
//            Gson gson = new Gson();
//            String gsonContent = gson.toJson(scenes);
//
//            try {
//                storeTextToFile(filepath, gsonContent);
//            }
//            catch (IOException exception) {
//                // ignore
//            }
//        } else {
//            // Open command cancelled by user
//        }
//    }
//    
//    public void loadPressed() {
//        int returnVal = fc.showOpenDialog(NlpStoryGui.this);
// 
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fc.getSelectedFile();
//            String filepath = selectedFile.getAbsolutePath();
//            
//            String gsonContent = "";
//            
//            try {
//                gsonContent = loadTextFromFile(filepath);
//            }
//            catch (IOException exception) {
//                // ignore
//            }
//            
//            Gson gson = new Gson();
//            scenes = (Scenes)gson.fromJson(gsonContent, Scenes.class);
//            
//            currentSceneIndex = 0;
//            updateGui();
//        } else {
//            // Open command cancelled by user
//        }
//    }
//    
//    private void flushStory() {
//        scenes.scenes.clear();
//        scenes.scenes.add(new Scene());
//        currentSceneIndex = 0;
//    }
//    
//    private void learnStory(ArrayList<Scene> scenesToLearn) {
//        for (int i = 0; i < STORY_REPEATS; i++) {
//            learnStorySingleRun(scenesToLearn);
//        }
//    }
//    
//    private void learnStorySingleRun(ArrayList<Scene> scenesToLearn) {
//        nar.addInput("<meta-stop --> meta-info>. :|:");
//        nar.step(1);
//        
//        for (Scene iteratorScene : scenesToLearn) {
//            String narseseOfActions = convertActionItemsToNarsese(iteratorScene.actionItems);
//            
//            nar.addInput(narseseOfActions + ". :|:");
//            nar.step(STEPS_BETWEEN);
//            translateTextIntoTemporalNarseseAndLearn(iteratorScene.textAsString);
//            nar.step(STEPS_BETWEENACTIONS);
//        }
//        
//        nar.step(STEPS_BETWEENSCENES-STEPS_BETWEENACTIONS);
//    }
//    
//    private void init() {
//        flushStory();
//    }
//    
//    private void storeScene() {
//        if( currentSceneIndex < 0 || currentSceneIndex >= scenes.scenes.size() ) {
//            // internal error
//            return;
//        }
//        
//        Scene selectedScene = scenes.scenes.get(currentSceneIndex);
//        selectedScene.actionItems = actionPanelContext.items;
//        selectedScene.textAsString = textTextArea.getText();
//    }
//    
//    private void updateGui() {
//        String currentSceneNumber = "" + (currentSceneIndex+1);
//        String scenesCountAsString = "" + scenes.scenes.size();
//        
//        labelCurrentStory.setText(currentSceneNumber + " / " + scenesCountAsString);
//        
//        if( currentSceneIndex < 0 || currentSceneIndex >= scenes.scenes.size() ) {
//            // internal error
//            return;
//        }
//        
//        Scene selectedScene = scenes.scenes.get(currentSceneIndex);
//        actionPanelContext.items = selectedScene.actionItems;
//        actionPanelContext.updateList();
//        //actionsTextArea.setText(selectedScene.actionAsString);
//        textTextArea.setText(selectedScene.textAsString);
//    }
//    
//    
//    
//    private void translateTextIntoTemporalNarseseAndLearn(String text) {
//        ArrayList<NaturalLanguagePerception.LinePart> lineParts = NaturalLanguagePerception.parseIntoLineParts(text);
//        int counter = 0;
//        
//        for (int i = 0; i < lineParts.size(); i++) {
//            NaturalLanguagePerception.LinePart iterationLinePart = lineParts.get(i);
//            
//            String linepartAsNarsese = translateLinePartToNarsese(iterationLinePart);
//            
//            nar.addInput(linepartAsNarsese + ". :|:");
//            nar.step(3);
//
//            recallAssociations(iterationLinePart);
//            
//            counter++;
//        }
//    }
//
//    /**
//     * This is a implementation of a mechanism I(SquareOfTwo) call 'recall'.
//     * The idea is from the book "Circuits in the Brain".
//     * The stated theoretic possible mechanism is that many words trigger association neurons, which trigger other association neurons.
//     * This way the "meaning" gets extracted.
//     *
//     * As the implementation in nars we just trigger the best n associations for a word.
//     * The nars system can do with it higherlevel reasoning and sorted out the correct meaning after the context.
//     *
//     */
//    private void recallAssociations(NaturalLanguagePerception.LinePart linePart) {
//        if( linePart.type != NaturalLanguagePerception.LinePart.EnumType.WORD ) {
//            nar.step(3*4);
//            return;
//        }
//
//        // TODO< retrive the 'ideas' associated with the word >
//        // for example if we have as a format for storing of associated concepts/interpretations of a word <(*, {concept-tree, concept-datastructure-tree, concept-name-tree}, $linePart.content$) --> meta-word-assoc>.
//        // we retrive the n best associated concepts and recall them
//        // the best concepts are the n concepts with the highest interest, which is the highest priority in the bag
//        for (int ni = 0; ni < n; ni++) {
//            nar.addInput(TODO best assocation + ". :|:");
//            nar.step(4-1);
//        }
//    }
//    
//    private static String translateLinePartToNarsese(NaturalLanguagePerception.LinePart linePart) {
//        if (linePart.type == NaturalLanguagePerception.LinePart.EnumType.SIGN) {
//            return "<{'" + linePart.content + "'} --> meta-sign>";
//        }
//        else {
//            return "<" + linePart.content + " --> meta-word>";
//        }
//    }
//    
//    private static String convertActionItemsToNarsese(ArrayList<ActionPanelContext.Item> items) {
//        int lastIndex = items.size()-1;
//        
//        String internResult = "";
//        
//        for (int i = 0; i < items.size(); i++) {
//            internResult += convertActionItemToNarsese(items.get(i));
//            
//            if (i != lastIndex) {
//                internResult += ",";
//            }
//        }
//        
//        return "(&|," + internResult + ")";
//    }
//    
//    private static String convertActionItemToNarsese(ActionPanelContext.Item item) {
//        String result;
//        
//        String typeAsText = ((ActionPanelContext.TypeInfo)ActionPanelContext.TYPES.get(item.type)).typeAsText;
//        
//        result = "";
//        
//        if (item.parameters.length == 1) {
//            result = item.parameters[0];
//        }
//        else {
//            int lastIndex = item.parameters.length-1;
//
//            for (int i = 0; i < item.parameters.length; i++) {
//                result += item.parameters[i];
//
//                if (i != lastIndex) {
//                    result += ",";
//                }
//            }
//            
//            result = "(*," + result + ")";
//        }
//        
//        result = "<" + result + " --> " + typeAsText + ">";
//        
//        if (item.isNegated) {
//            result = "(--," + result + ")";
//        }
//        
//        return result;
//    }
//    
//    private static void storeTextToFile(String filename, String text) throws IOException {
//        BufferedWriter writer = null;
//        try {
//            File file = new File(filename);
//
//            writer = new BufferedWriter(new FileWriter(file));
//            writer.write(text);
//        } finally {
//            try {
//                // Close the writer regardless of what happens...
//                writer.close();
//            } catch (Exception e) {
//            }
//        }
//    }
//    
//    private static String loadTextFromFile(String filename) throws IOException {
//        // JAVA 7
//        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while (line != null) {
//                sb.append(line);
//                sb.append(System.lineSeparator());
//                line = br.readLine();
//            }
//            String everything = sb.toString();
//            
//            return everything;
//        }
//    }
//    
//    private NAR nar;
//    
//    private class Scene {
//        public ArrayList<ActionPanelContext.Item> actionItems = new ArrayList<>();
//        public String textAsString = "";
//    }
//    
//    // encapsulated for simpler gson handling
//    private class Scenes {
//        public ArrayList<Scene> scenes = new ArrayList<Scene>();
//    }
//    
//    private Scenes scenes = new Scenes();
//    private int currentSceneIndex = 0;
//    
//    private TextArea actionsTextArea = new TextArea();
//    private TextArea textTextArea = new TextArea();
//    private JLabel labelCurrentStory = new JLabel("1 / 1", JLabel.CENTER);
//    
//    private ActionPanelContext actionPanelContext;
//    
//    private JFileChooser fc = new JFileChooser();
//    
//    private final int STEPS_BETWEEN = 3;
//    private final int STEPS_BETWEENACTIONS = 20;
//    private final int STEPS_BETWEENSCENES = 50; // must be greater or equal to STEPS_BETWEENACTIONS
//    
//    private final int STORY_REPEATS = 5;
// }
