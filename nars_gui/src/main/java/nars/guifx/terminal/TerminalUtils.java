package nars.guifx.terminal;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sergei.malafeev
 */
public enum TerminalUtils {
    ;
    private static final Map<Integer, byte[]> keyMap;

    public static byte[] getCode(KeyEvent event, TerminalPanel terminalPanel) {
        if (event.getCode() == KeyCode.BACK_SPACE) {
            return keyMap.get(72);
        }
        //noinspection IfStatementWithTooManyBranches
        if (event.isControlDown() && event.getCode() == KeyCode.L) {
            terminalPanel.getTextArea().clear();
            return keyMap.get(76);
        }
        if (event.getCode() == KeyCode.SPACE) {
            return new byte[]{0x20};
        }
        if (event.getCode() == KeyCode.TAB) {
            return keyMap.get(9);
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            return keyMap.get(27);
        }
        if (event.getCode() == KeyCode.UP) {
            return keyMap.get(38);
        }
        if (event.getCode() == KeyCode.DOWN) {
            return keyMap.get(40);
        }
        if (event.getCode() == KeyCode.LEFT) {
            return keyMap.get(37);
        }
        if (event.getCode() == KeyCode.RIGHT) {
            return keyMap.get(39);
        } /*else if (event.getCode() == KeyCode.DELETE) {
            //terminalPanel.getTextArea().positionCaret(terminalPanel.getTextArea().getCaretPosition() + 1);
            return keyMap.get(8);
        }*/
        return null;
    }

    static {
        keyMap = new HashMap<>();
        //ESC
        keyMap.put(27, new byte[]{(byte) 0x1b});
        //ENTER
        keyMap.put(13, new byte[]{(byte) 0x0d});
        //LEFT
        keyMap.put(37, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x44});
        //UP
        keyMap.put(38, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x41});
        //RIGHT
        keyMap.put(39, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x43});
        //DOWN
        keyMap.put(40, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x42});
        //DEL
        keyMap.put(8, new byte[]{(byte) 0x7f});
        //TAB
        keyMap.put(9, new byte[]{(byte) 0x09});
        //CTR
        keyMap.put(17, new byte[]{});
        //CTR-A
        keyMap.put(65, new byte[]{(byte) 0x01});
        //CTR-B
        keyMap.put(66, new byte[]{(byte) 0x02});
        //CTR-C
        keyMap.put(67, new byte[]{(byte) 0x03});
        //CTR-D
        keyMap.put(68, new byte[]{(byte) 0x04});
        //CTR-E
        keyMap.put(69, new byte[]{(byte) 0x05});
        //CTR-F
        keyMap.put(70, new byte[]{(byte) 0x06});
        //CTR-G
        keyMap.put(71, new byte[]{(byte) 0x07});
        //BACKSPACE
        keyMap.put(72, new byte[]{(byte) 0x08});
        //CTR-I
        keyMap.put(73, new byte[]{(byte) 0x09});
        //CTR-J
        keyMap.put(74, new byte[]{(byte) 0x0A});
        //CTR-K
        keyMap.put(75, new byte[]{(byte) 0x0B});
        //CTR-L
        keyMap.put(76, new byte[]{(byte) 0x0C});
        //CTR-M
        keyMap.put(77, new byte[]{(byte) 0x0D});
        //CTR-N
        keyMap.put(78, new byte[]{(byte) 0x0E});
        //CTR-O
        keyMap.put(79, new byte[]{(byte) 0x0F});
        //CTR-P
        keyMap.put(80, new byte[]{(byte) 0x10});
        //CTR-Q
        keyMap.put(81, new byte[]{(byte) 0x11});
        //CTR-R
        keyMap.put(82, new byte[]{(byte) 0x12});
        //CTR-S
        keyMap.put(83, new byte[]{(byte) 0x13});
        //CTR-T
        keyMap.put(84, new byte[]{(byte) 0x14});
        //CTR-U
        keyMap.put(85, new byte[]{(byte) 0x15});
        //CTR-V
        keyMap.put(86, new byte[]{(byte) 0x16});
        //CTR-W
        keyMap.put(87, new byte[]{(byte) 0x17});
        //CTR-X
        keyMap.put(88, new byte[]{(byte) 0x18});
        //CTR-Y
        keyMap.put(89, new byte[]{(byte) 0x19});
        //CTR-Z
        keyMap.put(90, new byte[]{(byte) 0x1A});
        //CTR-[
        keyMap.put(219, new byte[]{(byte) 0x1B});
        //CTR-]
        keyMap.put(221, new byte[]{(byte) 0x1D});
    }
}
