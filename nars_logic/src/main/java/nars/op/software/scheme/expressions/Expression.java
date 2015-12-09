package nars.op.software.scheme.expressions;


import nars.op.software.scheme.exception.UnexpectedExpression;

public interface Expression  {

    Expression NONE = new Expression() {
        public String toString() {
            return "none()";
        }

        @Override
        public String print() {
            return "";
        }
    };

    String print();

    static Expression none() {
        return NONE;
    }

    default <T extends Expression> T assertExpressionOfType(Class<T> type) {
        if (!instanceOf(type)) {
            throw new UnexpectedExpression(String.format("Expected expression of type '%s', got '%s'", type.getSimpleName(), this));
        }

        return type.cast(this);
    }

    default <T extends Expression> boolean instanceOf(Class<T> type) {
        return type.isAssignableFrom(getClass());
    }

    default ListExpression list() {
        return assertExpressionOfType(ListExpression.class);
    }

    default NumberExpression number() {
        return assertExpressionOfType(NumberExpression.class);
    }

    default ProcedureExpression procedure() {
        return assertExpressionOfType(ProcedureExpression.class);
    }

    default SymbolExpression symbol() {
        return assertExpressionOfType(SymbolExpression.class);
    }

    default BooleanExpression bool() {
        return assertExpressionOfType(BooleanExpression.class);
    }

    default StringExpression string() { return assertExpressionOfType(StringExpression.class); }

    default boolean isList() {
        return instanceOf(ListExpression.class);
    }

    default boolean isSymbol() {
        return instanceOf(SymbolExpression.class);
    }

    default boolean isNumber() {
        return instanceOf(NumberExpression.class);
    }

    default boolean isProcedure() {
        return instanceOf(ProcedureExpression.class);
    }

    default boolean isBoolean() {
        return instanceOf(BooleanExpression.class);
    }

    default boolean isString() { return instanceOf(StringExpression.class); }
}
