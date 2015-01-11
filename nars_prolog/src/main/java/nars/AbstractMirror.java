/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars;

import nars.core.NAR;
import nars.inference.AbstractObserver;

/**
 * Interface with which to implement a "mirror" - a mental prosthetic which
 * reflects NAR activity into an enhanced or accelerated representation.
 * Usually these violate NARS theory and principles as the expense of
 * improved performance.  However these can be uesd for comparing results.
 * 
 */
abstract public class AbstractMirror extends AbstractObserver {
    
    public AbstractMirror(NAR n, boolean active, Class... events) {
        super(n, active, events);
    }
}
