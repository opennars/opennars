package nars.guifx.wikipedia;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import nars.guifx.JFX;

/**
 * TODO
 *  include tag text area at the bottom, where
 *  selected tags collect by appending. this allows them
 *  to be re-ordered, modified, and removed.
 *
 *  show/hide a palette of common tags like:
 *     I, Someone, Learn, Do, Teach, Can, Need, Not
 *     (etc) each with an icon
 *
 *  a button aligned at the right will prepare
 *  the set of tags for construction of term or multiple-task input
 *  to the nar in one of several templates:
 *      --raw ext set
 *      --raw int set
 *      --subj pred obj - if this can be deduced or manually disambiguated
 *          ex: I Need Weapon_of_mass_destruction
 *      --goal
 *      --question
 *
 * control of inputting of acquired wikipedia/dbpedia taxonomy and semantic (ex: dbpedia) knowledge
 *
 *
 */
public class NARWikiBrowser extends WikiBrowser {

    final StringProperty tags;
    final TextArea tagEdit = new TextArea();
    final BorderPane tagPane = new BorderPane();

    public NARWikiBrowser(String startWikiPage) {
        super(startWikiPage);

        tagEdit.setPrefRowCount(1);
        tagEdit.setWrapText(true);
        tagEdit.setPromptText("(empty)");
        tagPane.setCenter(tagEdit);


        tags = tagEdit.textProperty();

        Button inputButton = JFX.newIconButton(FontAwesomeIcon.PLAY);

        tagPane.setRight(inputButton);



        tags.addListener(c -> {

            if (tags.get().isEmpty()) {
                setBottom(null);
            }
            else {
                setBottom(tagPane);
            }

        });


    }



    @Override
    public void onTagClicked(String id) {
        tagEdit.appendText(id);
        tagEdit.appendText(" ");
    }
}
