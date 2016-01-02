package nars.guifx.terminal;

import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import nars.util.Texts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sergei.malafeev
 */
public class TerminalWatcher implements Runnable {
    //private static final Logger logger = LoggerFactory.getLogger(TerminalWatcher.class);
    private final InputStream outFromChannel;
    private final TextArea textArea;

    public TerminalWatcher(InputStream outFromChannel, TextArea textArea) {
        this.outFromChannel = outFromChannel;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        try {
            char[] buff = new char[1024];
            int read;
            while ((read = isr.read(buff)) != -1) {
                String s = new String(buff, 0, read);

                Pattern pattern = Pattern.compile("^(\b+)(.*)");
                Matcher mathcher = pattern.matcher(s);
                if (mathcher.find()) {
                    String firstBacks = mathcher.group(1);
                    s = mathcher.group(2);
                    moveCursorLeft(firstBacks.length(), !Strings.isNullOrEmpty(s));
                }

                boolean eraseLine = false;
                if (s.contains("\u001B[K")) {
                    eraseLine = true;
                }

                boolean middleErase = false;
                int numBacks = 0;
                if (s.contains("\u001B[P")) {
                    middleErase = true;
                    numBacks = Texts.count(s, '\b');
                }

                if ("\u001B[C".equals(s)) {
                    moveCursorRight(1);
                }

                String res = removeEscapes(s);

                if (!res.isEmpty()) {
                    boolean finalMiddleErase = middleErase;
                    int finalNumBacks = numBacks;
                    Platform.runLater(() -> {
                        if (finalMiddleErase) {
                            textArea.insertText(textArea.getCaretPosition(), res);
                            textArea.positionCaret(textArea.getCaretPosition() - finalNumBacks);
                        } else {
                            textArea.appendText(res);
                        }
                    });
                } else if (eraseLine) {
                    Platform.runLater(() -> textArea.deleteText(textArea.getCaretPosition(), textArea.getLength()));
                }
            }
        } catch (InterruptedIOException e) {
            //ignore
        } catch (IOException e) {
            //logger.warn("failed to read from ssh", e);
            e.printStackTrace();
        }
    }

    private String removeEscapes(String source) {
        String target = source.replaceAll("\u001B[\\(\\)][AB012]", "");

        target = target.replaceAll("\u001B\\[\\?*\\d*;*\\d*[a-zA-Z]", "");

        target = target.replaceAll("\u001B[><=A-Z]", "");

        target = target.replaceAll("\\[\\d*;*\\d*m", "");

        target = target.replaceAll("\u000F", "");

        target = target.replaceAll("\u0007", "");

        target = target.replaceAll("\b", "");

        return target;
    }

    private void moveCursorRight(int count) {
        Platform.runLater(() -> textArea.positionCaret(textArea.getCaretPosition() + count));
    }

    private void moveCursorLeft(int count, boolean delete) {
        Platform.runLater(() -> {
            textArea.positionCaret(textArea.getCaretPosition() - count);
            if (delete) {
                textArea.deleteText(textArea.getCaretPosition(), textArea.getLength());
            }
        });
    }
}

