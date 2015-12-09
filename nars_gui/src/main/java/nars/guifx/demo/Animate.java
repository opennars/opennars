package nars.guifx.demo;

import javafx.animation.AnimationTimer;

import java.util.function.Consumer;


public final class Animate extends AnimationTimer {

    private final Consumer<Animate> run;
    private int periodMS;
    private long last;

    public Animate(int periodMS, Consumer<Animate> r) {
        super();
        this.periodMS = periodMS;
        run = r;
    }

    @Override
    public void handle(long nowNS) {
        long now = nowNS/1000000L; //ns -> ms
        if (now - last > periodMS) {
            last = now;
            run.accept(this);
        }
    }

    public int getPeriod() {
        return periodMS;
    }
}
