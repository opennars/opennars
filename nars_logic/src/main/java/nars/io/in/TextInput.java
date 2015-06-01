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
package nars.io.in;

import nars.io.TextPerception;

/**
 * Process experience from a string into zero or more input tasks
 */
public class TextInput extends Input.BufferedInput {


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
