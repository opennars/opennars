package nars.guifx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import nars.guifx.util.ColorArray;
import nars.task.Task;

/**
 * Created by me on 8/14/15.
 */
public class TaskSummaryIcon extends Canvas implements Runnable, ChangeListener<Number> {

    final static int colorLevels = 32;
    final static double VISIBLE_BUDGET_CHANGE = 0.5 / colorLevels;
    final static ColorArray grayRange = new ColorArray(colorLevels, Color.DARKGRAY, Color.WHITE);
    final static ColorArray beliefRange = new ColorArray(colorLevels, Color.RED, Color.ORANGE);
    final static ColorArray goalRange = new ColorArray(colorLevels, Color.BLUE, Color.GREEN);

    private final Task task;

    transient float lastPriority = -1;

    public TaskSummaryIcon(Task i, Region parent) {
        super();

        this.task = i;

        parent.heightProperty().addListener(this);
        repaint(parent.heightProperty().get());
    }

    public TaskSummaryIcon width(double w) {
        setWidth(w);
        return this;
    }

    protected void repaint() {
        paintConstants();

        run();
    }

    public Color getBudgetColor(float pri) {
        return grayRange.get(pri);
    }
    public Color getBeliefColor(float freq, float conf) {
        return beliefRange.get(freq, conf);
    }
    public Color getGoalColor(float freq, float conf) {
        return goalRange.get(freq, conf);
    }

    public void paintConstants() {
        GraphicsContext g = getGraphicsContext2D();
        final double W = getWidth();
        final double H = getHeight();
        if (W*H == 0) return;

        if (task.getTerm() == null) {
            //immediate?
        }
        else if (task.isQuestOrQuestion()) {
            //show solution priority?
        }
        else {
            if (task.isJudgment()) {
                g.setFill(getBeliefColor(task.getFrequency(), task.getConfidence()) );
            }
            else if (task.isGoal()) {
                g.setFill(getGoalColor(task.getFrequency(), task.getConfidence()));
            }
            g.fillRect(W/3, 0, W/3, H);
        }

    }

    /** paints non-constant changeable aspects */
    @Override public void run() {
        GraphicsContext g = getGraphicsContext2D();
        final double W = getWidth();
        final double H = getHeight();
        if (W*H == 0) return;

        //TODO only clear if size changed, because it can just paint on top
        //g.clearRect(0, 0, W, H);

        float p = task.getBudget().getPriorityIfNaNThenZero();

        if (Math.abs(lastPriority - p) > VISIBLE_BUDGET_CHANGE) {

            g.setFill(getBudgetColor(p));
            g.fillRect(0, 0, W/3, H);

            this.lastPriority = p;
        }

    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double h = newValue.doubleValue();
        repaint(h);
    }

    private void repaint(double h) {
        h *= 0.5;

        setHeight(h);
        setWidth(h * 3);

        lastPriority = -1;

        paintConstants();
        repaint();
    }
}
