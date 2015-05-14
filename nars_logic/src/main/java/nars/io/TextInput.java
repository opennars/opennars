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
import com.google.common.io.Files;
import nars.Events;
import nars.Global;
import nars.op.io.Echo;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * To read and write experience as Task streams
 */
public class TextInput extends Input.BufferedInput {



    public static class ReaderInput extends Input.BufferedInput {
        private final TextPerception perception;
        protected BufferedReader input;

        protected void setInput(BufferedReader input) {
            this.input = input;
        }

        public ReaderInput(TextPerception p) {
            this.perception = p;
        }

        public ReaderInput(TextPerception p, InputStream i) {
            this(p, new BufferedReader(new InputStreamReader(i)));
        }
        public ReaderInput(TextPerception p, URL u) throws IOException {
            this(p, u.openStream());
        }

        public ReaderInput(TextPerception p, BufferedReader input) {
            this(p);

            setInput(input);

        }

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

    }

    public static class FileInput extends TextInput {


        public FileInput(TextPerception p, File input) throws IOException {
            super(p, load(input));
        }

        public static String load(String path) throws IOException {
            return load(new File(path));
        }

        private static String load(File file) throws IOException {
            return Files.toString(file, Charset.defaultCharset());
        }



    }

    private final TextPerception perception;
    /**
     * Input experience from a file
     */
    protected final String input;
    
    //private boolean isLooping = false;
    
    public TextInput(TextPerception p, String input) {
        this.perception = p;
        this.input = input;
        perception.perceive(process(input), this);
    }



    /** can be overridden in subclasses to preprocess addInput */
    public String process(String input) {
        return input;
    }


}
