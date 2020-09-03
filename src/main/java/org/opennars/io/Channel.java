/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennars.io;

import java.util.HashMap;
import org.opennars.interfaces.Timable;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.storage.Buffer;
import org.opennars.main.Nar;

/**
 *
 * @author Daria
 */
public class Channel extends Buffer {
    
    public Channel(Nar nar, int levels, int capacity, Parameters narParameters) {
        super(nar, levels, capacity, narParameters);
    }
    
    public HashMap<String,Operation> operations;   
}
