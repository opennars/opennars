package nars.prolog;

import java.util.LinkedList;

/**
 * <code>FamilyClausesIndex</code> enables family clauses indexing
 * in {@link ClauseDatabase}.
 *
 * @author Paolo Contessi
 * @since 2.2
 */
class FamilyClausesIndex<K extends Comparable<? super K>>
        extends RBTree<K, LinkedList<ClauseInfo>> {

    private LinkedList<ClauseInfo> varsClauses;

    public FamilyClausesIndex(){
        super();
        varsClauses = new LinkedList<>();
    }

    private Node<K,LinkedList<ClauseInfo>> createNewNode(K key, ClauseInfo clause, boolean first){
        LinkedList<ClauseInfo> list = new LinkedList<>(varsClauses);

        if(first){
            list.addFirst(clause);
        } else {
            list.addLast(clause);
        }
        
        return new Node<>(key, list, Color.RED, null, null);
    }

    /**
     * @deprecated 
     */
    @Override
    public void insert(K key, LinkedList<ClauseInfo> value){
        super.insert(key, value);
    }

    /*
     * Voglio memorizzare un riferimento alla clausola, rispettando l'ordine
     * delle clausole
     *
     * Se l'indice non ha nodi?
     * Se aggiungo un nuovo nodo
     */
    public void insertAsShared(ClauseInfo clause, boolean first){
        if(first){
            varsClauses.addFirst(clause);
        } else {
            varsClauses.addLast(clause);
        }

        //Aggiorna tutti i nodi che ci sono
        if(root != null){
            LinkedList<Node<K, LinkedList<ClauseInfo>>> buf = new LinkedList<>();
            buf.add(root);

            while(buf.size() > 0){
                Node<K, LinkedList<ClauseInfo>> n = buf.remove();
                
                if(first){
                    n.value.addFirst(clause);
                } else {
                    n.value.addLast(clause);
                }

                if(n.left != null){
                    buf.addLast(n.left);
                }

                if(n.right != null){
                    buf.addLast(n.right);
                }
            }
        }
    }

    /**
     * Creates a new entry (<code>key</code>) in the index, relative to the
     * given <code>clause</code>. If other clauses is associated to <code>key</code>
     * <code>first</code> parameter is used to decide if it is the first or
     * the last clause to be retrieved.
     *
     * @param key       The key of the index
     * @param clause    The value to be binded to the given key
     * @param first     If the clause must be binded as first or last element
     */
    public void insert(K key, ClauseInfo clause, boolean first){
        Node<K, LinkedList<ClauseInfo>> insertedNode = null;
        if (root == null) {
            insertedNode = root = createNewNode(key, clause, first);
        } else {
            Node<K,LinkedList<ClauseInfo>> n = root;
            while (true) {
                int compResult = key.compareTo(n.key);
                if (compResult == 0) {
                    if(first){
                        n.value.addFirst(clause);
                    } else {
                        n.value.addLast(clause);
                    }
                    return;
                } else if (compResult < 0) {
                    if (n.left == null) {
                        insertedNode = n.left = createNewNode(key, clause,first);
                        break;
                    } else {
                        n = n.left;
                    }
                } else {
                    assert compResult > 0;
                    if (n.right == null) {
                        insertedNode = n.right = createNewNode(key, clause,first);
                        break;
                    } else {
                        n = n.right;
                    }
                }
            }
            
            insertedNode.parent = n;
        }
        insertCase1(insertedNode);
        verifyProperties();
    }

    /**
     * Removes all clauses related to the given key
     *
     * @param key   The key
     */
    public void remove(K key,ClauseInfo clause ){
        super.delete(key,clause);
    }

    public void removeShared(ClauseInfo clause){
        if(varsClauses.remove(clause)){
            if(root != null){
                if(root != null){
            LinkedList<Node<K, LinkedList<ClauseInfo>>> buf = new LinkedList<>();
            buf.add(root);

            while(buf.size() > 0){
                Node<K, LinkedList<ClauseInfo>> n = buf.remove();
                
                n.value.remove(clause);

                if(n.left != null){
                    buf.addLast(n.left);
                }

                if(n.right != null){
                    buf.addLast(n.right);
                }
            }
        }
            }
        } else {
            throw new IllegalArgumentException("Invalid clause: not registered in this index");
        }
    }

    /**
     * Retrieves all the clauses related to the key
     *
     * @param key   The key
     * @return      The related clauses
     */
    public LinkedList<ClauseInfo> get(K key){
        LinkedList<ClauseInfo> res = null;
        if(root != null){
            res = super.lookup(key);
        } 

        if(res == null){
            return varsClauses;
        }

        return res;
    }
}
