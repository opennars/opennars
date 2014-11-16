package automenta.vivisect.swing.property.util.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter implements Converter {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public void register(ConverterRegistry registry) {
		registry.addConverter(String.class, Date.class, this);
		registry.addConverter(Date.class, String.class, this);
	}

	public Object convert(Class type, Object value) {
		if (value == null)
			return null;
		if (String.class.equals(type) && Date.class.equals(value.getClass())) {
			return sdf.format((Date)value);
		} else if (Date.class.equals(type)
				&& String.class.equals(value.getClass())) { 
			try {
				return sdf.parse(value.toString());	
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Can't convert " + value
						+ " to " + type.getName());
			}
			
		} else {
			throw new IllegalArgumentException("Can't convert " + value
					+ " to " + type.getName());
		}
	}
	

}
