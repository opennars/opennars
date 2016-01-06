package automenta.vivisect.swing.property.sheet.editor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Number editor.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class CharacterEditor extends SpinnerEditor {

	public CharacterEditor() {
	}

	public CharacterEditor(Object property) {
		buildModel(Character.MIN_VALUE, (char) (Character.MAX_VALUE - 1));
		formatSpinner();
	}

	protected void buildModel(char min, char max) {

		List<Character> characters = new ArrayList<>();

		for (char c = min; c <= max; c++) {
			characters.add(c);
		}

		spinner.setModel(new SpinnerListModel(characters));

	}
}
