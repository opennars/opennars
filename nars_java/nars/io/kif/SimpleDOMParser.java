/*
 * @(#)SimpleDOMParser.java
 * From DevX
 * http://www.devx.com/xml/Article/10114
 * Further modified for Articulate Software by Adam Pease 12/2005
 */
package nars.io.kif;

import java.util.Stack;
import java.io.*;

/** *****************************************************************   
 * <code>SimpleDOMParser</code> is a highly-simplified XML DOM
 * parser.
 */
public class SimpleDOMParser {
    private static final int[] cdata_start = {'<', '!', '[', 'C', 'D', 'A', 'T', 'A', '['};
    private static final int[] cdata_end = {']', ']', '>'};

    private Reader reader;
    private Stack elements;
    private SimpleElement currentElement;

    /** *****************************************************************
    */
    public SimpleDOMParser() {
        elements = new Stack();
        currentElement = null;
    }

    /** *****************************************************************
    */
    public SimpleElement parse(Reader reader) throws IOException {

        this.reader = reader;
        skipPrologs();          // skip xml declaration or DocTypes
        while (true) {
            int index;
            String tagName;
            
            String currentTag = readTag().trim();                               // remove the prepend or trailing white spaces
            if (currentTag.startsWith("</")) {                                  // close tag
                tagName = currentTag.substring(2, currentTag.length()-1);
                if (currentElement == null)                                     // no open tag
                    throw new IOException("Got close tag '" + tagName +
                                    "' without open tag.");                
                if (!tagName.equals(currentElement.getTagName()))               // close tag does not match with open tag
                    throw new IOException("Expected close tag for '" +
                                    currentElement.getTagName() + "' but got '" +
                                    tagName + "'.");
                if (elements.empty()) 
                    return currentElement;                                      // document processing is over
                else                                                            // pop up the previous open tag
                    currentElement = (SimpleElement)elements.pop();
            }
            else {                                                              // open tag or tag with both open and close tags                        
                index = currentTag.indexOf(" ");
                if (index < 0) {                                                // tag with no attributes                    
                    if (currentTag.endsWith("/>")) {                            // close tag as well                        
                        tagName = currentTag.substring(1, currentTag.length()-2);
                        currentTag = "/>";
                    } else {                                                    // open tag                        
                        tagName = currentTag.substring(1, currentTag.length()-1);
                        currentTag = "";
                    }
                } 
                else {                                                          // tag with attributes                        
                    tagName = currentTag.substring(1, index);
                    currentTag = currentTag.substring(index+1);
                }              
                SimpleElement element = new SimpleElement(tagName);             // create new element
                
                boolean isTagClosed = false;                                    // parse the attributes
                while (currentTag.length() > 0) {                               // remove the prepend or trailing white spaces                    
                    currentTag = currentTag.trim();
                    if (currentTag.equals("/>")) {                              // close tag                                
                        isTagClosed = true;
                        break;
                    } 
                    else 
                        if (currentTag.equals(">"))                           // open tag                                
                            break;                        
                    index = currentTag.indexOf("=");
                    if (index < 0) 
                        throw new IOException("Invalid attribute for tag '" +
                                            tagName + "'.  With current tag=" + currentTag);                        
                    
                    String attributeName = currentTag.substring(0, index);    // get attribute name
                    currentTag = currentTag.substring(index+1);
                    
                    String attributeValue;                                    // get attribute value
                    boolean isQuoted = true;
                    if (currentTag.startsWith("\"")) {
                        index = currentTag.indexOf('"', 1);
                    } 
                    else 
                        if (currentTag.startsWith("'")) {
                            index = currentTag.indexOf('\'', 1);
                        } 
                        else {
                            isQuoted = false;
                            index = currentTag.indexOf(' ');
                            if (index < 0) {
                                index = currentTag.indexOf('>');
                                if (index < 0) 
                                    index = currentTag.indexOf('/');                                
                            }
                        }
                    if (index < 0)
                            throw new IOException("Invalid attribute for tag '" +
                                            tagName + "'.  With current tag=" + currentTag);                        
                    if (isQuoted)
                        attributeValue = currentTag.substring(1, index);
                    else
                        attributeValue = currentTag.substring(0, index);

                    element.setAttribute(attributeName, attributeValue);      // add attribute to the new element
                    currentTag = currentTag.substring(index+1);
                }
                
                if (!isTagClosed)                                  // read the text between the open and close tag
                    element.setText(readText());                                                         
                if (currentElement != null)                        // add new element as a child element of the current element
                    currentElement.addChildElement(element);
                if (!isTagClosed) {
                    if (currentElement != null) 
                        elements.push(currentElement);                            
                    currentElement = element;
                } 
                else 
                    if (currentElement == null)                    // only has one tag in the document                            
                        return element;                    
            }
        }
    }

    /** *****************************************************************
    */
    private int peek() throws IOException {

        reader.mark(1);
        int result = reader.read();
        reader.reset();

        return result;
    }

    /** *****************************************************************
    */
    private void peek(int[] buffer) throws IOException {

        reader.mark(buffer.length);
        for (int i=0; i<buffer.length; i++) {
                buffer[i] = reader.read();
        }
        reader.reset();
    }

    /** *****************************************************************
    */
    private void skipWhitespace() throws IOException {

        while (Character.isWhitespace((char)peek())) {
            reader.read();
        }
    }

    /** *****************************************************************
    */
    private void skipProlog() throws IOException {
        
        reader.skip(2);                        // skip "<?" or "<!"
        while (true) {
            int next = peek();

            if (next == '>') {
                reader.read();
                break;
            } else if (next == '<') {           // nesting prolog
                    
                skipProlog();
            } else {
                reader.read();
            }
        }
    }

    /** *****************************************************************
    */
    private void skipPrologs() throws IOException {

        while (true) {
            skipWhitespace();
            int[] next = new int[2];
            peek(next);
            if (next[0] != '<') 
                throw new IOException("Expected '<' but got '" + (char)next[0] + "'.");           
            if ((next[1] == '?') || (next[1] == '!')) 
                skipProlog();
            else
                break;            
        }
    }

    /** *****************************************************************
    */
    private String readTag() throws IOException {

        skipWhitespace();
        StringBuffer sb = new StringBuffer();
        int next = peek();
        if (next != '<') 
            throw new IOException("Expected < but got " + (char)next);        
        sb.append((char)reader.read());
        while (peek() != '>') 
            sb.append((char)reader.read());        
        sb.append((char)reader.read());

        return sb.toString();
    }

    /** ***************************************************************** 
     * Convert ampersand character elements to reserved characters.
     */
    public static String convertToReservedCharacters(String input) {

        if (input == null || input == "") 
            return "";
        input = input.replaceAll("&gt;",">");
        input = input.replaceAll("&lt;","<");
        return input;
    }

    /** ***************************************************************** 
     * Convert reserved characters to ampersand character elements.
     */
    public static String convertFromReservedCharacters(String input) {

        if (input == null || input == "") 
            return "";
        input = input.replaceAll(">","&gt;");
        input = input.replaceAll("<","&lt;");
        return input;
    }

    /** *****************************************************************
    */
    private String readText() throws IOException {

        StringBuffer sb = new StringBuffer();
        int[] next = new int[cdata_start.length];
        peek(next);
        if (compareIntArrays(next, cdata_start) == true) {      // CDATA            
            reader.skip(next.length);
            int[] buffer = new int[cdata_end.length];
            while (true) {
                peek(buffer);
                if (compareIntArrays(buffer, cdata_end) == true) {
                    reader.skip(buffer.length);
                    break;
                } 
                else 
                    sb.append((char)reader.read());               
            }
        } else {
            while (peek() != '<') 
                sb.append((char)reader.read());                
        }
        return sb.toString();
    }

    /** *****************************************************************
    */
    private boolean compareIntArrays(int[] a1, int[] a2) {

        if (a1.length != a2.length)
            return false;
        for (int i=0; i<a1.length; i++) {
            if (a1[i] != a2[i]) 
                return false;               
        }
        return true;
    }

    /** *****************************************************************
    */
    public static void main(String[] args) {

        /** String fname = "";
        try {
//            String _projectFileName = "projects-energy.xml";
//            fname = KBmanager.getMgr().getPref("baseDir") + File.separator + _projectFileName;
            fname = "test.xml";
            System.out.println(fname);
            File f = new File(fname);
            if (!f.exists()) 
                return;
            BufferedReader br = new BufferedReader(new FileReader(fname));
            SimpleDOMParser sdp = new SimpleDOMParser();
            SimpleElement se = sdp.parse(br);
            System.out.println(se.toString());            
        } 
        catch (java.io.IOException e) {
                System.out.println("Error in main(): IO exception parsing file " + fname);
        }  */
        String test = "<P>hi<P>";
        String converted = SimpleDOMParser.convertFromReservedCharacters(test);
        System.out.println(converted);
        System.out.println(SimpleDOMParser.convertToReservedCharacters(converted));
    }
}
