/*
 * Encog(tm) Core v3.3 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2014 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package objenome.solver.evolve.rewrite;

import objenome.solver.evolve.STGPIndividual;
import objenome.op.Node;

/**
 * Rewrite any parts of the tree that are constant with a simple constant value.
 * TODO Max2, Min2
 */
public class RewriteConstants implements RewriteRule {

    /**
     * True if the expression was rewritten.
     */
    private boolean rewritten;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rewrite(STGPIndividual program) {
        this.rewritten = false;
        Node rootNode = program.getRoot();
        Node rewrite = rewriteNode(rootNode);
        if (rewrite != null) {
            program.setRoot(rewrite);
        }
        return this.rewritten;
    }

    /**
     * Attempt to rewrite the specified node.
     *
     * @param node The node to attempt to rewrite.
     * @return The rewritten node, the original node, if no rewrite occured.
     */
    private Node rewriteNode(Node node) {

        // first try to rewrite the child node
        Node rewrite = tryNodeRewrite(node);
        if (rewrite != null) {
            return rewrite;
        }

        // if we could not rewrite the entire node, rewrite as many children as
        // we can
        for (int i = 0; i < node.getChildren().length; i++) {
            Node childNode = node.getChildren()[i];
            rewrite = rewriteNode(childNode);
            if (rewrite != null) {
                node.setChild(i, rewrite);
                this.rewritten = true;
            }
        }

        // we may have rewritten some children, but the parent was not
        // rewritten, so return null.
        return null;
    }

    /**
     * Try to rewrite the specified node.
     *
     * @param parentNode The node to attempt rewrite.
     * @return The rewritten node, or original node, if no rewrite could happen.
     */
    private Node tryNodeRewrite(Node parentNode) {
        return null;

//        Node result = null;
//
//        if (parentNode.isTerminal()) {
//            return null;
//        }
//
//        
//        if (parentNode.allConstDescendants()) {
//            Object v = parentNode.evaluate();
//            double ck = NumericUtils.asFloat( v );
//
//            // do not rewrite if it produces a div by 0 or other bad result.
//            if (Double.isNaN(ck) || Double.isInfinite(ck)) {
//                return result;
//            }
//
//            
//            result = parentNode
//                    .getOwner()
//                    .getContext()
//                    .getFunctions()
//                    .factorNode("#const", parentNode.getOwner(),
//                            new Node[]{});
//
//            // is it an integer?
//            if (MathUtils.equalEnough(ck, ((int) ck))) { //Math.abs(ck - ) < Encog.DEFAULT_DOUBLE_EQUAL) {
//                return new Literal((int) ck);
//            } else {
//                return new Literal(ck);
//            }
//        }
//        return result;
    }
}
