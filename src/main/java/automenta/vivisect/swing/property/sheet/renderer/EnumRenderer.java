package automenta.vivisect.swing.property.sheet.renderer;

import automenta.vivisect.swing.property.sheet.I18N;
import automenta.vivisect.swing.property.swing.renderer.DefaultCellRenderer;


/**
 * Enumeration value renderer.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class EnumRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = 6826062487749986507L;

	@Override
	protected String convertToString(Object value) {
		if (value == null) {
			return I18N.NOT_SET;
		} else {
			return value.toString();
		}
	}
}
