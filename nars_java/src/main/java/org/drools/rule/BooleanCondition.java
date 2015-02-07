package org.drools.rule;

import org.drools.WorkingMemory;
import org.drools.common.InternalFactHandle;
import org.drools.spi.FieldConstraint;
import org.drools.spi.Tuple;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Predicate;


abstract public class BooleanCondition<X> implements FieldConstraint<X>, Predicate<X> {

    private static final Declaration[] requiredDeclarations = new Declaration[0];
    private final Class type;

    public BooleanCondition() {

        this.type = (Class) ((ParameterizedType) ((this.getClass()).getGenericSuperclass())).getActualTypeArguments()[0];

    }

    @Override
    public boolean isAllowed(InternalFactHandle<X> handle, Tuple tuple, WorkingMemory workingMemory) {

        Object o = handle.getObject();
        if (!type.isAssignableFrom(o.getClass())) return false;

        return test(handle.getObject());
    }

    @Override
    public Declaration[] getRequiredDeclarations() {
        return requiredDeclarations;
    }
}
