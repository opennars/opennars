package nars.gui.output.audio;

import automenta.vivisect.Audio;
import automenta.vivisect.Sound;
import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.swing.NPanel;
import reactor.jarjar.jsr166e.extra.AtomicDouble;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
    protected void onShowing(boolean showing) {
        if (showing) {
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

        for (Sound s : sound.getSounds()) {
            playing.addRow(new Object[] { s.toString(), s.amplitude, s.pan });
        }

    }
    @Override
    public void run() {
        while (running) {

            clear();

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
