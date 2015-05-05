package nars.io.nlp;

import nars.narsese.InvalidInputException;
import nars.narsese.OldNarseseParser;
import nars.nal.Task;
import nars.util.language.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class NaturalLanguagePerception {
    static public class LinePart {
        public LinePart(EnumType type, String content) {
            this.type = type;
            this.content = content;
        }
        
        public EnumType type;
        public String content;
        
        public enum EnumType {
            SIGN,
            WORD
        }
    }
    
    static public List<Task> parseLine(String input, OldNarseseParser narsese, String prefix) {

        List<String> statements = new ArrayList();

        parseLineInternal(input, statements, prefix);
        
        List<Task> results = new ArrayList();
        for (String i : statements) {
            try {
                //try {
                    Task t = narsese.parseNarsese(new StringBuilder(i));
                    if (t != null) {
                        results.add(t);
                    }
//                }
//                catch (Exception e) {
//                    System.err.println("CAN NOT PARSE LINE: " + i);
//                }

            }
            catch (InvalidInputException e) {
                throw new RuntimeException(e);
            }
        }
        
        return results;
    }
    
    static private void parseLineInternal(String input, List<String> statements, String prefix) {
        ArrayList<LinePart> lineParts = parseIntoLineParts(input);
        
        // TODO< split the lineparts on dot and comma and semicolon >
        String naresesOfPartOfLine = translateLinepartsToNareses(lineParts, prefix);
        statements.add(naresesOfPartOfLine);
    }
    
    static public ArrayList<LinePart> parseIntoLineParts(String input) {
        String[] splitedInput = StringUtil.splitInclusive(input, splitSigns);
        ArrayList<LinePart> lineParts = new ArrayList<LinePart>();
        
        for (int splitedI = 0; splitedI < splitedInput.length; splitedI++) {
            String inputPart = splitedInput[splitedI];
            
            if (inputPart.equals(" ")) {
                continue;
            }
            
            addOneOrManyLinePartsOfText(lineParts, inputPart);
        }
        
        return lineParts;
    }
    
    /**
     * 
     * determines the type of the inputPart and adds depending on it one or many LineParts
     * handles also the splitting of numbers into digits
     * 
     * \param lineParts
     * \param inputPart 
     */
    static private void addOneOrManyLinePartsOfText(ArrayList<LinePart> lineParts, String inputPart) {
        if (inputPart.length() == 1 && isSplitSign(inputPart.charAt(0))) {
            lineParts.add(new LinePart(LinePart.EnumType.SIGN, inputPart));
            return;
        }
        // else
        
        if (StringUtil.isNumeric(inputPart)) {
            for (int i = 0; i < inputPart.length(); i++) {
                lineParts.add(new LinePart(LinePart.EnumType.WORD, "" + inputPart.charAt(i)));
            }
        }
        else {
            lineParts.add(new LinePart(LinePart.EnumType.WORD, inputPart));
        }
    }
    
    // helper for outside
    public static String reconstructString(ArrayList<LinePart> lineParts) {
        String resultString = "";
        
        for(LinePart linePart : lineParts) {
            resultString += linePart.content + " ";
        }
        
        return resultString;
    }
    
    // helper for outside
    public static ArrayList<ArrayList<LinePart>> splitLinePartsIntoSentenceParts(ArrayList<LinePart> lineParts) {
        // TODO< move this into NaturalLanguagePerception >
        ArrayList<LinePart> currentLine;
        
        ArrayList<ArrayList<LinePart>> resultList = new ArrayList<>();
        
        currentLine = new ArrayList<>();
        
        int i;
        
        for( i = 0; i < lineParts.size(); i++ ) {
            NaturalLanguagePerception.LinePart iterationlinePart = lineParts.get(i);
            
            currentLine.add(iterationlinePart);
            
            if (isSentenceSpliter(iterationlinePart)) {
                resultList.add(currentLine);
                
                currentLine = new ArrayList<>();
            }
        }
        
        if(currentLine.size() != 0) {
            resultList.add(currentLine);
            currentLine = new ArrayList<>();
        }
        
        return resultList;
    }
    
    private static boolean isSentenceSpliter(NaturalLanguagePerception.LinePart linePart) {
        if (linePart.type != NaturalLanguagePerception.LinePart.EnumType.SIGN) {
            return false;
        }
        
        return linePart.content.equals(".") || linePart.content.equals(",") || linePart.content.equals(";") || linePart.content.equals("!") || linePart.content.equals("?");
    }
    static private String translateLinepartsToNareses(ArrayList<LinePart> lineparts, String prefix) {
        String translated = "";
        
        int i = 0;
        for (LinePart iterationLinePart : lineparts) {
            boolean lastOne = i == (lineparts.size()-1);
            
            translated += translateLinepartToNareses(iterationLinePart, prefix);
            
            if (!lastOne) {
                translated += ", ";
            }
            
            i++;
        }
        
        return "<(*," + translated + ") --> linepart>. :|:";
    }
    
    static private String translateLinepartToNareses(LinePart linepart, String prefix) {
        if (linepart.type == LinePart.EnumType.SIGN) {
            return "'" + linepart.content + "'";
        }
        else {
            return withorWithoutPrefix(linepart.content, prefix);
        }
            
    }
    
    static private String withorWithoutPrefix(String content, String prefix) {
        if (prefix.isEmpty()) {
            return content;
        }
        return prefix + "-" + content;
    }
    
    static private boolean isSplitSign(char sign) {
        return StringUtil.containsChar(splitSigns, sign);
    }
    
    static private final char[] splitSigns = new char[]{' ', '.', ',', '?', '!', ';', ':', '/', '\\', '%', '(', ')', '-', '+', '*', '\'', '\"', '=', '{', '}', '(', ')', '<', '>'};
}