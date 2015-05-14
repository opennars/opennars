/*
 * TextInput.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.io;

import com.google.common.collect.Iterators;
import nars.Events;
import nars.Global;
import nars.op.io.Echo;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * To read and write experience as Task streams
 */
public class TextInput extends Input.BufferedInput {


    //TODO
    public static class CachingTextInput extends TextInput {


//        final static Map<String, Iterable<Task>> cache = new ConcurrentHashMap<>(1024);
//
        public CachingTextInput(TextPerception p, String input) {

            super(p, input);
        }
//
//        @Override
//        protected void perceive(String line) {
//            Iterable<Task> x = cache.get(line);
//            if (x == null) {
//                Iterator<Task> y = super.perceive(line);
//                cache.put(line, x = Lists.newArrayList(y));
//            }
//
//            return Iterators.transform(x.iterator(), new Function<Task,Task>() {
//                @Nullable  @Override
//                public Task apply(Task input) {
//                    //provide a new copy for every input
//                    Task t = input.clone();
//                    if (t.sentence!=null)
//                        t.sentence.stamp.setCreationTime(Stamp.UNPERCEIVED);
//                    return t;
//                }
//            });
//        }
    }

    private final TextPerception perception;
    /**
     * Input experience from a file
     */
    protected BufferedReader input;
    
    //private boolean isLooping = false;
    
    public TextInput(TextPerception p, String input) {
        this(p, new BufferedReader(new StringReader(input)));
    }
    
    public TextInput(TextPerception p, File input) throws FileNotFoundException {
        this(p, new BufferedReader(new FileReader(input)));
    }
    
    public TextInput(TextPerception p, URL u) throws IOException {
        this(p, new BufferedReader(new InputStreamReader(u.openStream())));
    }
    
    public TextInput(TextPerception p, BufferedReader input) {
        this(p);

        setInput(input);
        load();
    }
    
    public TextInput(TextPerception p) {
        this.perception = p;
    }
    
    protected void setInput(BufferedReader input) {
        this.input = input;
    }


    /*public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }*/

    @Override
    public void stop() {
        if (input!=null) {
            try {
                input.close();
                input = null;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected String readAll() throws IOException {
        return input.lines().collect(Collectors.joining("\n"));
    }

    public void load() {

        if (input!=null) {
            try {
                String line = readAll();

                perception.perceive(process(line), this);

                //if (!isLooping()) {
                    input.close();
                    input = null;
                /*}
                else {
                    input.reset(); //rewind
                }*/

            } catch (IOException e) {
                if (input != null) {
                    try {
                        input.close();
                        input = null;
                    } catch (IOException ex1) {
                        ex1.printStackTrace();
                    }
                }
                if (Global.DEBUG) {
                    e.printStackTrace();
                }
                accept(new Echo(Events.IN.class, e.toString()));
            }
        }

    }



    /** can be overridden in subclasses to preprocess addInput */
    public String process(String input) {
        return input;
    }


}
