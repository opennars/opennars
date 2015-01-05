package automenta.vivisect.swing.property.sheet.editor;

import automenta.vivisect.swing.property.beans.editor.ComboBoxPropertyEditor;
import automenta.vivisect.swing.property.propertysheet.Property;
import automenta.vivisect.swing.property.sheet.I18N;


/**
 * Property editor for all kind of enumerations.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class EnumEditor extends ComboBoxPropertyEditor {

	/**
	 * Wrapper for enumeration values.
	 */
	private static final class EnumWrapper {

		private Enum<?> value = null;

		public EnumWrapper(Enum<?> value) {
			this.value = value;
		}

		public Enum<?> getValue() {
			return value;
		}

		@Override
		public String toString() {
			if (value == null) {
				return I18N.NOT_SET;
			} else {
				return value.toString();
			}
		}
	}

	private Class<? extends Enum<?>> enumeration = null;

	public EnumEditor(Object property) {
		super();

		if (!(property instanceof Property)) {
			throw new IllegalArgumentException(String.format("Property has to be a %s instance", Property.class));
		}

		Property prop = (Property) property;

		@SuppressWarnings("unchecked")
		Class<? extends Enum<?>> enumeration = (Class<? extends Enum<?>>) prop.getType();

		Object[] values = enumeration.getEnumConstants();
		if (values == null) {
			throw new IllegalArgumentException(String.format("Unsupported %s", enumeration));
		}

		this.enumeration = enumeration;

		Enum<?>[] constants = enumeration.getEnumConstants();
		EnumWrapper[] options = new EnumWrapper[constants.length + 1];

		options[0] = new EnumWrapper(null);
		for (int i = 0; i < constants.length; i++) {
			options[i + 1] = new EnumWrapper(constants[i]);
		}

		setAvailableValues(options);
	}

	public Class<? extends Enum<?>> getEnumerationClass() {
		return enumeration;
	}

	@Override
	public Object getValue() {
		EnumWrapper option = (EnumWrapper) super.getValue();
		if (option != null) {
			return option.getValue();
		}
		return null;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Enum<?> || value == null) {
			super.setValue(new EnumWrapper((Enum<?>) value));
		} else {
			throw new IllegalArgumentException(String.format("Value must be instance of %s, instead found %s", Enum.class, value.getClass()));
		}
	}
}
