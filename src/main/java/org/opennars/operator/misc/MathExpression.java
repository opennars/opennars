/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.operator.misc;

import java.util.List;
import org.opennars.storage.Memory;
import org.opennars.io.Texts;
import static org.opennars.io.Texts.unescape;
import org.opennars.language.Inheritance;
import org.opennars.language.Product;
import org.opennars.language.Term;
import org.opennars.operator.FunctionOperator;
import org.encog.ml.prg.EncogProgram;
import org.encog.ml.prg.EncogProgramContext;
import org.encog.ml.prg.ProgramNode;
import org.encog.ml.prg.expvalue.ExpressionValue;
import org.encog.ml.tree.TreeNode;

/**
 * Parses an expression string to terms
 * @see https://github.com/encog/encog-java-core/blob/master/src/test/java/org/encog/ml/prg/TestProgramClone.java
 */
public class MathExpression  extends FunctionOperator {

    static EncogProgramContext context;

    
    public MathExpression() {
        super("^math");
    }

    final static String requireMessage = "Requires 1 string argument";
    
    final static Term exp = Term.get("math");
    
    
    @Override
    protected Term function(Memory memory, Term[] x) {

        //TODO this may not be thread-safe, this block may need synchronized:
        if (context == null) {
            context = new EncogProgramContext();            
            context.loadAllFunctions();
        }
        
        if (x.length!=1) {
            throw new RuntimeException(requireMessage);
        }

        Term content = x[0];
        if (content.getClass()!=Term.class) {
            throw new RuntimeException(requireMessage);
        }
        
        String expstring = unescape(content.name()).toString();
        if (expstring.startsWith("\""))
            expstring = expstring.substring(1, expstring.length()-1);
        
        EncogProgram p = context.createProgram(expstring);

        return getTerm(p.getRootNode());
    }

    @Override
    protected Term getRange() {
        return exp;
    }

    public static Term getTerm(TreeNode node) {
        
        CharSequence name = 
                    node instanceof ProgramNode ? 
                    ("\"" + Texts.escape(((ProgramNode)node).getName()) + '\"'):
                    node.getClass().getSimpleName();
        
        
        List<TreeNode> children = node.getChildNodes();
        
       
        ExpressionValue[] data = null;
        
        ProgramNode p = (ProgramNode)node;
        data = p.getData();
        if ((children == null) || (children.isEmpty())) {
            if ((data == null) || (data.length == 0) || (p.isVariable())) {
                if (p.isVariable()) {
                    long idx = data[0].toIntValue();
                    String varname = p.getOwner().getVariables().getVariableName((int)idx);
                    return Term.get(varname);
                }
                return Term.get(name);
            }
            else
                return getTerms(data);
        }
                
        if ((data!=null) && (data.length > 0))
            return Inheritance.make(new Product(getTerms(children), getTerms(data)), Term.get(name));
        else
            return Inheritance.make(getTerms(children), Term.get(name));
    }
    
    public static Term getTerms(List<TreeNode> children) {
        
        if (children.size() == 1)
            return getTerm(children.get(0));
        
        Term[] c = new Term[children.size()];
        int j = 0;
        for (TreeNode t : children) {
            c[j++] = getTerm(t);
        }
        
        return new Product(c);
    }
    
    public static Term getTerms(ExpressionValue[] data) {
        
        if (data.length == 1)
            return getTerm(data[0]);
        
        Term[] c = new Term[data.length];
        int j = 0;
        for (ExpressionValue t : data) {
            c[j++] = getTerm(t);
        }
        
        return new Product(c);        
    }

    public static Term getTerm(ExpressionValue t) {        
        return Inheritance.make(
                Term.get(Texts.escape(t.toStringValue())),
                Term.get(t.getExpressionType().toString()));
    }

}
