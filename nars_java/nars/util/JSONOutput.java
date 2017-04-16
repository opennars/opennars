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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.PrintStream;
import nars.core.NAR;
import nars.io.TextOutput;

/**
 *
 * @author me
 */
public class JSONOutput extends TextOutput {

    Gson gson;

    public JSONOutput(NAR reasoner, boolean pretty) {
        super(reasoner);
        init(pretty);
    }
    
    public JSONOutput(NAR reasoner, PrintStream ps, boolean pretty) {
        super(reasoner, ps);
        
        init(pretty);
    }
    
    protected void init(boolean pretty) {
        GsonBuilder builder = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {

                    @Override
                    public boolean shouldSkipField(FieldAttributes fa) {
                        //System.out.println(fa.getDeclaredClass() + 
                                //" " + fa.getName());
                        if (fa.getName().equals("derivationChain")) {
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> type) {
                        return false;
                    }
                })
                //.registerTypeAdapter(Id.class, new IdTypeAdapter())
                //.setDateFormat(DateFormat.LONG)
                //.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                //.setVersion(1.0)                
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .disableHtmlEscaping();

        if (pretty) {
            builder.setPrettyPrinting();
        }
        gson = builder.create();
        
    }

    @Override
    public String process(final Class c, Object o) {
        JsonElement t = gson.toJsonTree(o);
        t.getAsJsonObject().addProperty("_", o.getClass().getSimpleName());
        t.getAsJsonObject().addProperty("#", c.getSimpleName());
        return gson.toJson(t);
    }

}
