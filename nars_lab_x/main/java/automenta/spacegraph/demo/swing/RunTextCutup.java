///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.swing.SwingWindow;
//import com.syncleus.dann.math.statistics.SimpleMarkovChain;
//import com.syncleus.dann.math.statistics.SimpleMarkovChainEvidence;
//import java.awt.BorderLayout;
//import java.awt.Font;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.StringTokenizer;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import javax.swing.JTextField;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
///**
// *
// * @author seh
// */
//public class RunTextCutup extends JPanel implements Demo {
//
//    private final JTextArea inputArea;
//    private final JTextArea outputArea;
//    private Map<String, Integer> knownCount;
//    private Set<String> known;
//    //int maxKnown = 10;
//    //int order = 2;
//    //double orderWeights[] = { 1, 0.5, 0.25, 0.15, 0.05, 0.01 };
//
//    final int initialMaxKnown = 1024;
//    Font textFont = new Font("Arial", Font.PLAIN, 24);
//
//    public RunTextCutup() {
//        super(new BorderLayout(4, 4));
//
//        inputArea = new JTextArea();
//        inputArea.setLineWrap(true);
//        inputArea.setWrapStyleWord(true);
//        inputArea.setFont(textFont);
//
//        outputArea = new JTextArea();
//        outputArea.setLineWrap(true);
//        outputArea.setWrapStyleWord(true);
//        outputArea.setFont(textFont);
//
//        JPanel menuPanel = new JPanel(new GridBagLayout());
//        {
//            GridBagConstraints gc = new GridBagConstraints();
//            gc.gridx = 1;
//            gc.gridy = 1;
//            gc.fill = gc.VERTICAL;
//            gc.weightx = 0;
//            gc.weighty = 1.0;
//            gc.insets = new Insets(0,4,0,4);
//
//            final JTextField knownField = new JTextField(Integer.toString(initialMaxKnown));
//            knownField.setColumns(6);
//            menuPanel.add(knownField, gc);
//
//            gc.gridx++;
//
//            final JComboBox orderField = new JComboBox();
//            orderField.addItem("1st-Order*");
//            orderField.addItem("2nd-Order");
//            orderField.addItem("3rd-Order");
//            orderField.addItem("4th-Order");
//            menuPanel.add(orderField, gc);
//
//            gc.gridx++;
//
//            JButton run = new JButton("Run");
//            run.addActionListener(new ActionListener() {
//
//                @Override public void actionPerformed(ActionEvent e) {
//                    final int order = orderField.getSelectedIndex() + 1;
//                    final int maxSymbols = Integer.decode(knownField.getText());
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override public void run() {
//                            RunTextCutup.this.run(maxSymbols, order);
//                        }
//                    });
//                }
//            });
//            menuPanel.add(run, gc);
//        }
//
//        JPanel center = new JPanel(new GridLayout(2, 1));
//        center.add(new JScrollPane(inputArea));
//        center.add(new JScrollPane(outputArea));
//
//        add(center, BorderLayout.CENTER);
//        add(menuPanel, BorderLayout.SOUTH);
//
//
//    }
//
//    public List<String> getTokens() {
//        String input = inputArea.getText();
//        List<String> s = new LinkedList();
//
//        StringTokenizer st = new StringTokenizer(input, " ,.!?;-()[]{}/:@\n", true);
//        while (st.hasMoreTokens()) {
//            String w = st.nextToken();
//
//            if (!w.equals("\n")) {
//                w = w.trim();
//            }
//
//            if (w.length() > 0) {
//                w = w.toLowerCase();
//                s.add(w);
//            }
//        }
//        return s;
//
//    }
//
//    public List<String> getSequence(List<String> tokens) {
//        String input = inputArea.getText();
//        List<String> s = new LinkedList();
//
//        StringTokenizer st = new StringTokenizer(input, " ,.!?;-()[]{}/:@\n", true);
//        for (String w : tokens) {
//            if (isKnown(w)) {
//                s.add(w);
//            }
//        }
//
//        return s;
//    }
//
//    public boolean isKnown(String symbol) {
//        return known.contains(symbol);
//    }
//
//    public static boolean isEntirelyPunctuation(String w) {
//        for (char c : w.toCharArray()) {
//            if (Character.isLetterOrDigit(c)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    protected void run(int maxSymbols, int order) {
//        SimpleMarkovChainEvidence<String> sme = new SimpleMarkovChainEvidence<String>(true, order);
//
//        knownCount = new HashMap();
//
//        List<String> tokens = getTokens();
//        for (String s : tokens) {
//            Integer i = knownCount.get(s);
//
//
//            if (i == null) {
//                knownCount.put(s, 1);
//
//
//            } else {
//                knownCount.put(s, i + 1);
//
//
//            }
//        }
//
//
//        List<String> sortedKnown = new ArrayList<String>(knownCount.keySet());
//        Collections.sort(sortedKnown, new Comparator<String>() {
//
//            @Override public int compare(String o1, String o2) {
//                double f1 = getStrength(o1);
//
//
//                double f2 = getStrength(o2);
//
//
//                if (f1 > f2) {
//                    return -1;
//
//
//                } else if (f1 < f2) {
//                    return 1;
//
//
//                } else {
//                    return 0;
//
//
//                }
//            }
//        });
//
//
//        int n = Math.min(sortedKnown.size(), maxSymbols);
//        known = new HashSet(sortedKnown.subList(0, n));
//
//        long learnStart = System.nanoTime();
//        List<String> seq = getSequence(tokens);
//        for (String s : seq) {
//            sme.learnStep(s);
//            //if (s.equals(".") || (s.equals("?")) || (s.equals("!")))
//                //sme.newChain();
//        }
//        double learnTime = (System.nanoTime() - learnStart)/1e9;
//        System.out.println("Learning: " + learnTime + " #: " + seq.size());
//
//        String output = "";
//
//        try {
//            long markovStart = System.nanoTime();
//
//            //TODO the cast will be unnecessary when MarkovChain supports weighted transition generation
//            SimpleMarkovChain<String> mc = (SimpleMarkovChain)sme.getMarkovChain();
//            double markovChainGenerationTime = (System.nanoTime() - markovStart)/1e9;
//            System.out.println("Markov Chain: " + markovChainGenerationTime);
//
//            //System.out.println(mc.getTransitionProbabilityMatrix());
//            System.out.println("#" + mc.getStates().size());
//
//            for (int i = 0; i < 700; i++) {
//                String nextSymbol = mc.generateTransition();
//
//
//                if (nextSymbol.equals(" ")) {
//                    output += " ";
//                } else {
//                    output += nextSymbol + " ";
//                }
//
//
//            }
//        } catch (Exception e) {
//            output += "\n" + e.toString();
//            e.printStackTrace();
//
//
//        }
//
//        outputArea.setText(output);
//
//
//    }
//
//    public double getStrength(String symbol) {
//        int count = knownCount.get(symbol);
//
//        double sizeBoost = symbol.length() > 4 ? 2.0 : 1.0;
//
//        return count * sizeBoost;
//    }
//
//    @Override
//    public JPanel newPanel() {
//        return this;
//    }
//
//    @Override
//    public String getName() {
//        return "Text Cut-Up";
//    }
//
//    @Override
//    public String getDescription() {
//        return "..";
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//
//
//                } catch (Exception ex) {
//                    System.err.println(ex);
//
//
//                }
//
//                SwingWindow window = new SwingWindow(new RunTextCutup().newPanel(), 900, 800, true);
//
//
//            }
//        });
//
//
//    }
//}
