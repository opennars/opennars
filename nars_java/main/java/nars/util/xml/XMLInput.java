package nars.util.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nars.core.NAR;
import nars.core.build.Default;
import nars.io.TextOutput;
import nars.util.PrintWriterInput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * XML DOM parser
 * http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
 */
public class XMLInput extends PrintWriterInput {

    private final String nodePrefix;

    //Node -> ID
    private final Map<Node, String> nodes = new HashMap();
    private final Set<String> elementTypes = new HashSet();

    final int maxWords = 4;

    public static void main(String[] args) throws Exception {
        NAR n = new Default().
                setConceptBagSize(8192).
                setTermLinkBagLevels(8).
                setTaskLinkBagLevels(8).
                build();

        new TextOutput(n, System.out);

        //new NARSwing(n);
        n.start(0);

        //new XMLInput(n, "/home/me/Downloads/schemaorg.owl","");
        n.addInput(new XMLInput("/home/me/Downloads/Semantic_Web.rdf", ""));

    }

    public XMLInput(String filename, String nodePrefix) throws IOException, ParserConfigurationException, SAXException {
        this(new File(filename), nodePrefix);
    }

    public XMLInput(File f, String nodePrefix) throws IOException, ParserConfigurationException, SAXException {
        super();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);

        this.nodePrefix = nodePrefix;

        Element root = doc.getDocumentElement();
        traverse(root);

        out.close();
    }

    public String escape(String x) {
        return x.replaceAll(":", "\u25B8").replaceAll(" ", "\u2581")
                .replaceAll("#", "\u25B8") //TODO find a different unicode char
                .replaceAll("&", "\u25B8") //TODO find a different unicode char
                .replaceAll("/", "\u25B8") //TODO find a different unicode char
                .replaceAll("=", "\u25B8") //TODO find a different unicode char
                .replaceAll(";", "\u25B8") //TODO find a different unicode char
                .replaceAll("-", "\u25B8") //TODO find a different unicode char                
                .replaceAll("\\.", "\u25B8") //TODO find a different unicode char
                ;
    }

    private String[] tokenize(String value) {
        String v = value.replaceAll(",", " \uFFEB ").
                replaceAll("\\.", " \uFFED").
                replaceAll("\\!", " \uFFED"). //TODO alternate char
                replaceAll("\\?", " \uFFED") //TODO alternate char
                ;
        return v.split(" ");
    }

    public String elementType(String e) {
        String id = escape(e);
        if (elementTypes.contains(e)) {
            return id;
        }

        String ns = null;
        if (e.indexOf(':') != -1) {
            ns = e.split(":")[0];
        }

        //inherit from namespace        
        if ((ns != null) && (ns.length() > 0)) {
            append("<" + id + " --> " + ns + ">. %1.00;0.90%" + '\n');
        }
        elementTypes.add(e);

        return id;

    }

    public String addNode(Node n) {
        String ns = null;
        String nodeName = n.getNodeName();

        nodeName = elementType(nodeName);
        String id = nodePrefix + "_" + nodeName + "_" + nodes.size();
        nodes.put(n, id);

        //inherit from nodename
        append("<" + id + " --> " + nodeName + ">.\n");

        String parent = nodes.get(n.getParentNode());
        if (parent != null) {
            append("<(*," + id + "," + parent + ") --> xmlParent>.\n");
        }

        return id;
    }

    public String addNode(Node n, String value) {
        if (value == null) {
            return addNode(n);
        }

        if (value.indexOf(' ') == -1) {
            value = escape(value);
        } else {
            String[] words = tokenize(value);
            if (words.length > 0) {
                value = "(*,";
                for (String w : words) {
                    w = w.trim();
                    if (w.length() > 0) {
                        value += escape(w).toLowerCase() + ",";
                    }
                }
                if (words.length > maxWords) {
                    return null;
                }
                if (value.endsWith(",")) {
                    value = value.substring(0, value.length() - 1);
                }
                value += ")";
            } else {
                return null;
            }
        }
        if (value.length() == 0) {
            return null;
        }

        String id = addNode(n);
        append("<(*," + id + "," + value + ") --> value>.\n");
        return id;
    }


    /*
     private void printlnCommon(Node n) {
     out.print(" nodeName=\"" + n.getNodeName() + "\"");

     String val = n.getNamespaceURI();
     if (val != null) {
     out.print(" uri=\"" + val + "\"");
     }

     val = n.getPrefix();

     if (val != null) {
     out.print(" pre=\"" + val + "\"");
     }

     val = n.getLocalName();
     if (val != null) {
     out.print(" local=\"" + val + "\"");
     }

     String o = getNodeTextValue(n);
     //        if (o!=null)
     //            out.print(" value=\"" + o + "\"");
        
     out.println();
     }
     */
    public static String getNodeTextValue(final Node n) {
        String val = n.getNodeValue();
        if (val != null) {
            if (val.trim().isEmpty()) {
                // Whitespace
                return null;
            } else {
                return n.getNodeValue();
            }
        }
        return null;
    }

    /*
     String basicIndent = " ";
     private void outputIndentation() {
     for (int i = 0; i < indent; i++) {
     out.print(basicIndent);
     }
     }
     */
    protected void traverse(Node n) {
        //outputIndentation();
        int type = n.getNodeType();

        switch (type) {
            case Node.ATTRIBUTE_NODE:
                //out.println("ATTR: " + n.getNodeName() + " = " + getNodeTextValue(n));
                addNode(n, getNodeTextValue(n));
                break;

            case Node.CDATA_SECTION_NODE:
                //System.err.print("CDATA:" + " " + getNodeTextValue(n));
                //printlnCommon(n);
                break;

            case Node.TEXT_NODE:
                addNode(n, getNodeTextValue(n));
                //if (v != null) {
                //out.print("TEXT: " + v + "\n");
                //printlnCommon();
                //}
                break;

            case Node.ELEMENT_NODE:
                //out.print("ELEM:");
                //printlnCommon(n);
                addNode(n);

                //indent += 2;
                NamedNodeMap atts = n.getAttributes();
                for (int i = 0; i < atts.getLength(); i++) {
                    traverse(atts.item(i));
                }
                //indent -= 2;
                break;

            /*case Node.COMMENT_NODE:
             out.print("COMM:");
             printlnCommon(n);
             break;*/

            /*case Node.DOCUMENT_FRAGMENT_NODE:
             out.print("DOC_FRAG:");
             printlnCommon(n);
             break;*/

            /*case Node.DOCUMENT_NODE:
             out.print("DOC:");
             printlnCommon(n);
             break;*/

            /*case Node.DOCUMENT_TYPE_NODE:
             out.print("DOC_TYPE:");
             printlnCommon(n);
             NamedNodeMap nodeMap = ((DocumentType) n).getEntities();
             indent += 2;
             for (int i = 0; i < nodeMap.getLength(); i++) {
             Entity entity = (Entity) nodeMap.item(i);
             echo(entity);
             }
             indent -= 2;
             break;*/
            /*case Node.ENTITY_NODE:
             out.print("ENT:");
             printlnCommon(n);
             break;

             case Node.ENTITY_REFERENCE_NODE:
             out.print("ENT_REF:");
             printlnCommon(n);
             break;

             case Node.NOTATION_NODE:
             out.print("NOTATION:");
             printlnCommon(n);
             break;

             case Node.PROCESSING_INSTRUCTION_NODE:
             out.print("PROC_INST:");
             printlnCommon(n);
             break;
             */
            default:
                /*out.print("UNSUPPORTED NODE: " + type);
                 printlnCommon(n);*/
                break;
        }

        //indent++;
        if (type != Node.ATTRIBUTE_NODE) {
            for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
                traverse(child);
            }
        }
        //indent--;
    }

}
