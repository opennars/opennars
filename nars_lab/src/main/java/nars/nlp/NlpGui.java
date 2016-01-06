//package nars.io.nlp;
//
//import nars.core.NAR;
//import nars.build.Default;
//import nars.gui.NARSwing;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//
//public class NlpGui extends JFrame {
//    private class SentencePartContext {
//        JTextArea text = new JTextArea();
//        JLabel nlpPartLabel = new JLabel("TEST", JLabel.LEFT);
//        
//        
//        public void reset() {
//            nlpPart = "";
//            nlpPartLabel.setText("");
//            
//            text.setText("");
//            
//            sentencePart = null;
//        }
//        
//        public void set() {
//            nlpPartLabel.setText(nlpPart);
//        }
//        
//        public String getNarsese() {
//            return text.getText();
//        }
//        
//        public ArrayList<NaturalLanguagePerception.LinePart> sentencePart;
//        public String nlpPart = "";
//    }
//    
//    private class ParseActionListener implements ActionListener {
//        public ParseActionListener( JButton button, NlpGui nlpGui) {
//            this.button = button;
//            this.nlpGui = nlpGui;
//        }
//        
//        private JButton button;
//        private NlpGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            if(ae.getSource() != this.button){
//                return;
//            }
//            
//            nlpGui.parseWasPressed();
//        }
//        
//    }
//    
//    private class LearnActionListener implements ActionListener {
//        public LearnActionListener( JButton button, NlpGui nlpGui) {
//            this.button = button;
//            this.nlpGui = nlpGui;
//        }
//        
//        private JButton button;
//        private NlpGui nlpGui;
//        
//        public void actionPerformed (ActionEvent ae){
//            if(ae.getSource() != this.button){
//                return;
//            }
//            
//            nlpGui.learnWasPressed();
//        }
//    }
//    
//    public NlpGui(NAR nar) {
//        super("NLP");
//        
//        this.nar = nar;
//        
//        init();
//        
//        GridLayout gridLayout = new GridLayout(3+2, 2, 30, 20);
//
//        
//        setSize(300,300);
//        setLocation(300,300);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(gridLayout);
//        
//        pureTextArea = new JTextArea();
//        
//        JButton parseButton = new JButton("parse");
//        parseButton.addActionListener(new ParseActionListener(parseButton, this));
//        
//        JButton learnButton = new JButton("learn");
//        learnButton.addActionListener(new LearnActionListener(learnButton, this));
//        
//        add(pureTextArea);
//        add(parseButton);
//        
//        for( int i = 0; i < sentencePartContexts.length; i++ ) {
//            add(sentencePartContexts[i].nlpPartLabel);
//            add(sentencePartContexts[i].text);
//        }
//        
//        add(new JLabel());
//        add(learnButton);
//        
//        pack();
//        setVisible(true);
//    }
//    
//    public void parseWasPressed() {
//        resetSentencePartContexts();
//        
//        String pureTextAreaContent = pureTextArea.getText();
//        
//        ArrayList<NaturalLanguagePerception.LinePart> lineParts = NaturalLanguagePerception.parseIntoLineParts(pureTextAreaContent);
//        ArrayList<ArrayList<NaturalLanguagePerception.LinePart>> sentenceParts = NaturalLanguagePerception.splitLinePartsIntoSentenceParts(lineParts);
//        
//        int countOfSentenceParts = sentenceParts.size();
//        if (countOfSentenceParts > 3) {
//            countOfSentenceParts = 3;
//        }
//        
//        // for each sentencePart
//        // - reconstruct text
//        // - set sentencePart
//        // - set gui stuff
//        for (int i = 0; i < countOfSentenceParts; i++) {
//            ArrayList<NaturalLanguagePerception.LinePart> sentencePart = sentenceParts.get(i);
//            
//            String reconstructedString = NaturalLanguagePerception.reconstructString(sentencePart);
//            sentencePartContexts[i].nlpPart = reconstructedString;
//            sentencePartContexts[i].sentencePart = sentencePart;
//            sentencePartContexts[i].set();
//        }
//    }
//    
//    public void learnWasPressed() {
//        learn();
//        
//        resetSentencePartContexts();
//    }
//    
//    private void resetSentencePartContexts() {
//        for (int i = 0; i < 3; i++) {
//            sentencePartContexts[i].reset();
//        }
//    }
//    
//    private void learn() {
//        for (int i = 0; i < 3; i++) {
//            // TODO< more clean way to check if it is active/used >
//            if (sentencePartContexts[i].sentencePart == null) {
//                continue;
//            }
//            
//            learnSentencePart(sentencePartContexts[i]);
//        }
//    }
//    
//    private void learnSentencePart(SentencePartContext context) {
//        for (NaturalLanguagePerception.LinePart iterationLinePart : context.sentencePart) {
//            String narseseOfLinePart = translateLinePartToNarsese(iterationLinePart);
//            
//            nar.addInput(narseseOfLinePart);
//            nar.step(5);
//        }
//        
//        nar.addInput(context.getNarsese());
//        nar.step(15);
//    }
//    
//    private static String translateLinePartToNarsese(NaturalLanguagePerception.LinePart linePart) {
//        if (linePart.type == NaturalLanguagePerception.LinePart.EnumType.SIGN) {
//            return "'" + linePart.content + "'.";
//        }
//        else {
//            return linePart.content + ".";
//        }
//    }
//    
//    private void init() {
//        sentencePartContexts = new  SentencePartContext[3];
//        
//        sentencePartContexts[0] = new SentencePartContext();
//        sentencePartContexts[1] = new SentencePartContext();
//        sentencePartContexts[2] = new SentencePartContext();
//    }
//    
//    public static void main(String[] args) {
//        NAR nar = new NAR(new Default().setSubconceptBagSize(1000));
//        
//        new NARSwing(nar);
//        
//        NlpGui g = new NlpGui(nar);
//    }
//    
//    private NAR nar;
//    
//    private JTextArea pureTextArea;
//    
//    private SentencePartContext[] sentencePartContexts;
// }
