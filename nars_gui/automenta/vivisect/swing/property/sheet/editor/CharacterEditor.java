package automenta.vivisect.swing.property.sheet.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SpinnerListModel;


/**
 * Number editor.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class CharacterEditor extends SpinnerEditor {

	public CharacterEditor() {
		super();
	}

	public CharacterEditor(Object property) {
		super();
		buildModel(Character.MIN_VALUE, (char) (Character.MAX_VALUE - 1));
		formatSpinner();
	}

	protected void buildModel(char min, char max) {

		List<Character> characters = new ArrayList<Character>();

		for (char c = min; c <= max; c++) {
			characters.add(c);
		}

		spinner.setModel(new SpinnerListModel(characters));

	}
}
