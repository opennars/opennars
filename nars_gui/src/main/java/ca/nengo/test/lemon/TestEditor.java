package ca.nengo.test.lemon;
import ca.nengo.ui.NengrowPanel;

public class TestEditor {
    public static void main(String[] args) {
        NengrowPanel panel = new NengrowPanel();
        {
            Editor editor = new Editor("editor1", 60, 80);
            Cursor c = new Cursor("cursor1", editor);
            editor.area.lines.setLine(3, "<[TEXT_SYSTEM] --> [operational]>.");
            panel.add(editor);
            //lang.update(mesh);
        }
        panel.newWindow(800, 600);
    }
}
