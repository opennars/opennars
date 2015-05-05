package nars.gui.output.audio;

import automenta.vivisect.Audio;
import automenta.vivisect.Sound;
import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.swing.NPanel;
import com.google.common.util.concurrent.AtomicDouble;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by me on 2/1/15.
 */
public class MixerPanel extends NPanel implements Runnable, SoundListener {

    private final Audio sound;
    private final DefaultTableModel playing;
    private final JTable playingTable;
    boolean running = false;
    final long updatePeriodMS = 512;
    final AtomicDouble pan = new AtomicDouble(0f);

    public MixerPanel(Audio sound) {
        super(new BorderLayout());

        this.sound = sound;
        sound.setListener(this);

        this.playing = new DefaultTableModel();

        playing.addColumn("Sound");
        playing.addColumn("Volume");
        playing.addColumn("Pan");

        this.playingTable = new JTable(playing);
        add(playingTable, BorderLayout.CENTER);

    }

    @Override
    protected void visibility(boolean appearedOrDisappeared) {
        if (appearedOrDisappeared) {
            running = true;
            new Thread(this).start();
        }
        else {
            running = false;
        }
    }

    protected void clear() {

        while (playing.getRowCount() > 0) {
            playing.removeRow(0);
        }

        List<Sound> ss = new ArrayList<Sound>( sound.getSounds() );
        for (Sound s : ss) {
            playing.addRow(new Object[]{s.toString(), s.amplitude, s.pan});
        }
    }

    @Override
    public void run() {
        while (running) {

            SwingUtilities.invokeLater(this::clear);

            try {
                Thread.sleep(updatePeriodMS);
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public float getX(float alpha) {
        return pan.floatValue();
    }

    @Override
    public float getY(float alpha) {
        return 0;
    }
}
