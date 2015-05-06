package objenome.op.cas;

public abstract class Function extends Operation {
    
    public Integer printLevelLeftPass() {
        return classOrderNum - 1;
    }
    
    // makes the ouput ambiguous, according to my mom
//     public Integer printLevelRightPass() {
//         if (ofExpr().functionalParens()) return classOrderNum - 1;
//         return super.printLevelRightPass();
//     }
    
    public boolean firstParenPrint() {
        return false;
    }
    
    public Expr ofExpr() {
        return getExpr(0);
    }
    
}
