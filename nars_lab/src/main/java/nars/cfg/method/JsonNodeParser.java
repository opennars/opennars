///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package nars.cfg.method;
//
//import jdk.nashorn.internal.ir.ObjectNode;
//import nars.cfg.method.CGClass;
//import nars.cfg.method.CGMethod;
//
//import java.util.List;
//
///**
// *
// * @author nmalik
// */
//public class JsonNodeParser {
//
//    protected static void safePut(ObjectNode object, String key, String value) {
//        if (value != null) {
//            object.put(key, value);
//        }
//    }
//
//    protected static void put(ObjectNode object, String key, JsonNode value) {
//        if (value != null) {
//            object.put(key, value);
//        }
//    }
//
//    public static ObjectNode objectNode() {
//        return new ObjectNode(JsonNodeFactory.instance);
//    }
//
//    /**
//     * Takes input className and splits it into package and name (simple name) and returns in new ObjectNode with
//     * following structure: {class:{package:<package>,name:<simple-name>}}
//     *
//     * @param className
//     * @return
//     */
//    public static JsonNode toJsonFromClass(String className) {
//        if ("void".equals(className)) {
//            return new TextNode(className);
//        }
//
//        String pkg = className.substring(0, className.lastIndexOf('.'));
//        String name = className.substring(className.lastIndexOf('.') + 1);
//
//        ObjectNode output = objectNode();
//        ObjectNode clazz = output.objectNode();
//        put(output, "class", clazz);
//        safePut(clazz, "package", pkg);
//        safePut(clazz, "name", name);
//
//        return output;
//    }
//
//    public static JsonNode toJson(List<CGClass> classes) {
//        ArrayNode output = new ArrayNode(JsonNodeFactory.instance);
//        for (CGClass c : classes) {
//            output.add(toJson(c));
//        }
//        return output;
//    }
//
//    public static JsonNode toJson(CGClass clazz) {
//        ObjectNode output = objectNode();
//
//        safePut(output, "className", clazz.className);
//        safePut(output, "extends", clazz.superClassName);
//
//        ArrayNode methods = output.arrayNode();
//        put(output, "methods", methods);
//
//        for (CGMethod m : clazz.methods) {
//            methods.add(toJson(m));
//        }
//
//        return output;
//    }
//
//    public static JsonNode toJson(CGMethod m) {
//        ObjectNode output = objectNode();
//        output.put("methodName", m.methodName);
//
//        ArrayNode arguments = toArray(m.argumentTypes);
//        if (arguments != null) {
//            output.put("arguments", arguments);
//        }
//
//        ArrayNode throwz = toArray(m.throwing);
//        if (throwz != null) {
//            output.put("throws", throwz);
//        }
//
//        output.put("returns", m.returnType);
//
//        if (m.invokedBy != null && !m.invokedBy.isEmpty()) {
//            ArrayNode invokedBy = output.arrayNode();
//            output.put("invokedBy", invokedBy);
//            for (CGMethod ib : m.invokedBy) {
//                invokedBy.add(toJson(ib));
//            }
//        }
//
//        return output;
//    }
//
//    public static ArrayNode toArray(List<String> strings) {
//        if (strings != null && !strings.isEmpty()) {
//            ArrayNode output = new ArrayNode(JsonNodeFactory.instance);
//            for (String string : strings) {
//                output.add(string);
//            }
//            return output;
//        } else {
//            return null;
//        }
//    }
// }
