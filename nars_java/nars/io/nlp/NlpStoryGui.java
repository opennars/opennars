package nars.io.nlp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import nars.core.NAR;
import nars.core.build.Default;
import nars.gui.NARSwing;

public class NlpStoryGui extends JFrame {
    private class PreviousSceneActionListener implements ActionListener {
        public PreviousSceneActionListener(NlpStoryGui nlpGui) {
            this.nlpGui = nlpGui;
        }
        
        private NlpStoryGui nlpGui;
        
        public void actionPerformed (ActionEvent ae){
            nlpGui.previousScenePressed();
        }
    }
    
    private class NextSceneActionListener implements ActionListener {
        public NextSceneActionListener(NlpStoryGui nlpGui) {
            this.nlpGui = nlpGui;
        }
        
        private NlpStoryGui nlpGui;
        
        public void actionPerformed (ActionEvent ae){
            nlpGui.nextScenePressed();
        }
    }
    
    private class LearnActionListener implements ActionListener {
        public LearnActionListener(NlpStoryGui nlpGui) {
            this.nlpGui = nlpGui;
        }
        
        private NlpStoryGui nlpGui;
        
        public void actionPerformed (ActionEvent ae){
            nlpGui.learnPressed();
        }
    }
    
    public NlpStoryGui(NAR nar) {
        super("NLP");
        
        this.nar = nar;
        
        init();
        
        JPanel nextPreviousPanel = new JPanel();
        BorderLayout nextPreviousBorderLayout = new BorderLayout();
        nextPreviousPanel.setLayout(nextPreviousBorderLayout);
        
        JButton previousButton = new JButton("<");
        previousButton.addActionListener(new PreviousSceneActionListener(this));
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(new NextSceneActionListener(this));
        
        nextPreviousPanel.add(previousButton, BorderLayout.LINE_START);
        nextPreviousPanel.add(labelCurrentStory, BorderLayout.CENTER);
        nextPreviousPanel.add(nextButton, BorderLayout.LINE_END);
        
        JButton resetStoryButton = new JButton("reset story");
        JButton learnButton = new JButton("learn story");
        learnButton.addActionListener(new LearnActionListener(this));
        
        setSize(300,300);
        setLocation(300,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 1, 0, 15));
        
        add(nextPreviousPanel);
        add(new JLabel("Action(s)"));
        add(actionsTextArea);
        add(new JLabel("Text"));
        add(textTextArea);
        
        add(resetStoryButton);
        add(learnButton);
        
        pack();
        setVisible(true);
    }
    
    /*
    public void parseWasPressed() {
        resetSentencePartContexts();
        
        String pureTextAreaContent = pureTextArea.getText();
        
        ArrayList<NaturalLanguagePerception.LinePart> lineParts = NaturalLanguagePerception.parseIntoLineParts(pureTextAreaContent);
        ArrayList<ArrayList<NaturalLanguagePerception.LinePart>> sentenceParts = NaturalLanguagePerception.splitLinePartsIntoSentenceParts(lineParts);
        
        int countOfSentenceParts = sentenceParts.size();
        if (countOfSentenceParts > 3) {
            countOfSentenceParts = 3;
        }
        
        // for each sentencePart
        // - reconstruct text
        // - set sentencePart
        // - set gui stuff
        for (int i = 0; i < countOfSentenceParts; i++) {
            ArrayList<NaturalLanguagePerception.LinePart> sentencePart = sentenceParts.get(i);
            
            String reconstructedString = NaturalLanguagePerception.reconstructString(sentencePart);
            sentencePartContexts[i].nlpPart = reconstructedString;
            sentencePartContexts[i].sentencePart = sentencePart;
            sentencePartContexts[i].set();
        }
    }
    
    public void learnWasPressed() {
        learn();
        
        resetSentencePartContexts();
    }
    
    private void resetSentencePartContexts() {
        for (int i = 0; i < 3; i++) {
            sentencePartContexts[i].reset();
        }
    }
    
    private void learn() {
        for (int i = 0; i < 3; i++) {
            // TODO< more clean way to check if it is active/used >
            if (sentencePartContexts[i].sentencePart == null) {
                continue;
            }
            
            learnSentencePart(sentencePartContexts[i]);
        }
    }
    
    private void learnSentencePart(SentencePartContext context) {
        for (NaturalLanguagePerception.LinePart iterationLinePart : context.sentencePart) {
            String narseseOfLinePart = translateLinePartToNarsese(iterationLinePart);
            
            nar.addInput(narseseOfLinePart);
            nar.step(5);
        }
        
        nar.addInput(context.getNarsese());
        nar.step(15);
    }
    
    
    
    private void init() {
        sentencePartContexts = new  SentencePartContext[3];
        
        sentencePartContexts[0] = new SentencePartContext();
        sentencePartContexts[1] = new SentencePartContext();
        sentencePartContexts[2] = new SentencePartContext();
    }
    */
    
    public static void main(String[] args) {
        NAR nar = NAR.build(new Default().setSubconceptBagSize(1000));
        
        new NARSwing(nar);
        
        NlpStoryGui g = new NlpStoryGui(nar);
    }
    
    public void nextScenePressed() {
        storeScene();
        
        if (currentSceneIndex >= scenes.size()-1) {
            scenes.add(new Scene());
            currentSceneIndex = scenes.size()-1;
        }
        else {
            currentSceneIndex++;
        }
        
        updateGui();
    }
    
    public void previousScenePressed() {
        if (currentSceneIndex == 0) {
            return;
        }
        // else
        
        storeScene();
        currentSceneIndex--;
        updateGui();
    }
    
    public void learnPressed() {
        storeScene();
        
        learnStory(scenes);
        
        // flush and update GUI
        flushStory();
        updateGui();
    }
    
    private void flushStory() {
        scenes.clear();
        scenes.add(new Scene());
        currentSceneIndex = 0;
    }
    
    private void learnStory(ArrayList<Scene> scenesToLearn) {
        for (Scene iteratorScene : scenesToLearn) {
            nar.addInput(iteratorScene.actionAsString + ". :|:");
            nar.step(5);
            nar.addInput(translateTextIntoTemporalNarsese(iteratorScene.textAsString) + ". :|:");
            nar.step(20);
        }
    }
    
    private void init() {
        flushStory();
    }
    
    private void storeScene() {
        if( currentSceneIndex < 0 || currentSceneIndex >= scenes.size() ) {
            // internal error
            return;
        }
        
        Scene selectedScene = scenes.get(currentSceneIndex);
        selectedScene.actionAsString = actionsTextArea.getText();
        selectedScene.textAsString = textTextArea.getText();
    }
    
    private void updateGui() {
        String currentSceneNumber = "" + (currentSceneIndex+1);
        String scenesCountAsString = "" + scenes.size();
        
        labelCurrentStory.setText(currentSceneNumber + " / " + scenesCountAsString);
        
        if( currentSceneIndex < 0 || currentSceneIndex >= scenes.size() ) {
            // internal error
            return;
        }
        
        Scene selectedScene = scenes.get(currentSceneIndex);
        actionsTextArea.setText(selectedScene.actionAsString);
        textTextArea.setText(selectedScene.textAsString);
    }
    
    private static String translateTextIntoTemporalNarsese(String text) {
        String result = "";
        
        ArrayList<NaturalLanguagePerception.LinePart> lineParts = NaturalLanguagePerception.parseIntoLineParts(text);
        int maximalIndex = lineParts.size()-1;
        int index = 0;
        
        for (NaturalLanguagePerception.LinePart iterationLinePart : lineParts) {
            result += translateLinePartToNarsese(iterationLinePart);
            
            if (index != maximalIndex) {
                result += ", ";
            }
            
            index++;
        }
        
        return "<(&/," + result + ") --> sentence>";
    }
    
    private static String translateLinePartToNarsese(NaturalLanguagePerception.LinePart linePart) {
        if (linePart.type == NaturalLanguagePerception.LinePart.EnumType.SIGN) {
            return "<'" + linePart.content + "' --> sign>";
        }
        else {
            return "<" + linePart.content + " --> word>";
        }
    }

    private NAR nar;
    
    private class Scene {
        public String actionAsString = ""; // narsese
        public String textAsString = "";
    }
    
    private ArrayList<Scene> scenes = new ArrayList<Scene>();
    private int currentSceneIndex = 0;
    
    private TextArea actionsTextArea = new TextArea();
    private TextArea textTextArea = new TextArea();
    private JLabel labelCurrentStory = new JLabel("1 / 1", JLabel.CENTER);
}
