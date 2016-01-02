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

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;


public class TetrisState {
    /*Action values*/
    static final int LEFT = 0; /*Action value for a move left*/
    static final int RIGHT = 1; /*Action value for a move right*/
    static final int CW = 2; /*Action value for a clockwise rotation*/
    static final int CCW = 3; /*Action value for a counter clockwise rotation*/
    static final int NONE = 4; /*The no-action Action*/
    static final int FALL = 5; /* fall down */

    float fallingBLockColor = 0.5f;

    private final Random randomGenerator = new Random();
    
    public boolean blockMobile = true;
    public int currentBlockId;/*which block we're using in the block table*/

    public int currentRotation = 0;
    public int currentX;/* where the falling block is currently*/

    public int currentY;
    public int score;/* what is the current_score*/

    public boolean is_game_over;/*have we reached the end state yet*/

    public int worldWidth=10;/*how wide our board is*/
    public int worldHeight=20;/*how tall our board is*/
    
    

    public double[] worldState;/*what the world looks like without the current block*/

    //	/*Hold all the possible bricks that can fall*/
    Vector<TetrisPiece> possibleBlocks = new Vector<>();
    private double[] worldObservation;



    public TetrisState(int width, int height) {
        worldWidth = width;
        worldHeight = height;
        possibleBlocks.add(TetrisPiece.makeLine());
        possibleBlocks.add(TetrisPiece.makeSquare());
        possibleBlocks.add(TetrisPiece.makeTri());
        possibleBlocks.add(TetrisPiece.makeSShape());
        possibleBlocks.add(TetrisPiece.makeZShape());
        possibleBlocks.add(TetrisPiece.makeLShape());
        possibleBlocks.add(TetrisPiece.makeJShape());

        worldState=new double[worldHeight*worldWidth];
        reset();
    }

    public void reset() {
        currentX = worldWidth / 2 - 1;
        currentY = 0;
        score = 0;
        for (int i = 0; i < worldState.length; i++) {
            worldState[i] = 0;
        }
        currentRotation = 0;
        is_game_over = false;
    }

    public double[] asVector(boolean monochrome) {
        //eget observation with only the state space
            
        if (worldObservation == null) 
            worldObservation = new double[worldState.length + possibleBlocks.size() + 2];
        else
            Arrays.fill(worldObservation, 0);
            
            int x = 0;
            for (double i : worldState) {
                if (monochrome)
                    worldObservation[x] = i > 0 ? 1.0 : -1.0;
                else
                    worldObservation[x] = i > 0 ? i : - 1.0;
                x++;
            }
            
            
            writeCurrentBlock(worldObservation, fallingBLockColor);
                        
            //Set the bit vector value for which block is currently following
            worldObservation[worldState.length + currentBlockId] = 1;
            return worldObservation;

    }


    private void writeCurrentBlock(double[] game_world, double color) {
        int[][] thisPiece = possibleBlocks.get(currentBlockId).getShape(currentRotation);

        if (color == -1)
            color = currentBlockId + 1;
        for (int y = 0; y < thisPiece[0].length; ++y) {
            for (int x = 0; x < thisPiece.length; ++x) {
                if (thisPiece[x][y] != 0) {
                    //Writing currentBlockId +1 because blocks are 0 indexed, and we want spots to be
                    //0 if they are clear, and >0 if they are not.
                    int linearIndex = p(currentX + x, currentY + y);
                    /*if(linearIndex<0){
                        System.err.printf("Bogus linear index %d for %d + %d, %d + %d\n",linearIndex,currentX,x,currentY,y);
                        Thread.dumpStack();
                        System.exit(1);
                    }*/
                    game_world[linearIndex] = color;
                }
            }
        }

    }

    public boolean gameOver() {
        return is_game_over;
    }

    /* This code applies the action, but doesn't do the default fall of 1 square */
    public void take_action(int theAction) {

        if (theAction > 5 || theAction < 0) {
            System.err.println("Invalid action selected in Tetrlais: " + theAction);
            //Random >=0 < 6
            theAction = randomGenerator.nextInt(6);
        }

        int nextRotation = currentRotation;
        int nextX = currentX;
        int nextY = currentY;

        switch (theAction) {
            case CW:
                nextRotation = (currentRotation + 1) % 4;
                break;
            case CCW:
                nextRotation = (currentRotation - 1);
                if (nextRotation < 0) {
                    nextRotation = 3;
                }
                break;
            case LEFT:
                nextX = currentX - 1;
                break;
            case RIGHT:
                nextX = currentX + 1;
                break;
            case FALL:
                nextY = currentY;

                boolean isInBounds = true;
                boolean isColliding = false;

                //Fall until you hit something then back up once
                while (isInBounds && !isColliding) {
                    nextY++;
                    isInBounds = inBounds(nextX, nextY, nextRotation);
                    if (isInBounds) {
                        isColliding = colliding(nextX, nextY, nextRotation);
                    }
                }
                nextY--;
                break;
            default:
                break;
        }
        //Check if the resulting position is legal. If so, accept it.
        //Otherwise, don't change anything
        if (inBounds(nextX, nextY, nextRotation)) {
            if (!colliding(nextX, nextY, nextRotation)) {
                currentRotation = nextRotation;
                currentX = nextX;
                currentY = nextY;
            }
        }

    }

    /**
     * Calculate the learn array position from (x,y) components based on 
     * worldWidth.
     * Package level access so we can use it in tests.
     * @param x
     * @param y
     * @return
     */
    int p(int x, int y) {
        return y * worldWidth + x;
        //assert returnValue >= 0 : " "+y+" * "+worldWidth+" + "+x+" was less than 0.";
        //return returnValue;
    }

    /**
     * Check if any filled part of the 5x5 block array is either out of bounds
     * or overlapping with something in wordState
     * @param checkX X location of the left side of the 5x5 block array
     * @param checkY Y location of the top of the 5x5 block array
     * @param checkOrientation Orientation of the block to check
     * @return
     */
    private boolean colliding(int checkX, int checkY, int checkOrientation) {
        int[][] thePiece = possibleBlocks.get(currentBlockId).getShape(checkOrientation);
        try {

            for (int y = 0; y < thePiece[0].length; ++y) {
                for (int x = 0; x < thePiece.length; ++x) {
                    if (thePiece[x][y] != 0) {
                        //First check if a filled in piece of the block is out of bounds!
                        //if the height of this square is negative or the X of 
                        //this square is negative, then we're "colliding" with the wall
                        if (checkY + y < 0 || checkX + x < 0) {
                            return true;
                        }

                        //if the height of this square is more than the board size or the X of 
                        //this square is more than the board size, then we're "colliding" with the wall
                        if (checkY + y >= worldHeight || checkX + x >= worldWidth) {
                            return true;
                        }

                        //Otherwise check if it hits another piece
                        int linearArrayIndex = p(checkX + x, checkY + y);
                        if (worldState[linearArrayIndex] != 0) {
                            return true;
                        }
                    }
                }
            }
            return false;

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: ArrayIndexOutOfBoundsException in GameState::colliding called with params: " + checkX + " , " + checkY + ", " + checkOrientation);
            System.err.println("Error: The Exception was: " + e);
            Thread.dumpStack();
            System.err.println("Returning true from colliding to help save from error");
            System.err.println("Setting is_game_over to true to hopefully help us to recover from this problem");
            is_game_over = true;
            return true;
        }
    }

    private boolean collidingCheckOnlySpotsInBounds(int checkX, int checkY, int checkOrientation) {
        int[][] thePiece = possibleBlocks.get(currentBlockId).getShape(checkOrientation);
        try {

            for (int y = 0; y < thePiece[0].length; ++y) {
                for (int x = 0; x < thePiece.length; ++x) {
                    if (thePiece[x][y] != 0) {

                        //This checks to see if x and y are in bounds
                        if ((checkX + x >= 0 && checkX + x < worldWidth && checkY + y >= 0 && checkY + y < worldHeight)) {
                            //This array location is in bounds  
                            //Check if it hits another piece
                            int linearArrayIndex = p(checkX + x, checkY + y);
                            if (worldState[linearArrayIndex] != 0) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: ArrayIndexOutOfBoundsException in GameState::collidingCheckOnlySpotsInBounds called with params: " + checkX + " , " + checkY + ", " + checkOrientation);
            System.err.println("Error: The Exception was: " + e);
            Thread.dumpStack();
            System.err.println("Returning true from colliding to help save from error");
            System.err.println("Setting is_game_over to true to hopefully help us to recover from this problem");
            is_game_over = true;
            return true;
        }
    }

    /**
     * This function checks every filled part of the 5x5 block array and sees if
     * that piece is in bounds if the entire block is sitting at (checkX,checkY)
     * on the board.
     * @param checkX X location of the left side of the 5x5 block array
     * @param checkY Y location of the top of the 5x5 block array
     * @param checkOrientation Orientation of the block to check
     * @return
     */
    private boolean inBounds(int checkX, int checkY, int checkOrientation) {
        try {
            int[][] thePiece = possibleBlocks.get(currentBlockId).getShape(checkOrientation);

            for (int y = 0; y < thePiece[0].length; ++y) {
                for (int x = 0; x < thePiece.length; ++x) {
                    if (thePiece[x][y] != 0) {
                        //if ! (thisX is non-negative AND thisX is less than width
                        // AND thisY is non-negative AND thisY is less than height)
                        //Through demorgan's law is
                        //if thisX is negative OR thisX is too big or 
                        //thisY is negative OR this Y is too big
                        if (!(checkX + x >= 0 && checkX + x < worldWidth && checkY + y >= 0 && checkY + y < worldHeight)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: ArrayIndexOutOfBoundsException in GameState::inBounds called with params: " + checkX + " , " + checkY + ", " + checkOrientation);
            System.err.println("Error: The Exception was: " + e);
            Thread.dumpStack();
            System.err.println("Returning false from inBounds to help save from error.  Not sure if that's wise.");
            System.err.println("Setting is_game_over to true to hopefully help us to recover from this problem");
            is_game_over = true;
            return false;
        }

    }

    private boolean nextInBounds() {
        return inBounds(currentX, currentY + 1, currentRotation);
    }

    private boolean nextColliding() {
        return colliding(currentX, currentY + 1, currentRotation);
    }

    /*Ok, at this point, they've just taken their action.  We now need to make them fall 1 spot, and check if the game is over, etc */
    public void update() {
        // Sanity check.  The game piece should always be in bounds.
        if (!inBounds(currentX, currentY, currentRotation)) {
            System.err.println("In GameState.Java the Current Position of the board is Out Of Bounds... Consistency Check Failed");
        }

        //Need to be careful here because can't check nextColliding if not in bounds

        //onSomething means we're basically done with this piece
        boolean onSomething = false;
        if (!nextInBounds()) {
            onSomething = true;
        }
        if (!onSomething) {
            if (nextColliding()) {
                onSomething = true;
            }
        }

        if (onSomething) {
            blockMobile = false;
            writeCurrentBlock(worldState, -1);
            checkIfRowAndScore();
        } else {
            //fall
            currentY += 1;
        }
    }

    public void spawn_block() {
        blockMobile = true;

        currentBlockId = randomGenerator.nextInt(possibleBlocks.size());

        currentRotation = 0;
        currentX = (worldWidth / 2) - 2;
        currentY = -4;
        
        score += getWidth() / 2;

//Colliding checks both bounds and piece/piece collisions.  We really only want the piece to be falling
//If the filled parts of the 5x5 piece are out of bounds.. IE... we want to stop falling when its all on the screen
        boolean hitOnWayIn = false;
        while (!inBounds(currentX, currentY, currentRotation)) {
            //We know its not in bounds, and we're bringing it in.  Let's see if it would have hit anything...
            hitOnWayIn = collidingCheckOnlySpotsInBounds(currentX, currentY, currentRotation);
            currentY++;
        }
        is_game_over = colliding(currentX, currentY, currentRotation) || hitOnWayIn;
        if (is_game_over) {
            blockMobile = false;
        }
    }

    void checkIfRowAndScore() {
        int numRowsCleared = 0;

        //Start at the bottom, work way up
        for (int y = worldHeight - 1; y >= 0; --y) {
            if (isRow(y)) {
                removeRow(y);
                numRowsCleared += 1;
                y += 1;
            }
        }
        
        //1 line == 1
        //2 lines == 2
        //3 lines == 4
        //4 lines == 8
        score += Math.pow(2.0d, numRowsCleared-1);
    }

    /**
     * Check if a row has been completed at height y.
     * Short circuits, returns false whenever we hit an unfilled spot.
     * @param y
     * @return
     */
    boolean isRow(int y) {
        for (int x = 0; x < worldWidth; ++x) {
            int linearIndex = p(x, y);
            if (worldState[linearIndex] == 0) {
                return false;
            }
        }
        return true;
    }
    boolean isEmpty(int y) {
        for (int x = 0; x < worldWidth; ++x) {
            int linearIndex = p(x, y);
            if (worldState[linearIndex] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Dec 13/07.  Radkie + Tanner found 2 bugs here.
     * Bug 1: Top row never gets updated when removing lower rows. So, if there are 
     * pieces in the top row, and we clear something, they will float there.
     * @param y
     */
    void removeRow(int y) {
        if (!isRow(y)) {
            System.err.println("In GameState.java remove_row you have tried to remove a row which is not complete. Failed to remove row");
            return;
        }

        for (int x = 0; x < worldWidth; ++x) {
            int linearIndex = p(x, y);
            worldState[linearIndex] = 0;
        }

        //Copy each row down one (except the top)
        for (int ty = y; ty > 0; --ty) {
            for (int x = 0; x < worldWidth; ++x) {
                int linearIndexTarget = p(x, ty);
                int linearIndexSource = p(x, ty - 1);
                worldState[linearIndexTarget] = worldState[linearIndexSource];
            }
        }

        //Clear the top row
        for (int x = 0; x < worldWidth; ++x) {
            int linearIndex = p(x, 0);
            worldState[linearIndex] = 0;
        }

    }
    
    public int numEmptyRows() {
        int t = 0;
        for (int y = 0; y < getHeight(); y++)
            if (isEmpty(y))
                t++;
        return t;
    }

    public double get_score() {
        return (score + getWidth() * numEmptyRows());
    }

    public int getWidth() {
        return worldWidth;
    }

    public int getHeight() {
        return worldHeight;
    }

//    public int[] getNumberedStateSnapShot() {
//        int[] numberedStateCopy = new int[worldState.length];
//        for (int i = 0; i < worldState.length; i++) {
//            numberedStateCopy[i] = worldState[i];
//        }
//        writeCurrentBlock(numberedStateCopy);
//        return numberedStateCopy;
//
//    }

    public int getCurrentPiece() {
        return currentBlockId;
    }

    /**
     * Utility methd for debuggin
     * 
     */
    public void printState() {
        int index = 0;
        for (int i = 0; i < worldHeight - 1; i++) {
            for (int j = 0; j < worldWidth; j++) {
                System.out.print(worldState[i * worldWidth + j]);
            }
            System.out.print("\n");
        }
        System.out.println("-------------");


    }

    public Random getRandom() {
        return randomGenerator;
    }

    
    /*End of Tetris Helper Functions*/

    public TetrisState(TetrisState stateToCopy) {
        blockMobile = stateToCopy.blockMobile;
        currentBlockId = stateToCopy.currentBlockId;
        currentRotation = stateToCopy.currentRotation;
        currentX = stateToCopy.currentX;
        currentY = stateToCopy.currentY;
        score = stateToCopy.score;
        is_game_over = stateToCopy.is_game_over;
        worldWidth = stateToCopy.worldWidth;
        worldHeight = stateToCopy.worldHeight;

        worldState = new double[stateToCopy.worldState.length];
        System.arraycopy(stateToCopy.worldState, 0, worldState, 0, worldState.length);

        possibleBlocks = new Vector<>();
        //hopefully nobody modifies the pieces as they go
        possibleBlocks.addAll(stateToCopy.possibleBlocks.stream().collect(Collectors.toList()));

    }
}

