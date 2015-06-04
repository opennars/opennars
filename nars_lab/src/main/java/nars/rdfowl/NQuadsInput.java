package nars.rdfowl;

import nars.NAR;
import nars.gui.NARSwing;
import nars.io.out.TextOutput;
import nars.model.impl.Default;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.nal.nal8.Operation;
import nars.nal.term.Atom;
import nars.nal.term.Term;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by me on 6/4/15.
 */
public class NQuadsInput {


    private final static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static String parentTagName = null;

    private final Map<String, Entity> entities = new HashMap();
    private final NAR nar;

    public NQuadsInput(NAR n, String nqLoc) throws Exception {
        this(n);
        input(new File(nqLoc));
    }

    public NQuadsInput(NAR n) {
        this.nar = n;
    }

    final Pattern nQuads = Pattern.compile("((?:\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?:@\\w+(?:-\\w+)?|\\^\\^<[^>]+>)?)|<[^>]+>|\\_\\:\\w+|\\.)");

    static protected class Entity {

        public String name;
        public final List<String[]> attributes = new LinkedList();

        public Entity() {
        }

        public void setName(String name) {
            this.name = name;
        }

        private void addAttribute(String key, String value) {
            attributes.add(new String[]{key, value});
        }

        private String getName() {
            return name;
        }

    }

    /**
     * These parsing rules were devised by physically looking at the OWL file
     * and figuring out what goes where. This should by no means be considered a
     * generalized way to parse OWL files.
     *
     * Parsing rules:
     *
     * owl:Class@rdf:ID = entity (1), type=Wine optional:
     * owl:Class/rdfs:subClassOf@rdf:resource = entity (2), type=Wine (2) --
     * parent --> (1) if owl:Class/rdfs:subClassOf has no attributes, ignore if
     * no owl:Class/rdfs:subClassOf entity, ignore it
     * owl:Class/owl:Restriction/owl:onProperty@rdf:resource related to
     * owl:Class/owl:Restriction/owl:hasValue@rdf:resource
     *
     * Region@rdf:ID = entity, type=Region optional:
     * Region/locatedIn@rdf:resource=entity (2), type=Region (2) -- parent --
     * (1) owl:Class/rdfs:subClassOf/owl:Restriction - ignore
     *
     * WineBody@rdf:ID = entity, type=WineBody WineColor@rdf:ID = entity,
     * type=WineColor WineFlavor@rdf:ID = entity, type=WineFlavor
     * WineSugar@rdf:ID = entity, type=WineSugar Winery@rdf:ID = entity,
     * type=Winery WineGrape@rdf:ID = entity, type=WineGrape
     *
     * Else if no namespace, this must be a wine itself, capture as entity:
     * ?@rdf:ID = entity, type=Wine all subtags are relations: tagname =
     * relation_name tag@rdf:resource = target entity
     */
    public void input(File f) throws Exception {
//        XMLInputFactory factory = XMLInputFactory.newInstance();
//        XMLStreamReader parser = factory.createXMLStreamReader(
//                new FileInputStream(f));
//        int depth = 0;
//        for (;;) {
//            int event = parser.next();
//            if (event == XMLStreamConstants.END_DOCUMENT) {
//
//                break;
//            }
//            switch (event) {
//                case XMLStreamConstants.START_ELEMENT:
//                    depth++;
//                    String tagName = formatTag(parser.getName());
//
//                    if (tagName.equalsIgnoreCase("owl:Class")) {
//                        processTag(parser, new TagProcessor() {
//                            @Override
//                            public void execute(XMLStreamReader parser) {
//
//                                String tagName = formatTag(parser.getName());
//
//                                if (tagName.equalsIgnoreCase("owl:Class")) {
//                                    //String name = parser.getAttributeValue(RDF_URI, "ID");
//                                    String name = parser.getAttributeValue(0);
//
//                                    if (name != null) {
//                                        Entity classEntity = new Entity();
//                                        parentTagName = name;
//                                        classEntity.setName(parentTagName);
//                                        classEntity.addAttribute("Type", "Class");
//
//                                        //saveEntity(classEntity);
//                                    }
//                                } else if (tagName.equalsIgnoreCase("rdfs:subClassOf")) {
//                                    //String name = parser.getAttributeValue(RDF_URI, "resource");
//                                    String name = parser.getAttributeValue(0);
//                                    if (name != null) {
//                                        Entity superclassEntity = new Entity();
//
//                                        superclassEntity.setName(name);
//                                        superclassEntity.addAttribute("Type", name);
//
//                                        //saveEntity(superclassEntity);
//                                        input(parentTagName, superclassEntity.getName(), "parentOf");
//                                        parentTagName = null;
//                                    }
//                                }
//                            }
//                        });
////                    } else if (tagName.equals("Region")) {
////                        processTag(parser, new TagProcessor() {
////                            public void execute(XMLStreamReader parser) {
////                                String tagName = formatTag(parser.getName());
////                                if (tagName.equals("Region")) {
////                                    Entity classEntity = new Entity();
////                                    parentTagName = parser.getAttributeValue(RDF_URI, "ID");
////                                    classEntity.setName(parentTagName);
////                                    classEntity.addAttribute("Type", "Region");
////                                    saveEntity(classEntity);
////                                } else if (tagName.equals("locatedIn")) {
////                                    Entity superclassEntity = new Entity();
////                                    String locationEntityName = parser.getAttributeValue(RDF_URI, "resource");
////                                    if (locationEntityName.startsWith("#")) {
////                                        locationEntityName = locationEntityName.substring(1);
////                                    }
////                                    superclassEntity.setName(locationEntityName);
////                                    superclassEntity.addAttribute("Type", "Region");
////                                    saveEntity(superclassEntity);
////                                    saveRelation(parentTagName, locationEntityName, "locatedIn");
////                                    parentTagName = null;
////                                }
////                            }
////                        });
////                    } else if (tagName.equals("WineBody")
////                            || tagName.equals("WineColor")
////                            || tagName.equals("WineFlavor")
////                            || tagName.equals("WineSugar")
////                            || tagName.equals("WineGrape")) {
////                        processTag(parser, new TagProcessor() {
////                            public void execute(XMLStreamReader parser) {
////                                Entity entity = new Entity();
////                                String name = parser.getAttributeValue(RDF_URI, "ID");
////                                if (name != null) {
////                                    entity.setName(name);
////                                    String tagName = parser.getLocalName();
////                                    Attribute attribute = null;
////                                    if (tagName.equals("WineBody")) {
////                                        attribute = new Attribute("Type", "Body");
////                                    } else if (tagName.equals("WineColor")) {
////                                        attribute = new Attribute("Type", "Color");
////                                    } else if (tagName.equals("WineFlavor")) {
////                                        attribute = new Attribute("Type", "Flavor");
////                                    } else if (tagName.equals("WineSugar")) {
////                                        attribute = new Attribute("Type", "Sugar");
////                                    } else if (tagName.equals("WineGrape")) {
////                                        attribute = new Attribute("Type", "Grape");
////                                    }
////                                    entity.addAttribute(attribute);
////                                    saveEntity(entity);
////                                }
////                            }
////                        });
////                    } else if (tagName.equals("vin:Winery")) {
////                        processTag(parser, new TagProcessor() {
////                            public void execute(XMLStreamReader parser) {
////                                String wineryName = parser.getAttributeValue(RDF_URI, "about");
////                                if (wineryName.startsWith("#")) {
////                                    wineryName = wineryName.substring(1);
////                                }
////                                Entity entity = new Entity();
////                                entity.setName(wineryName);
////                                entity.addAttribute("Type", "Winery");
////                                saveEntity(entity);
////                            }
////                        });
//                    } else if (!tagName.startsWith("owl:")) {
//                        Entity parentEntity = getEntity(tagName);
//                        //parentEntity = null;
//
//                        /*
//                         System.out.println(tagName + " " + parser.getAttributeName(0) + " " + parser.getAttributeValue(0) + " " + (parser.hasText() ? parser.getText() : ""));
//                         */
//                        if (parentEntity != null) {
//                            processTag(parser, new TagProcessor() {
//                                @Override
//                                public void execute(XMLStreamReader parser) {
//                                    String tagName = formatTag(parser.getName());
//                                    //String id = parser.getAttributeValue(RDF_URI, "ID");
//                                    String id = parser.getAttributeValue(0);
//
//                                    System.out.println("  " + tagName);
//                                    if (id != null && id.length() > 0) {
//                                        // this is the entity
//                                        Entity entity = new Entity();
//                                        entity.setName(id);
//                                        parentTagName = entity.getName();
//                                        input(entity);
//                                    } else {
//                                        // these are the relations
//                                        String relationName = tagName;
//                                        //String targetEntityName = parser.getAttributeValue(RDF_URI, "resource");
//                                        String targetEntityName = parser.getAttributeValue(0);
//                                        if (targetEntityName != null && targetEntityName.startsWith("#")) {
//                                            targetEntityName = targetEntityName.substring(1);
//                                        }
//                                        if (targetEntityName != null) {
//                                            input(parentTagName, targetEntityName, relationName);
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//
//                        if (!parser.hasText()) {
//
//                            String pred = formatTag(parser.getAttributeName(0));
//                            String obj = formatTag(new QName(parser.getAttributeValue(0)));
//
//                            System.out.println();
//                            System.out.println(tagName + " " + pred + " " + obj + " " +
//                                    parser.getAttributeValue(0));
//                            //(parser.hasText() ? parser.getText() : ""));
//                        }
//
//                    }
//                    break;
//                case XMLStreamConstants.END_ELEMENT:
//                    depth--;
//                    break;
//                default:
//                    break;
//            }
//            parser.close();
//        }

        List<String> items = new ArrayList(4);

        new Scanner(f).useDelimiter("\n").forEachRemaining(s -> {
            // remove unicode escaping:
            //	var regex = new RegExp('\\\\U[0-9a-fA-F]{8}|\\\\u[0-9a-fA-F]{4}', 'g');
            //https://github.com/j13z/rdf-nx-parser/blob/master/lib/rdf-nx-parser.js
/*

	// This thing does most of this module's work. See `regex.md` for details.
	var splitTokensRegex = /((?:"[^"\\]*(?:\\.[^"\\]*)*"(?:@\w+(?:-\w+)?|\^\^<[^>]+>)?)|<[^>]+>|\_\:\w+|\.)/g;

	return function (string, options) {

		var tokens = string.match(splitTokensRegex);
 */
            Matcher m = nQuads.matcher(s);
            int count = 0;
            items.clear();
            while(m.find()) {

//                System.out.println("Match number "
//                        + count);
//                System.out.println("start(): "
//                        + m.start());
//                System.out.println("end(): "
//                        + m.end());
                String t = s.substring(m.start(), m.end());
                items.add(t);
            }

            if (items.size() >= 3) {

                Atom subj = resource(items.get(0));
                Atom pred = resource(items.get(1));
                Term obj = resourceOrValue(items.get(2));
                if (subj != null && obj != null && pred != null) {
                    if (!subj.equals(obj)) //avoid equal subj & obj, if only namespace differs
                        input(subj, pred, obj);
                }
            }
        });
    }

    public Atom resource(String s) {
        if (s.startsWith("<") && s.endsWith(">")) {
            s = s.substring(1, s.length() - 1);

            if (s.contains("#")) {
                String[] a = s.split("#");
                String p = a[0]; //TODO handle the namespace
                s = a[1];
            }
            else {
                String[] a = s.split("/");
                if (a.length == 0) return null;
                s = a[a.length - 1];
            }
            return Atom.the(s, true);
        }
        else
            return null;
    }
    public Term resourceOrValue(String s) {
        Atom a = resource(s);
        if (a == null) {
            //...
        }
        return a;
    }

    abstract public static class TagProcessor {

        abstract protected void execute(XMLStreamReader parser);
    }


    static public Term atom(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash!=-1)
            uri = uri.substring(lastSlash + 1);

        switch(uri) {
            case "owl#Thing": uri = "thing"; break;
        }

        return Atom.the(uri);
    }

    // ====================== DB load/save methods =========================
    /**
     * Saves an entity to the database. Takes care of setting attribute_types
     * and attribute objects linked to the entity.
     *
     * @param entity the Entity to save.
     */
    protected void input(final Entity entity) {
        //entities.put(entity.getName(), entity);

        Term clas = atom(entity.getName());
        if (clas!=null)
            inputClass(clas);

        //System.out.println("Save: " + entity.getName());
//        // if entity already exists, don't save
//        long entityId = getEntityIdFromDb(entity.getName());
//        if (entityId == -1L) {
//            log.debug("Saving entity:" + entity.getName());
//            // insert the entity
//            KeyHolder entityKeyHolder = new GeneratedKeyHolder();
//            jdbcTemplate.update(new PreparedStatementCreator() {
//                public PreparedStatement createPreparedStatement(Connection conn)
//                        throws SQLException {
//                    PreparedStatement ps = conn.prepareStatement(
//                            "insert into entities(name) values (?)",
//                            Statement.RETURN_GENERATED_KEYS);
//                    ps.setString(1, entity.getName());
//                    return ps;
//                }
//            }, entityKeyHolder);
//            entityId = entityKeyHolder.getKey().longValue();
//            List<Attribute> attributes = entity.getAttributes();
//            for (Attribute attribute : attributes) {
//                saveAttribute(entityId, attribute);
//            }
//            // finally, always save the "english name" of the entity as an attribute
//            saveAttribute(entityId, new Attribute("EnglishName", getEnglishName(entity.getName())));
//        }
    }

    //TODO make this abstract for inserting the fact in different ways
    protected void inputClass(Term clas) {
        inputClassBelief(clas);
    }

    private void inputClassBelief(Term clas) {
        nar.believe(isAClass(clas));
    }

    public static final Atom owlClass = Atom.the("class");
    public static Term isAClass(Term clas) {
        return Instance.make(clas, owlClass);
    }

    /**
     * Saves an entity attribute to the database and links the attribute to the
     * specified entity id.
     *
     * @param entityId the entity id.
     * @param attribute the Attribute object to save.
     */
    private void saveAttribute(long entityId, Attribute attribute) {
//        // check to see if the attribute is defined, if not define it
//        long attributeId = 0L;
//        try {
//            attributeId = jdbcTemplate.queryForLong(
//                    "select id from attribute_types where attr_name = ?",
//                    new String[]{attribute.getName()});
//        } catch (IncorrectResultSizeDataAccessException e) {
//            KeyHolder keyholder = new GeneratedKeyHolder();
//            final String attributeName = attribute.getName();
//            jdbcTemplate.update(new PreparedStatementCreator() {
//                public PreparedStatement createPreparedStatement(Connection conn)
//                        throws SQLException {
//                    PreparedStatement ps = conn.prepareStatement(
//                            "insert into attribute_types(attr_name) values (?)");
//                    ps.setString(1, attributeName);
//                    return ps;
//                }
//            }, keyholder);
//            attributeId = keyholder.getKey().longValue();
//        }
//        jdbcTemplate.update(
//                "insert into attributes(entity_id, attr_id, value) values (?,?,?)",
//                new Object[]{entityId, attributeId, attribute.getValue()});
    }

    static final Atom parentOf = Atom.the("parentOf");
    static final Atom type = Atom.the("type");
    static final Atom subClassOf = Atom.the("subClassOf");
    static final Atom subPropertyOf = Atom.the("subPropertyOf");
    static final Atom equivalentClass = Atom.the("equivalentClass");
    static final Atom equivalentProperty = Atom.the("equivalentProperty");
    static final Atom inverseOf = Atom.the("inverseOf");
    static final Atom disjointWith = Atom.the("disjointWith");
    static final Atom domain = Atom.the("domain");
    static final Atom range = Atom.the("range");
    static final Atom sameAs = Atom.the("sameAs");

    /**
     * Saves the relation into the database. Both entities must exist if the
     * relation is to be saved. Takes care of updating relation_types as well.
     *
     */
    private void input(final Atom subject, final Atom predicate, final Term object) {

        //http://www.w3.org/TR/owl-ref/

        Term belief = null;

        if (predicate.equals(parentOf) || predicate.equals(type)
                ||predicate.equals(subClassOf)||predicate.equals(subPropertyOf)) {
            belief = (Inheritance.make(subject, object));
        }
        else if (predicate.equals(equivalentClass)) {
            belief = (Equivalence.make(subject, object));
        }
        else if (predicate.equals(sameAs)) {
            belief = (Similarity.make(subject, object));
        }
        else if (predicate.equals(domain)) {
            // PROPERTY domain CLASS
            //<PROPERTY($subj, $obj) ==> <$subj {-- CLASS>>.

            belief = nar.term(
                "<" + subject + "($subj,$obj) ==> <$subj {-- " + object + ">>"
            );
            System.err.println(belief);
        }
        else if (predicate.equals(range)) {
            // PROPERTY range CLASS
            //<PROPERTY($subj, $obj) ==> <$obj {-- CLASS>>.

        }
        else if (predicate.equals(equivalentProperty)) {

        }
        else if (predicate.equals(inverseOf)) {

        }
        else if (predicate.equals(disjointWith)) {

        }
        else {

            belief = (Operation.make(predicate,
                    Product.make(subject, object)));
        }

        if (belief!=null) {
            nar.believe(belief);
        }

//        // get the entity ids for source and target
//        long sourceEntityId = getEntityIdFromDb(sourceEntityName);
//        long targetEntityId = getEntityIdFromDb(targetEntityName);
//        if (sourceEntityId == -1L || targetEntityId == -1L) {
//            log.error("Cannot save relation: " + relationName + "("
//                    + sourceEntityName + "," + targetEntityName + ")");
//            return;
//        }
//        log.debug("Saving relation: " + relationName + "("
//                + sourceEntityName + "," + targetEntityName + ")");
//        // get the relation id
//        long relationTypeId = 0L;
//        try {
//            relationTypeId = jdbcTemplate.queryForInt(
//                    "select id from relation_types where type_name = ?",
//                    new String[]{relationName});
//        } catch (IncorrectResultSizeDataAccessException e) {
//            KeyHolder keyholder = new GeneratedKeyHolder();
//            jdbcTemplate.update(new PreparedStatementCreator() {
//                public PreparedStatement createPreparedStatement(Connection conn)
//                        throws SQLException {
//                    PreparedStatement ps = conn.prepareStatement(
//                            "insert into relation_types(type_name) values (?)",
//                            Statement.RETURN_GENERATED_KEYS);
//                    ps.setString(1, relationName);
//                    return ps;
//                }
//            }, keyholder);
//            relationTypeId = keyholder.getKey().longValue();
//        }
//        // save it
//        jdbcTemplate.update(
//                "insert into relations(src_entity_id, trg_entity_id, relation_id) values (?, ?, ?)",
//                new Long[]{sourceEntityId, targetEntityId, relationTypeId});
    }

    /**
     * Looks up the database to get the entity id given the name of the entity.
     * If the entity is not found, it returns -1.
     *
     * @return the entity id, or -1 of the entity.
     */
    private Entity getEntity(String name) {
        return entities.get(name);
    }

    // ======== String manipulation methods ========
    /**
     * Format the XML tag. Takes as input the QName of the tag, and formats it
     * to a namespace:tagname format.
     *
     * @param qname the QName for the tag.
     * @return the formatted QName for the tag.
     */
    private String formatTag(QName qname) {
        String prefix = qname.getPrefix();
        String suffix = qname.getLocalPart();

        suffix = suffix.replace("http://dbpedia.org/ontology/", "");

        if (prefix == null || prefix.length() == 0) {
            return suffix;
        } else {
            return prefix + ":" + suffix;
        }
    }

    /**
     * Split up Uppercase Camelcased names (like Java classnames or C++ variable
     * names) into English phrases by splitting wherever there is a transition
     * from lowercase to uppercase.
     *
     * @param name the input camel cased name.
     * @return the "english" name.
     */
    private String getEnglishName(String name) {
        StringBuilder englishNameBuilder = new StringBuilder();
        char[] namechars = name.toCharArray();
        for (int i = 0; i < namechars.length; i++) {
            if (i > 0 && Character.isUpperCase(namechars[i])
                    && Character.isLowerCase(namechars[i - 1])) {
                englishNameBuilder.append(' ');
            }
            englishNameBuilder.append(namechars[i]);
        }
        return englishNameBuilder.toString();
    }

    public static void main(String[] args) throws Exception {
        Default d = new Default(4096,128,8).setInternalExperience(null).level(6);
        d.inputsMaxPerCycle.set(1024);

        NAR n = new NAR(d);
        n.input("schizo(I)!");

        new TextOutput(n, System.out);


        new NQuadsInput(n, "/home/me/Downloads/dbpedia.n4");

        new NARSwing(n);

        //n.frame(5000);
    }
}

