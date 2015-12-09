/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package objenome.util.bytecode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A method.
 */
public final class SgMethod extends SgBehavior {

    private final SgClass returnType;

    private final String name;

    private final List<String> body;
    private Method existingMethod;

    /**
     * Constructor. The method will automatically be added to the
     * <code>owner</code>.
     * 
     * @param owner
     *            Class the behavior belongs to - Cannot be null.
     * @param modifiers
     *            Modifiers for the constructor/method - Cannot be null (but
     *            empty).
     * @param returnType
     *            Return type of the method - Cannot be null (Use VOID in model
     *            class for no return value).
     * @param name
     *            Name of the method.
     */
    public SgMethod(SgClass owner, String modifiers, SgClass returnType,
                    String name) {
        super(owner, modifiers);
        if (returnType == null) {
            throw new IllegalArgumentException("The argument 'returnType' cannot be NULL!");
        }
        this.returnType = returnType;

        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be NULL!");
        }
        this.name = name;

        body = new ArrayList<>();

        // TODO Check if the class not already contains a method with the same
        // name and arguments!

        owner.addMethod(this);

    }

    public SgMethod(Method existingMethod, SgClass cl, String mModifiers, SgClass returnType, String name) {
        this(cl, mModifiers, returnType, name);
        this.existingMethod = existingMethod;
    }

    public Method getExistingMethod() {
        return existingMethod;
    }

    
    /**
     * Return the return type of the method.
     * 
     * @return Type - Always non-null.
     */
    public SgClass getReturnType() {
        return returnType;
    }

    /**
     * Returns the body of the method.
     * 
     * @return Body - Always non-null, maybe empty and is unmodifiable.
     */
    public List<String> getBody() {
        return Collections.unmodifiableList(body);
    }

    /**
     * Add a new line to the body.
     * 
     * @param line
     *            Line to add - Cannot be null (but empty).
     */
    public void addBodyLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("The argument 'line' cannot be NULL!");
        }
        body.add(line.trim());
    }

    /**
     * Returns the name of the method.
     * 
     * @return Name - Always non-null.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the method with an "underscore" inserted before all
     * upper case characters and all characters converted to lower case.
     * 
     * @return Name usable as a package - Always non-null.
     */
    public String getNameAsPackage() {
        return SgUtils.uppercaseToUnderscore(getName());
    }

    /**
     * Returns the "signature" of the method.
     * 
     * @return Modifiers, return type, name and arguments - Always non-null.
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        if (!getModifiers().isEmpty()) {
            sb.append(getModifiers());
            sb.append(' ');
        }
        sb.append(returnType.getName());
        sb.append(' ');
        sb.append(getName());
        sb.append('(');
        for (int i = 0; i < getArguments().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(getArguments().get(i));
        }
        sb.append(')');
        if (!getExceptions().isEmpty()) {
            sb.append(" throws ");
            for (int i = 0; i < getExceptions().size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(getExceptions().get(i).getName());
            }
        }
        return sb.toString();
    }

    /**
     * Returns the "call" signature of the method.
     * 
     * @return Method name and argument names (like "methodXY(a, b, c)").
     */
    public String getCallSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append('(');
        for (int i = 0; i < getArguments().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            SgArgument arg = getArguments().get(i);
            sb.append(arg.getName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Returns the "type" signature of the method.
     * 
     * @return Method name and argument types (like
     *         "methodXY(String, int, boolean)").
     */
    public String getTypeSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append('(');
        for (int i = 0; i < getArguments().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            SgArgument arg = getArguments().get(i);
            sb.append(arg.getType().getSimpleName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Returns the name of the method (first character upper case) and the
     * argument types divided by by an underscore.
     * 
     * @return Method name and argument types (like
     *         "MethodXY_String_int_boolean").
     */
    public String getUnderscoredNameAndTypes() {
        StringBuilder sb = new StringBuilder();
        sb.append(SgUtils.firstCharUpper(getName()));
        sb.append('_');
        for (int i = 0; i < getArguments().size(); i++) {
            if (i > 0) {
                sb.append('_');
            }
            SgArgument arg = getArguments().get(i);
            String typeName = arg.getType().getSimpleName();
            sb.append(SgUtils.replace(typeName, "[]", "ARRAY", -1));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Creates the method's source with or without annotations.
     * 
     * @param showAnnotations
     *            To include annotations <code>true</code> else
     *            <code>true</code>.
     * 
     * @return Source code of the method.
     */
    public String toString(boolean showAnnotations) {
        StringBuilder sb = new StringBuilder();
        if (showAnnotations && (!getAnnotations().isEmpty())) {
            for (int i = 0; i < getAnnotations().size(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(getAnnotations().get(i));
            }
            sb.append('\n');
        }
        sb.append(getSignature());
        if (getOwner().isInterface()) {
            sb.append(';');
        } else {
            sb.append("{\n");
            if (body.isEmpty()) {
                sb.append("// No method source available\n");
            } else {
                for (String aBody : body) {
                    sb.append(aBody);
                    sb.append('\n');
                }
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

}
