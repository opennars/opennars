/*Castagna 06/2011*/
package nars.tuprolog;

public interface TermVisitor {
	void visit(Struct s);
	void visit(Var v);
	void visit(Number n);
}
/**/