package nars.op.software.scheme;


import nars.op.software.scheme.exception.VariableNotDefinedException;
import nars.op.software.scheme.expressions.Expression;
import nars.op.software.scheme.expressions.SymbolExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SchemeClosure {
    public final Map<SymbolExpression, Expression> bindings;
    public final SchemeClosure enclosingEnvironment;

    private SchemeClosure(Map<SymbolExpression, Expression> bindings, SchemeClosure enclosingEnvironment) {
        this.bindings = bindings;
        this.enclosingEnvironment = enclosingEnvironment;
    }

    public SchemeClosure(Map<SymbolExpression, Expression> bindings) {
        this(bindings, null);
    }

    public SchemeClosure() {
        this(new HashMap<>(DefaultEnvironment.PRIMITIVES));
    }

    public SchemeClosure extend(Map<SymbolExpression, Expression> bindings) {
        return new SchemeClosure(bindings, this);
    }


    public Expression get(SymbolExpression symbol) {
        SchemeClosure other = this;
        while (true) {
            Expression exists = other.bindings.get(symbol);
            if (exists!=null) {
                return exists;
            }
            if (other.enclosingEnvironment != null) {
                other = other.enclosingEnvironment;
                continue;
            }
            throw new VariableNotDefinedException(symbol.toString());
        }
    }

    public void set(SymbolExpression symbol, Expression value) {
        if (bindings.containsKey(symbol)) {
            bindings.put(symbol, value);
        } else if (enclosingEnvironment != null) {
            enclosingEnvironment.set(symbol, value);
        } else {
            throw new VariableNotDefinedException(symbol.toString());
        }
    }

    public void define(SymbolExpression symbol, Expression value) {
        bindings.put(symbol, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchemeClosure that = (SchemeClosure) o;

        if (bindings != null ? !bindings.equals(that.bindings) : that.bindings != null) {
            return false;
        }
        return !(enclosingEnvironment != null ? !enclosingEnvironment.equals(that.enclosingEnvironment) : that.enclosingEnvironment != null);

    }

    @Override
    public int hashCode() {
        int result = bindings != null ? bindings.hashCode() : 0;
        result = 31 * result + (enclosingEnvironment != null ? enclosingEnvironment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + Integer.toUnsignedString( hashCode(), Character.MAX_RADIX);
    }

    public Stream<Expression> evalStream(String input) {
        List<Expression> read = Reader.read(input);
        return eval(read);

    }

    public Stream<Expression> eval(List<Expression> exprs) {
        return exprs.stream().map(e -> Evaluator.evaluate(e, this));
    }

    public List<Expression> eval(String input) {
        return evalStream(input).collect(Collectors.toList());
    }


}
