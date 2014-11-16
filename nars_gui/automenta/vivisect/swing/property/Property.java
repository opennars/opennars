package automenta.vivisect.swing.property;

import java.beans.PropertyEditor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
	/**
	 * @return The name of the field (to be presented by the GUI). If not set, the name of the
	 * variable will be used.
	 */
	String name() default "";
	
	/**
	 * The description will provide further information on this field (seen in the configuration panel)
	 * @return The description of the field. If not set, the <b>name</b> parameter will be used.
	 */
    String description() default "";
    
    /**
     * You can use categories to group various properties that are somehow related
     * @return The category for this configuration field
     */
    String category() default "";
    
    boolean editable() default true;
    
    /**
     * The (full) class name of the editor to be used for this property
     */
    Class<? extends PropertyEditor> editorClass() default PropertyEditor.class;
}
