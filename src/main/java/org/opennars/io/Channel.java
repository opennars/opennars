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

/**
 *
 * @author Daria
 */
public class Channel extends Buffer {
    
    public Channel(Timable timable, int levels, int capacity, Parameters narParameters) {
        super(timable, levels, capacity, narParameters);
    }
    
    //TODO add the 4 subclasses
    //KnowledgeChannel, SensorimotorChannel, NarseseChannel

    public HashMap<String,Operation> operations;   
}
