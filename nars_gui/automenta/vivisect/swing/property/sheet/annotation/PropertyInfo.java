/**
 * Copyright (C) 2012 Bartosz Firyn (SarXos)
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package automenta.vivisect.swing.property.sheet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.table.TableCellRenderer;

import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.sheet.AnnotatedBeanInfo;


/**
 * This annotation used together with {@link AnnotatedBeanInfo} expose only
 * those properties which are annotated with.
 * 
 * @author Bartosz Firyn (SarXos)
 * @see AnnotatedBeanInfo
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface PropertyInfo {

	String name() default "#default";

	String category() default "";

	String description() default "";

	Class<? extends AbstractPropertyEditor> editor() default AbstractPropertyEditor.class;

	Class<? extends TableCellRenderer> renderer() default TableCellRenderer.class;

	boolean readonly() default false;

	boolean expert() default false;

	boolean important() default false;

	boolean constrained() default false;

	boolean hidden() default false;
}
