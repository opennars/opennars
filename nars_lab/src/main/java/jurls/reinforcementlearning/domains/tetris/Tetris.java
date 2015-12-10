/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
brian@tannerpages.com
http://brian.tannerpages.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package jurls.reinforcementlearning.domains.tetris;

import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.tetris.visualizer.TetrisVisualizer;

import java.awt.*;
import java.net.URL;


public class Tetris implements RLEnvironment {

    private double currentScore = 0;
    public TetrisState gameState = null;
    
    private int nextAction;
    private TetrisVisualizer vis;
    private double previousScore;

    public Tetris(int width, int height) {
        gameState = new TetrisState(width, height);
        restart();
    }

    @Override
    public Component component() {
        if (vis == null)
            vis = new TetrisVisualizer(this, 40);
        return vis;
    }

    @Override
    public int numActions() {
        return 6;
    }


    @Override
    public double getReward() {
        return Math.max(-30, Math.min(30, currentScore - previousScore))/30.0;
    }

    @Override
    public void frame() {
        step(nextAction);
        vis.render();
    }

    @Override
    public boolean takeAction(int action) {
        nextAction = action;
        return true;
    }

    @Override
    public double[] observe() {
        return gameState.asVector(false);
    }

    public void restart() {
        gameState.reset();
        gameState.spawn_block();
        gameState.blockMobile = true;
        previousScore = currentScore = 0;
    }

    public double step(int nextAction) {

        if (nextAction > 5 || nextAction < 0) {
            throw new RuntimeException("Invalid action selected in Tetrlais: " + nextAction);            
        }

        if (gameState.blockMobile) {
            gameState.take_action(nextAction);
            gameState.update();
        } else {
            gameState.spawn_block();
        }

        

        if (!gameState.gameOver()) {
            previousScore = currentScore;
            currentScore = gameState.get_score();
            return currentScore - previousScore;
        } else {
            restart();
            return 0;
        }
    }

    public void env_cleanup() {
    }



    public String getVisualizerClassName() {
        return TetrisVisualizer.class.getName();
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public URL getImageURL() {
        URL imageURL = Tetris.class.getResource("/images/tetris.png");
        return imageURL;
    }

//    private String makeTaskSpec() {
//        int boardSize = gameState.getHeight() * gameState.getWidth();
//        int numPieces = gameState.possibleBlocks.size();
//
//        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
//        theTaskSpecObject.setEpisodic();
//        theTaskSpecObject.setDiscountFactor(1.0d);
//        //First add the binary variables for the board
//        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1, boardSize));
//        //Now the binary features to tell what piece is falling
//        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1, numPieces));
//        //Now the actual board size in the observation. The reason this was here is/was because
//        //there was no way to add meta-data to the task spec before.
//        //First height
//        theTaskSpecObject.addDiscreteObservation(new IntRange(gameState.getHeight(), gameState.getHeight()));
//        //Then width
//        theTaskSpecObject.addDiscreteObservation(new IntRange(gameState.getWidth(), gameState.getWidth()));
//
//        theTaskSpecObject.addDiscreteAction(new IntRange(0, 5));
//        //This is actually a lie... the rewards aren't in that range.
//        theTaskSpecObject.setRewardRange(new DoubleRange(0, 8.0d));
//
//        //This is a better way to tell the rows and cols
//        theTaskSpecObject.setExtra("EnvName:Tetris HEIGHT:" + gameState.getHeight() + " WIDTH:" + gameState.getWidth() + " Revision: " + this.getClass().getPackage().getImplementationVersion());
//
//        String taskSpecString = theTaskSpecObject.toTaskSpec();
//
//        TaskSpec.checkTaskSpec(taskSpecString);
//        return taskSpecString;
//    }

    public int getWidth() {
        return gameState.worldWidth;
    }

    public int getHeight() {
        return gameState.worldHeight;
    }
}
//
//class DetailsProvider implements hasVersionDetails {
//
//    public String getName() {
//        return "Tetris 1.1";
//    }
//
//    public String getShortName() {
//        return "Tetris";
//    }
//
//    public String getAuthors() {
//        return "Brian Tanner, Leah Hackman, Matt Radkie, Andrew Butcher";
//    }
//
//    public String getInfoUrl() {
//        return "http://library.rl-community.org/tetris";
//    }
//
//    public String getDescription() {
//        return "Tetris problem from the reinforcement learning library.";
//    }
//}
