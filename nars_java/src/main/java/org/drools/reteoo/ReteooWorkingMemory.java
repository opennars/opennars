package org.drools.reteoo;

/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.FactException;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.common.AbstractWorkingMemory;
import org.drools.common.DefaultAgenda;
import org.drools.common.EqualityKey;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalRuleBase;
import org.drools.common.PropagationContextImpl;
import org.drools.rule.Rule;
import org.drools.spi.Activation;
import org.drools.spi.PropagationContext;

/**
 * Implementation of <code>WorkingMemory</code>.
 * 
 * @author <a href="mailto:bob@werken.com">bob mcwhirter </a>
 * @author <a href="mailto:mark.proctor@jboss.com">Mark Proctor</a>
 * @author <a href="mailto:simon@redhillconsulting.com.au">Simon Harris</a>
 */
public class ReteooWorkingMemory extends AbstractWorkingMemory {

    /**
     * 
     */
    private static final long serialVersionUID = -5107074490638575715L;

    /**
     * Construct.
     * 
     * @param ruleBase
     *            The backing rule-base.
     */
    public ReteooWorkingMemory(final InternalRuleBase ruleBase) {
        super( ruleBase,
               ruleBase.newFactHandleFactory() );
        this.agenda = new DefaultAgenda( this );
    }

    public void doAssertObject(final InternalFactHandle handle,
                               final Object object,
                               final PropagationContext propagationContext) throws FactException {
        this.ruleBase.assertObject( handle,
                                    object,
                                    propagationContext,
                                    this );
    }

    public void doRetract(final InternalFactHandle handle,
                          final PropagationContext propagationContext) {
        this.ruleBase.retractObject( handle,
                                     propagationContext,
                                     this );
    }

    /**
     * @see WorkingMemory
     */
    public void modifyObject(final FactHandle factHandle,
                             final Object object,
                             final Rule rule,
                             final Activation activation) throws FactException {
        this.lock.lock();
        try {
            final int status = ((InternalFactHandle) factHandle).getEqualityKey().getStatus();
            final InternalFactHandle handle = (InternalFactHandle) factHandle;
            final Object originalObject = handle.getObject();

            if ( handle.getId() == -1 || object == null ) {
                // the handle is invalid, most likely already  retracted, so return
                // and we cannot assert a null object
                return;
            }
            
            // set anyway, so that it updates the hashCodes
            handle.setObject( object );

            // We only need to put objects, if its a new object
            if ( originalObject != object ) {
                this.assertMap.put( handle,
                                    handle );
            }

            // the hashCode and equality has changed, so we must update the EqualityKey
            EqualityKey key = handle.getEqualityKey();
            key.removeFactHandle( handle );

            // If the equality key is now empty, then remove it
            if ( key.isEmpty() ) {
                this.tms.remove( key );
            }

            // now use an  existing  EqualityKey, if it exists, else create a new one
            key = this.tms.get( object );
            if ( key == null ) {
                key = new EqualityKey( handle,
                                       status );
                this.tms.put( key );
            } else {
                key.addFactHandle( handle );
            }
            
            handle.setEqualityKey( key );

            this.handleFactory.increaseFactHandleRecency( handle );

            final PropagationContext propagationContext = new PropagationContextImpl( this.propagationIdCounter++,
                                                                                      PropagationContext.MODIFICATION,
                                                                                      rule,
                                                                                      activation );

            this.ruleBase.modifyObject( factHandle,
                                        propagationContext,
                                        this );

            if (this.workingEvents!=null)
                this.workingEvents.fireObjectModified( propagationContext,
                                                               factHandle,
                                                               originalObject,
                                                               object );

            if ( !this.factQueue.isEmpty() ) {
                propagateQueuedActions();
            }
        } finally {
            this.lock.unlock();
        }
    }

//    public QueryResults getQueryResults(final String query) {
//        final FactHandle handle = assertObject( new DroolsQuery( query ) );
//        final QueryTerminalNode node = (QueryTerminalNode) this.queryResults.remove( query );
//        Query queryObj = null;
//        List list = null;
//
//        if ( node == null ) {
//            org.drools.rule.Package[] pkgs = this.ruleBase.getPackages();
//            for( int i = 0; i < pkgs.length; i++ ) {
//                Rule rule = pkgs[i].getRule( query );
//                if( ( rule != null ) && ( rule instanceof Query ) ) {
//                    queryObj = (Query) rule;
//                    break;
//                }
//            }
//            retractObject( handle );
//            if( queryObj == null ) {
//                throw new IllegalArgumentException("Query '"+query+"' does not exist");
//            }
//            list = Collections.EMPTY_LIST;
//        } else {
//            list = (List) this.nodeMemories.remove( node.getId() );
//
//            retractObject( handle );
//            if ( list == null ) {
//                list = Collections.EMPTY_LIST;
//            }
//            queryObj = (Query) node.getRule();
//        }
//
//        return new QueryResults( list,
//                                 queryObj,
//                                 this );
//    }
//
//    void setQueryResults(final String query,
//                         final QueryTerminalNode node) {
//        if ( this.queryResults == Collections.EMPTY_MAP ) {
//            this.queryResults = new HashMap();
//        }
//        this.queryResults.put( query,
//                               node );
//    }

}
