package nars.operate.software.scheme.expressions;


import nars.operate.software.scheme.cons.Cons;

import java.util.function.Function;

public class ProcedureExpression implements Expression {

    public final Function<Cons<Expression>, Expression> lambda;

    public ProcedureExpression(Function<Cons<Expression>, Expression> lambda) {
        this.lambda = lambda;
    }

    public static ProcedureExpression procedure(Function<Cons<Expression>, Expression> lambda) {
        return new ProcedureExpression(lambda);
    }

    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass() && lambda.equals(((ProcedureExpression) o).lambda);
    }

    @Override
    public int hashCode() {
        return lambda.hashCode();
    }

    @Override
    public String print() {
        return "#<Procedure>";
    }
}
