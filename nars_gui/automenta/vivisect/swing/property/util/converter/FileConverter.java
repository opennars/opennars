package automenta.vivisect.swing.property.util.converter;

import java.io.File;

public class FileConverter implements Converter {

	public void register(ConverterRegistry registry) {
		registry.addConverter(String.class, File.class, this);
		registry.addConverter(File.class, String.class, this);
	}

	public Object convert(Class type, Object value) {
		if (value == null)
			return null;
		if (String.class.equals(type) && File.class.equals(value.getClass())) {
			return ((File) value).getAbsolutePath();
		} else if (File.class.equals(type)
				&& String.class.equals(value.getClass())) {
			return new File(value.toString());
		} else {
			throw new IllegalArgumentException("Can't convert " + value
					+ " to " + type.getName());
		}
	}
}
