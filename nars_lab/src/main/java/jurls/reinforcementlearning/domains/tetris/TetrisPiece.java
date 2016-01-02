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

public class TetrisPiece {

    int[][][] thePiece = new int[4][5][5];
    int currentOrientation = 0;

    public void setShape(int Direction, int[] row0, int[] row1, int[] row2, int[] row3, int[] row4) {
        thePiece[Direction][0] = row0;
        thePiece[Direction][1] = row1;
        thePiece[Direction][2] = row2;
        thePiece[Direction][3] = row3;
        thePiece[Direction][4] = row4;
    }

    public int[][] getShape(int whichOrientation) {
        return thePiece[whichOrientation];
    }

    public static TetrisPiece makeSquare() {
        TetrisPiece newPiece = new TetrisPiece();

        //Orientation 0,1,2,3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 1, 1, 0};
        int[] row2 = {0, 0, 1, 1, 0};
        int[] row3 = {0, 0, 0, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(0, row0, row1, row2, row3, row4);
        newPiece.setShape(1, row0, row1, row2, row3, row4);
        newPiece.setShape(2, row0, row1, row2, row3, row4);
        newPiece.setShape(3, row0, row1, row2, row3, row4);

        return newPiece;
    }

    public static TetrisPiece makeTri() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 1, 1, 1, 0};
            int[] row3 = {0, 0, 0, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
        }
        {
            //Orientation 1
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 0, 1, 1, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(1, row0, row1, row2, row3, row4);
        }

        {
            //Orientation 2
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 0, 0, 0};
            int[] row2 = {0, 1, 1, 1, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }
        //Orientation 3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 1, 0, 0};
        int[] row2 = {0, 1, 1, 0, 0};
        int[] row3 = {0, 0, 1, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(3, row0, row1, row2, row3, row4);

        return newPiece;
    }

    public static TetrisPiece makeLine() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0+2
            int[] row0 = {0, 0, 1, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 0, 1, 0, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }

        //Orientation 1+3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 0, 0, 0};
        int[] row2 = {0, 1, 1, 1, 1};
        int[] row3 = {0, 0, 0, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(1, row0, row1, row2, row3, row4);
        newPiece.setShape(3, row0, row1, row2, row3, row4);
        return newPiece;

    }

    public static TetrisPiece makeSShape() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0+2
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 1, 0, 0, 0};
            int[] row2 = {0, 1, 1, 0, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }

        //Orientation 1+3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 1, 1, 0};
        int[] row2 = {0, 1, 1, 0, 0};
        int[] row3 = {0, 0, 0, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(1, row0, row1, row2, row3, row4);
        newPiece.setShape(3, row0, row1, row2, row3, row4);
        return newPiece;

    }

    public static TetrisPiece makeZShape() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0+2
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 1, 1, 0, 0};
            int[] row3 = {0, 1, 0, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }

        //Orientation 1+3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 1, 1, 0, 0};
        int[] row2 = {0, 0, 1, 1, 0};
        int[] row3 = {0, 0, 0, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(1, row0, row1, row2, row3, row4);
        newPiece.setShape(3, row0, row1, row2, row3, row4);
        return newPiece;

    }

    public static TetrisPiece makeLShape() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 0, 1, 0, 0};
            int[] row3 = {0, 0, 1, 1, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
        }
        {
            //Orientation 1
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 0, 0, 0};
            int[] row2 = {0, 1, 1, 1, 0};
            int[] row3 = {0, 1, 0, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(1, row0, row1, row2, row3, row4);
        }

        {
            //Orientation 2
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 1, 1, 0, 0};
            int[] row2 = {0, 0, 1, 0, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }
        //Orientation 3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 0, 1, 0};
        int[] row2 = {0, 1, 1, 1, 0};
        int[] row3 = {0, 0, 0, 0, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(3, row0, row1, row2, row3, row4);

        return newPiece;
    }

    public static TetrisPiece makeJShape() {
        TetrisPiece newPiece = new TetrisPiece();

        {
            //Orientation 0
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 0, 0};
            int[] row2 = {0, 0, 1, 0, 0};
            int[] row3 = {0, 1, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(0, row0, row1, row2, row3, row4);
        }
        {
            //Orientation 1
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 1, 0, 0, 0};
            int[] row2 = {0, 1, 1, 1, 0};
            int[] row3 = {0, 0, 0, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(1, row0, row1, row2, row3, row4);
        }

        {
            //Orientation 2
            int[] row0 = {0, 0, 0, 0, 0};
            int[] row1 = {0, 0, 1, 1, 0};
            int[] row2 = {0, 0, 1, 0, 0};
            int[] row3 = {0, 0, 1, 0, 0};
            int[] row4 = {0, 0, 0, 0, 0};
            newPiece.setShape(2, row0, row1, row2, row3, row4);
        }
        //Orientation 3
        int[] row0 = {0, 0, 0, 0, 0};
        int[] row1 = {0, 0, 0, 0, 0};
        int[] row2 = {0, 1, 1, 1, 0};
        int[] row3 = {0, 0, 0, 1, 0};
        int[] row4 = {0, 0, 0, 0, 0};
        newPiece.setShape(3, row0, row1, row2, row3, row4);

        return newPiece;
    }

    @Override
    public String toString() {
        StringBuilder shapeBuffer = new StringBuilder();
        for (int i = 0; i < thePiece[currentOrientation].length; i++) {
            for (int j = 0; j < thePiece[currentOrientation][i].length; j++) {
                shapeBuffer.append(' ').append(thePiece[currentOrientation][i][j]);
            }
            shapeBuffer.append('\n');
        }
        return shapeBuffer.toString();

    }
}
