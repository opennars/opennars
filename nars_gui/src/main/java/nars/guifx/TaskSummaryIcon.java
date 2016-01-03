package nars.guifx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import nars.guifx.util.ColorArray;
import nars.task.Task;

/**
 * Created by me on 8/14/15.
 */
public class TaskSummaryIcon extends SummaryIcon implements Runnable {

    static final int colorLevels = 32;
    static final double VISIBLE_BUDGET_CHANGE = 0.5 / colorLevels;
    static final ColorArray grayRange = new ColorArray(colorLevels, Color.DARKGRAY, Color.WHITE);
    static final ColorArray beliefRange = new ColorArray(colorLevels, Color.RED, Color.ORANGE);
    static final ColorArray goalRange = new ColorArray(colorLevels, Color.BLUE, Color.GREEN);

    private final Task task;


    transient float lastPriority = -1;

    public TaskSummaryIcon(Task i, Region parent) {

        task = i;


        parent.heightProperty().addListener(this);
        changed(null,null,parent.heightProperty().get());
    }

    @Override
    protected void repaint() {
        paintConstants();

        run();
    }

    final Color getBudgetColor(float pri) {
        return grayRange.get(pri);
    }
    final Color getBeliefColor(float freq, float conf) {
        return beliefRange.get(freq);
    }
    final Color getGoalColor(float freq, float conf) {
        return goalRange.get(freq);
    }

    void paintConstants() {
        double W = getWidth();
        double H = getHeight();
        if (W*H == 0) return;

        GraphicsContext g = getGraphicsContext2D();

        if (task == null || task.isQuestOrQuestion()) {
            //immediate?
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
        double W = getWidth();
        double H = getHeight();
        if (W*H == 0) return;

        //TODO only clear if size changed, because it can just paint on top
        //g.clearRect(0, 0, W, H);

        float p = task.getBudget().getPriorityIfNaNThenZero();

        if (Math.abs(lastPriority - p) > VISIBLE_BUDGET_CHANGE) {

            g.setFill(getBudgetColor(p));
            g.fillRect(0, 0, W/3, H);

            lastPriority = p;
        }

    }

}
