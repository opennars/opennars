package nars.util.rdfowl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import nars.core.NAR;
import nars.core.build.Default;
import nars.io.TextOutput;
import nars.util.PrintWriterInput;

/**
 * Simple combined OWL/RDF-S XML parser
 *
 * Code from:
 * http://sujitpal.blogspot.com/2008/05/parsing-owl-xml-with-stax.html
 *
 * @author me
 */
public class OWLInput extends PrintWriterInput {

    private final static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static String parentTagName = null;

    private final Map<String, Entity> entities = new HashMap();

    public OWLInput(String owlFileLocation) throws Exception {
        super();

        parseAndLoadData(new File(owlFileLocation));
    }

    protected class Entity {

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
    public void parseAndLoadData(File f) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(
                new FileInputStream(f));
        int depth = 0;
        for (;;) {
            int event = parser.next();
            if (event == XMLStreamConstants.END_DOCUMENT) {
                close();
                break;
            }
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
                    String tagName = formatTag(parser.getName());

                    if (tagName.equalsIgnoreCase("owl:Class")) {
                        processTag(parser, new TagProcessor() {
                            @Override
                            public void execute(XMLStreamReader parser) {

                                String tagName = formatTag(parser.getName());

                                if (tagName.equalsIgnoreCase("owl:Class")) {
                                    //String name = parser.getAttributeValue(RDF_URI, "ID");
                                    String name = parser.getAttributeValue(0);

                                    if (name != null) {
                                        Entity classEntity = new Entity();
                                        parentTagName = name;
                                        classEntity.setName(parentTagName);
                                        classEntity.addAttribute("Type", "Class");

                                        //saveEntity(classEntity);
                                    }
                                } else if (tagName.equalsIgnoreCase("rdfs:subClassOf")) {
                                    //String name = parser.getAttributeValue(RDF_URI, "resource");
                                    String name = parser.getAttributeValue(0);
                                    if (name != null) {
                                        Entity superclassEntity = new Entity();

                                        superclassEntity.setName(name);
                                        superclassEntity.addAttribute("Type", name);

                                        //saveEntity(superclassEntity);
                                        saveRelation(parentTagName, superclassEntity.getName(), "parentOf");
                                        parentTagName = null;
                                    }
                                }
                            }
                        });
//                    } else if (tagName.equals("Region")) {
//                        processTag(parser, new TagProcessor() {
//                            public void execute(XMLStreamReader parser) {
//                                String tagName = formatTag(parser.getName());
//                                if (tagName.equals("Region")) {
//                                    Entity classEntity = new Entity();
//                                    parentTagName = parser.getAttributeValue(RDF_URI, "ID");
//                                    classEntity.setName(parentTagName);
//                                    classEntity.addAttribute("Type", "Region");
//                                    saveEntity(classEntity);
//                                } else if (tagName.equals("locatedIn")) {
//                                    Entity superclassEntity = new Entity();
//                                    String locationEntityName = parser.getAttributeValue(RDF_URI, "resource");
//                                    if (locationEntityName.startsWith("#")) {
//                                        locationEntityName = locationEntityName.substring(1);
//                                    }
//                                    superclassEntity.setName(locationEntityName);
//                                    superclassEntity.addAttribute("Type", "Region");
//                                    saveEntity(superclassEntity);
//                                    saveRelation(parentTagName, locationEntityName, "locatedIn");
//                                    parentTagName = null;
//                                }
//                            }
//                        });
//                    } else if (tagName.equals("WineBody")
//                            || tagName.equals("WineColor")
//                            || tagName.equals("WineFlavor")
//                            || tagName.equals("WineSugar")
//                            || tagName.equals("WineGrape")) {
//                        processTag(parser, new TagProcessor() {
//                            public void execute(XMLStreamReader parser) {
//                                Entity entity = new Entity();
//                                String name = parser.getAttributeValue(RDF_URI, "ID");
//                                if (name != null) {
//                                    entity.setName(name);
//                                    String tagName = parser.getLocalName();
//                                    Attribute attribute = null;
//                                    if (tagName.equals("WineBody")) {
//                                        attribute = new Attribute("Type", "Body");
//                                    } else if (tagName.equals("WineColor")) {
//                                        attribute = new Attribute("Type", "Color");
//                                    } else if (tagName.equals("WineFlavor")) {
//                                        attribute = new Attribute("Type", "Flavor");
//                                    } else if (tagName.equals("WineSugar")) {
//                                        attribute = new Attribute("Type", "Sugar");
//                                    } else if (tagName.equals("WineGrape")) {
//                                        attribute = new Attribute("Type", "Grape");
//                                    }
//                                    entity.addAttribute(attribute);
//                                    saveEntity(entity);
//                                }
//                            }
//                        });
//                    } else if (tagName.equals("vin:Winery")) {
//                        processTag(parser, new TagProcessor() {
//                            public void execute(XMLStreamReader parser) {
//                                String wineryName = parser.getAttributeValue(RDF_URI, "about");
//                                if (wineryName.startsWith("#")) {
//                                    wineryName = wineryName.substring(1);
//                                }
//                                Entity entity = new Entity();
//                                entity.setName(wineryName);
//                                entity.addAttribute("Type", "Winery");
//                                saveEntity(entity);
//                            }
//                        });
                    } else if (!tagName.startsWith("owl:")) {
                        Entity parentEntity = getEntity(tagName);
                        //parentEntity = null;

                        /*
                         System.out.println(tagName + " " + parser.getAttributeName(0) + " " + parser.getAttributeValue(0) + " " + (parser.hasText() ? parser.getText() : ""));
                         */
                        if (parentEntity != null) {
                            processTag(parser, new TagProcessor() {
                                @Override
                                public void execute(XMLStreamReader parser) {
                                    String tagName = formatTag(parser.getName());
                                    //String id = parser.getAttributeValue(RDF_URI, "ID");
                                    String id = parser.getAttributeValue(0);

                                    System.out.println("  " + tagName);
                                    if (id != null && id.length() > 0) {
                                        // this is the entity
                                        Entity entity = new Entity();
                                        entity.setName(id);
                                        parentTagName = entity.getName();
                                        saveEntity(entity);
                                    } else {
                                        // these are the relations
                                        String relationName = tagName;
                                        //String targetEntityName = parser.getAttributeValue(RDF_URI, "resource");
                                        String targetEntityName = parser.getAttributeValue(0);
                                        if (targetEntityName != null && targetEntityName.startsWith("#")) {
                                            targetEntityName = targetEntityName.substring(1);
                                        }
                                        if (targetEntityName != null) {
                                            saveRelation(parentTagName, targetEntityName, relationName);
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        /*
                         System.out.println();
                         System.out.println(parser.getName() + " " + parser.getAttributeName(0) + " " + parser.getAttributeValue(0) + " " + (parser.hasText() ? parser.getText() : ""));                        
                         System.out.println();
                         */
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth--;
                    break;
                default:
                    break;
            }
            parser.close();
        }
    }

    abstract public static class TagProcessor {

        abstract protected void execute(XMLStreamReader parser);
    }

    /**
     * A tag processor template method which takes as input a closure that is
     * responsible for extracting the information from the tag and saving it to
     * the database. The contents of the closure is called inside the
     * START_DOCUMENT case of the template code.
     *
     * @param parser a reference to our StAX XMLStreamReader.
     * @param tagProcessor a reference to the Closure to process the tag.
     * @throws Exception if one is thrown.
     */
    private void processTag(XMLStreamReader parser, TagProcessor tagProcessor)
            throws Exception {
        int depth = 0;
        int event = parser.getEventType();
        String startTag = formatTag(parser.getName());
        FOR_LOOP:
        for (;;) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    String tagName = formatTag(parser.getName());
                    tagProcessor.execute(parser);
                    depth++;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    tagName = formatTag(parser.getName());
                    depth--;
                    if (tagName.equalsIgnoreCase(startTag) && depth == 0) {
                        break FOR_LOOP;
                    }
                    break;
                default:
                    break;
            }
            event = parser.next();
        }
    }

    public String getClassName(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        return uri.substring(lastSlash + 1);
    }

    // ====================== DB load/save methods =========================
    /**
     * Saves an entity to the database. Takes care of setting attribute_types
     * and attribute objects linked to the entity.
     *
     * @param entity the Entity to save.
     */
    private void saveEntity(final Entity entity) {
        //entities.put(entity.getName(), entity);

        String className = getClassName(entity.getName());
        append("<" + className + " --> class>.\n");

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

    /**
     * Saves the relation into the database. Both entities must exist if the
     * relation is to be saved. Takes care of updating relation_types as well.
     *
     * @param sourceEntityName the name of the source entity.
     * @param targetEntityName the name of the target entity.
     * @param relationName the name of the relation.
     */
    private void saveRelation(final String subject, final String object, final String predicate) {

        if ((subject == null) || (object == null)) {
            return;
        }
        if (predicate.equals("parentOf")) {
            append("<" + getClassName(subject) + " --> " + getClassName(object) + ">.\n");
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
     * @param entityName the name of the entity.
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
        NAR n = new Default().build();

        new TextOutput(n, System.out);

        //new NARSwing(n);
        n.addInput(new OWLInput("/home/me/Downloads/schemaorg.owl"));

    }
}
