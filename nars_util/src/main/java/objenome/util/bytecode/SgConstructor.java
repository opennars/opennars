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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A constructor.
 */
public final class SgConstructor extends SgBehavior {

    private final List<String> body;

    /**
     * Constructor with class. Modifier defaults to "public". The constructor
     * will automatically be added to the <code>owner</code>.
     * 
     * @param owner
     *            Class the behavior belongs to - Cannot be null.
     */
    public SgConstructor(SgClass owner) {
        this(owner, "public");
    }

    /**
     * Constructor with class and modifiers. The constructor will automatically
     * be added to the <code>owner</code>.
     * 
     * @param owner
     *            Class the behavior belongs to - Cannot be null.
     * @param modifiers
     *            Modifiers for the constructor/method - Cannot be null (but
     *            empty).
     */
    public SgConstructor(SgClass owner, String modifiers) {
        super(owner, modifiers);
        body = new ArrayList<>();
        // TODO Check if the class not already contains a constructor with the
        // same name and arguments!
        owner.addConstructor(this);
    }

    /**
     * Returns the body of the constructor.
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
            throw new IllegalArgumentException("The argument 'line' cannot be null!");
        }
        body.add(line);
    }

    /**
     * Returns the "signature" of the constructor.
     * 
     * @return Modifiers and arguments.
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        if (!getModifiers().isEmpty()) {
            sb.append(getModifiers());
            sb.append(' ');
        }
        sb.append(getOwner().getSimpleName());
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getAnnotations().isEmpty()) {
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
