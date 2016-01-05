//package nars.util.io;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.*;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;
//import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
//
//import java.io.IOException;
//import java.util.Map;
//
///**
// * JSON utilities
// */
//public class JSON {
//
//    public static final ObjectMapper om = new ObjectMapper()
//
//            .enableDefaultTyping()
//
//            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
//
//            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
//            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
//            .registerModule(new SimpleModule().addSerializer(StackTraceElement.class, new ToStringSerializer()));
//
//    static final ObjectWriter omPretty = om.copy().writerWithDefaultPrettyPrinter();
//
//    public static final ObjectMapper omDeep = new ObjectMapper()
//
//            .enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, "_")
//
//            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
//            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
//            //.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
//
//            /*.enable(SerializationFeature.WRAP_ROOT_VALUE)
//            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)*/
//
//            //.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
//            .enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
//            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
//            .enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
//            //.enable(MapperFeature.USE_STATIC_TYPING)
//            .enable(MapperFeature.AUTO_DETECT_FIELDS)
//            .enable(MapperFeature.AUTO_DETECT_CREATORS)
//            .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
//            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
//            .enable(MapperFeature.INFER_PROPERTY_MUTATORS)
//            .disable(MapperFeature.AUTO_DETECT_GETTERS)
//            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
//            .disable(MapperFeature.USE_GETTERS_AS_SETTERS)
//
//            //.enable(MapperFeature.USE_STD_BEAN_NAMING)
//
//
//            .enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
//            //.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
//            .enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
//            //.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
//            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
//            .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
//            .enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
//
//
//            //.disable(MapperFeature.USE_ANNOTATIONS)
//            .registerModule(new SimpleModule().addSerializer(new ByteArraySerializer()))
//            .registerModule(new SimpleModule().addSerializer(StackTraceElement.class, new ToStringSerializer()))
//            //.findAndRegisterModules()
//    ;
//
////
//    public static JsonNode toJSON(String json) throws IOException {
//        return om.readTree(json);
//    }
//
//    public static Map<String,Object> toMap(String json) {
//        try {
//            return om.readValue(json, Map.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
////
////    public static Object toObj(String json) {
////        try {
////            return def.anyFrom(json);
////        } catch (IOException e) {
////            e.printStackTrace();
////            return null;
////        }
////    }
////
//    public static String stringFrom(Object obj) {
//        try {
//            return om.writeValueAsString(obj);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static String pretty(JsonNode j) {
//        if (j == null)
//            return "['null']";
//
//        try {
//            return omPretty.writeValueAsString(j);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
////    public static String stringFromUnreflected(Object obj) {
////        try {
////            return unRef.asString(obj);
////        } catch (IOException e) {
////            e.printStackTrace();
////            return null;
////        }
////    }
////
////
////    public static Map<String,Object> mapFrom(Object obj) {
////        try {
////            return def.mapFrom(obj);
////        } catch (IOException e) {
////            e.printStackTrace();
////            return null;
////        }
////    }
//
//
// }
