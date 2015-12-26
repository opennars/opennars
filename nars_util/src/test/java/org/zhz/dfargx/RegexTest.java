package org.zhz.dfargx;

import org.junit.Assert;
import org.junit.Test;
import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;
import org.zhz.dfargx.node.Node;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 5/6/15.
 */
public class RegexTest {

    //"([ab]([^cd]*\\w+(abc|abcd){2,5})+)?.*"
    @Test
    public void testProcessing() {
        long pre = System.nanoTime();
        String regex = "(a*b|ab*)";
        SyntaxTree tree = new SyntaxTree(regex);
        Node root = tree.getRoot();
        System.out.println("For regex: " + regex);
        System.out.println("Syntax tree: ");
        TreePrinter.getInstance().printTree(root);
        NFA nfa = new NFA(root);
        System.out.println("NFA has " + nfa.getStateList().size() + " states");
        DFA dfa = new DFA(nfa.getStateList());
        System.out.println("DFA has " + dfa.getTransitionTable().length + " states");
        System.out.println("Cost " + (System.nanoTime() - pre)/1E6 + " ms to compile");
    }

    @Test
    public void testUUID() {
        String regex = "\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";
        String str = UUID.randomUUID().toString();
        int num = 100000;

        testMatchingSpeed(regex, str, num);
    }

    @Test
    public void testAddress() {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        String str = "192.168.0.255";
        int num = 100000;

        testMatchingSpeed(regex, str, num);
    }

    @Test
    public void testLog() {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} - - \\[[^\\]]+\\] \"[^\"]+\" \\d+ \\d+ \"[^\"]+\" \"[^\"]+\"";
        String str = "11.11.11.11 - - [25/Jan/2000:14:00:01 +0100] \"GET /1986.js HTTP/1.1\" 200 932 \"http://domain.com/index.html\" \"Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.1.7) Gecko/20091221 Firefox/3.5.7 GTB6\"";
        int num = 100000;

        testMatchingSpeed(regex, str, num);
    }

    @Test
    public void testEmail() {
        String regex = "\\[\\w+@[\\w\\.]+\\]";
        String str = "[Tom@yahoo.com] sends an email to [Lucy@gmail.com] and [Mike@hotmail.com][John@hotmail.com]";
        int num = 100000;

        testSearchingSpeed(regex, str, num);
    }

    private void testMatchingSpeed(String regex, String str, int num) {
        System.out.println("Matching " + num + " strings using: ");
        System.out.println("[Pattern] " + regex);
        System.out.println("[String]" + str);
        long pre = System.nanoTime();
        RegexMatcher rgxMatcher = new RegexMatcher(regex);
        System.out.println("DFA matcher Cost " + (System.nanoTime() - pre)/1E6f + " ms to compile");
        pre = System.nanoTime();
        boolean dfaResult = false;
        for (int i = 0; i < num; i++) {
            dfaResult = rgxMatcher.match(str);
        }
        System.out.println("DFA matcher Cost " + matchesPerMS(num, pre)*1E6f + " matches per ms");
        System.out.println(dfaResult);

        pre = System.nanoTime();
        Pattern pattern = Pattern.compile(regex);
        Matcher mc = pattern.matcher(str);
        System.out.println("Java pattern Cost " + (System.nanoTime() - pre)/1E6f + " ms to compile");
        pre = System.nanoTime();
        boolean jpResult = false;
        for (int i = 0; i < num; i++) {
            jpResult = pattern.matcher(str).matches();
        }
        System.out.println("Java pattern Cost " + matchesPerMS(num, pre)*1E6f + " matches per ms");
        System.out.println(jpResult);
        System.out.println();
        Assert.assertTrue(dfaResult == jpResult);
    }

    private float matchesPerMS(float num, long pre) {
        return (num * 1f)/(System.nanoTime() - pre);
    }

    private void testSearchingSpeed(String regex, String str, int num) {
        long pre = System.nanoTime();
        RegexSearcher searcher = new RegexSearcher(regex);
        for (int i = 0; i < num; i++) {
            searcher.search(str);
            while (searcher.hasMoreElements()) {
                MatchedText text = searcher.nextElement();
                text.getText();
                text.getPos();
                //System.out.println(text);
            }
        }
        System.out.println("DFA matcher Cost " + (System.nanoTime() - pre)/1E6 + " ms to do searching");
        pre = System.nanoTime();
        Pattern pattern = Pattern.compile(regex);
        for (int i = 0; i < num; i++) {
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                matcher.group();
                matcher.start();
                //System.out.println(matcher + " " + matcher.group() + " " + matcher.start());
            }
        }
        System.out.println("Java pattern Cost " + (System.nanoTime() - pre)/1E6 + " ms to do searching");
    }
}