/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.customProperties.HyperSchemaFactoryWrapper;
import nars.Memory;
import nars.term.Term;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * http://wiki.fasterxml.com/JacksonJsonSchemaGeneration
 * https://github.com/FasterXML/jackson-module-jsonSchema*
 */
public class JsonIO {

    static final ObjectMapper fieldMapper = new ObjectMapper()

            .enableDefaultTyping()

            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)

            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .registerModule(new SimpleModule().addSerializer(StackTraceElement.class, new ToStringSerializer()))
            //.registerModule(new SimpleModule().addSerializer(Term.class, new ToStringSerializer()))
            ;

    static final ObjectMapper fieldMapperIndent = fieldMapper.copy()
            .configure(SerializationFeature.INDENT_OUTPUT, true)

            ;

    static final ObjectWriter pretty = fieldMapperIndent
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .writerWithDefaultPrettyPrinter();


    public static String stringFromFields(Object obj) {
        try {
            return fieldMapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String stringFromFieldsPretty(Object obj) {
        try {
            return pretty.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }

    }

    public static void outputFromFields(Object obj, OutputStream o) {
        try {
            fieldMapperIndent.writeValue(o, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws JsonMappingException {


        ObjectMapper m = fieldMapper;

        HyperSchemaFactoryWrapper visitor = new HyperSchemaFactoryWrapper();
        //SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        m.acceptJsonFormatVisitor(m.constructType(Memory.class), visitor);

        JsonSchema jsonSchema = visitor.finalSchema();

        System.out.println(stringFromFieldsPretty(jsonSchema));

    }



    public static class TermSerializer extends RawSerializer {

        public TermSerializer() {
            super(Term.class);
        }


    }


}
//
//    Gson gson;
//
//    public JSONOutput(NAR reasoner, boolean pretty) {
//        super(reasoner);
//        init(pretty);
//    }
//
//    public JSONOutput(NAR reasoner, PrintStream ps, boolean pretty) {
//        super(reasoner, ps);
//
//        init(pretty);
//    }
//
//    protected void init(boolean pretty) {
//        GsonBuilder builder = new GsonBuilder()
//                .addSerializationExclusionStrategy(new ExclusionStrategy() {
//
//                    @Override
//                    public boolean shouldSkipField(FieldAttributes fa) {
//                        return fa.getName().equals("derivationChain");
//                    }
//
//                    @Override
//                    public boolean shouldSkipClass(Class<?> type) {
//                        return false;
//                    }
//                })
//                //.registerTypeAdapter(Id.class, new IdTypeAdapter())
//                //.setDateFormat(DateFormat.LONG)
//                //.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
//                //.setVersion(1.0)
//                .enableComplexMapKeySerialization()
//                .serializeNulls()
//                .disableHtmlEscaping();
//
//        if (pretty) {
//            builder.setPrettyPrinting();
//        }
//        gson = builder.create();
//
//    }
//
//    @Override
//    public String process(final Class c, Object[] os) {
//        Object o;
//        if (os.length == 1) o = os[0];
//        else o = os;
//
//        JsonElement t = gson.toJsonTree(o);
//        t.getAsJsonObject().addProperty("_", o.getClass().getSimpleName());
//        t.getAsJsonObject().addProperty("#", c.getSimpleName());
//        return gson.toJson(t);
//    }
//
//}
