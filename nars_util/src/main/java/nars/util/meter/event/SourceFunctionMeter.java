/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter.event;

import nars.util.meter.FunctionMeter;

/**
 * Function meter with one specific ID
 */
abstract class SourceFunctionMeter<T> extends FunctionMeter<T> {
    
    private final String name;

    public SourceFunctionMeter(String id) {
        super(id);
        name = id;
    }

    public String id() { return name; }
    

    
}
