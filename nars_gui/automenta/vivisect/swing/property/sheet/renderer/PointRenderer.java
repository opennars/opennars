package automenta.vivisect.swing.property.sheet.renderer;

import java.awt.Point;
import java.text.NumberFormat;

import automenta.vivisect.swing.property.swing.renderer.DefaultCellRenderer;


public class PointRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -777052685333950693L;

	@Override
	protected String convertToString(Object value) {
		Point p = (Point) value;
		NumberFormat format = NumberFormat.getInstance();
		String x = format.format(p.x);
		String y = format.format(p.y);
		return String.format("[%s; %s]", x, y);
	}

}
