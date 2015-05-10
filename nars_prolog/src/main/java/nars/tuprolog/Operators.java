/*
 * tuProlog - Copyright (C) 2001-2006  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import com.google.common.collect.HashBasedTable;
import nars.tuprolog.interfaces.IOperators;

import java.io.Serializable;
import java.util.*;

/**
 * This class manages Prolog operators.
 *
 * @see Operator
 */
@SuppressWarnings("serial")
/*Castagna 06/2911*/public/**/ class Operators implements /*Castagna 06/2011*/IOperators,/**/Serializable {
    
    /**
	 * current known operators
	 */
    private final OperatorRegister operatorList = new OperatorRegister();
    
    /** lowest operate priority */
    public static final int OP_LOW = 1;
    
    /** highest operate priority */
    public static final int OP_HIGH = 1200;
    
    /**
     * Creates a new operate. If the operate is already provided,
     * it replaces it with the new one
     */
    public synchronized void opNew(String name,String type,int prio) {
        final Operator op = new Operator(name, type, prio);
        if (prio >= OP_LOW && prio <= OP_HIGH)
            operatorList.addOperator(op);
    }
    
    /**
     * Returns the priority of an operate (0 if the operate is not defined).
     */
    public synchronized int opPrio(CharSequence name,CharSequence type) {
        Operator o = operatorList.getOperator(name, type);
        return (o == null) ? 0 : o.prio;
    }
    
    /**
     * Returns the priority nearest (lower) to the priority of a defined operate
     */
    public synchronized int opNext(int prio) {
        int n = 0;
        for (Operator opFromList:operatorList){
            if (opFromList.prio > n && opFromList.prio < prio)
                n = opFromList.prio;
        }
        return n;
    }
    
    /**
     *  Gets the list of the operators currently defined
     *
     *  @return the list of the operators
     */
    public synchronized Collection<Operator> getOperators() {
        //return new LinkedList<>(operatorList);
        return operatorList;
    }
    
/*Castagna 06/2011*/     
    /* Francesco Fabbri		 
     * 16/05/2011		 
     * Clone operation added		 
     */		 
//    public IOperatorManager clone() {
//    	Operators om = new Operators();
//    	om.operatorList = (OperatorRegister)this.operatorList.clone();
//    	return om;
//    }
/**/    
    /**
     * Register for operators
     * Cashes operate by name+type description.
     * Retains insertion order as LinkedHashSet.
     * <p/>
     * todo Not 100% sure if 'insertion-order-priority' should be completely replaced
     * by the explicit priority given to operators.
     *
     * @author ivar.orstavik@hist.no
     */
    private static class OperatorRegister extends LinkedHashSet<Operator> /*Castagna 06/2011*/implements Cloneable/**/
    {
        //map of operators by name and type
        //key is the nameType of an operate (for example ":-xfx") - value is an Operator
        //private HashMap<CharSequence,Operator> nameTypeToKey = new HashMap<>();
        private HashBasedTable<CharSequence,CharSequence,Operator> nameTypeToKey = HashBasedTable.create();
        
        public final boolean addOperator(final Operator op) {            
            Operator existing = nameTypeToKey.put(op.name, op.type, op);
            if (existing != null)
                super.remove(existing);       //removes found match from the main list
            return super.add(op);               //adds new operate to the main list
        }
        
        public final Operator getOperator(final CharSequence name, final CharSequence type) {         
            return nameTypeToKey.get(name, type);
        }

        /*Castagna 06/2011*/        
        @Override		 
        public Object clone() {		 
        	OperatorRegister or = (OperatorRegister)super.clone();		 
        	Iterator<Operator> ior = or.iterator();
        	or.nameTypeToKey = HashBasedTable.create(nameTypeToKey);
        	while(ior.hasNext()) {		 
        		Operator o = ior.next();
        		or.nameTypeToKey.put(o.name, o.type, o);		 
        	}		 
        	return or;
        }
        /**/
    }
    
}