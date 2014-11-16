package automenta.vivisect.swing.property.sheet.converter;

import java.awt.Dimension;

import automenta.vivisect.swing.property.sheet.Converter;


public class DimensionConverter implements Converter<Dimension> {

	@Override
	public Dimension toObject(String string) {
		string = string.trim();
		String[] parts = string.split("([^0-9])+");
		if (parts.length == 2) {
			int width = Integer.parseInt(parts[0].trim());
			int height = Integer.parseInt(parts[1].trim());
			return new Dimension(width, height);
		} else {
			throw new NumberFormatException("Incorrect dimension format");
		}
	}

	@Override
	public String toString(Dimension dimension) {
		return String.format("%d \u00D7 %d", dimension.width, dimension.height);
	}

}
