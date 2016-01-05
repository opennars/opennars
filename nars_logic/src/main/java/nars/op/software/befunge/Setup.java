package nars.op.software.befunge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by didrik on 30.12.2014.
 */
public enum Setup {
    ;

    public static void main(String[] args) throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Board board = new Board();
        for(int y = 0; y < 25; y++){
            char[] line = stdin.readLine().toCharArray();
            if (line.length == 0) break;
            for(int x = 0; x < 80 && x < line.length; x++){
                board.put(y, x, line[x]);
            }
        }

        Pointer p = new Pointer(board);
        while(p.step());
    }
}
