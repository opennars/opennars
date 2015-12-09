package nars.op.software.befunge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by didrik on 30.12.2014.
 */
public class Pointer {
    private int x, y, dx, dy;
    private final char WIDTH = 80;
    private final char HEIGHT = 25;
    private final Board board;

    //TODO use EnumMap
    private final Map<Character, Runnable> map;
    private final BefungeStack stack;

    public Pointer(Board board){
        x = y = 0;
        dx = 1;
        dy = 0;
        stack = new BefungeStack();
        this.board = board;
        map = new HashMap();
        generateMap();
    }

    private void move(){
        x = (x+dx) % WIDTH;
        y = (y+dy) % HEIGHT;
    }

    boolean step(){
        char c = board.get(y, x);
        if (c == '@') return false;
        if (Character.isDigit(c)) stack.push((long) c-'0');
        else map.get(c).run();
        move();
        return true;
    }

    private void generateMap(){
        map.put('+', () -> {
            long temp = stack.pop();
            temp += stack.pop();
            stack.push(temp);
        });

        map.put('-', () -> {
            long temp = -stack.pop();
            temp += stack.pop();
            stack.push(temp);
        });

        map.put('*', () -> {
            long temp = stack.pop();
            temp *= stack.pop();
            stack.push(temp);
        });

        map.put('/', () -> {
            long t1 = stack.pop();
            long t2 = stack.pop();
            stack.push(t2/t1);
        });

        map.put('%', () -> {
            long t1 = stack.pop();
            long t2 = stack.pop();
            stack.push(t2%t1);
        });

        map.put('!', () -> stack.push((long) (stack.pop() == 0 ? 1 : 0)));

        map.put('`', () -> stack.push((long) (stack.pop() <= stack.pop() ? 1 : 0)));

        map.put('<', this::left);

        map.put('>', this::right);

        map.put('v', this::down);

        map.put('^', this::up);

        map.put('?', () -> {
            int[] xarray = {-1, 1, 0, 0};
            int[] yarray = {0, 0, -1, 1};
            Random r = new Random();
            int t = r.nextInt(3);
            dx = xarray[t];
            dy = yarray[t];
        });

        map.put('_', () -> {
            if(stack.pop() == 0) right();
            else left();
        });

        map.put('|', () -> {
            if(stack.pop() == 0) down();
            else up();
        });

        map.put('"', () -> {
            move();
            char c = board.get(y, x);
            while(c != '"'){
                stack.push((long) c);
                move();
                c = board.get(y, x);
            }
        });

        map.put(':', () -> stack.push(stack.peek()));

        map.put('\\', () -> {
            long t1 = stack.pop();
            long t2 = stack.pop();
            stack.push(t2);
            stack.push(t1);
        });

        map.put('$', stack::pop);

        map.put('.', () -> System.out.print(stack.pop()));

        map.put(',', () -> System.out.print((char) stack.pop().shortValue()));

        map.put('#', this::move);

        map.put('p', () -> {
            int y = stack.pop().intValue();
            int x = stack.pop().intValue();
            char v = (char)stack.pop().shortValue();
            board.put(y, x, v);
        });

        map.put('g', () -> {
            int y = stack.pop().intValue();
            int x = stack.pop().intValue();
            char c = board.get(y, x);
            stack.push((long) c);
        });

        map.put(' ', () -> {});
    }

    private void right(){
        dx = 1;
        dy = 0;
    }

    private void left(){
        dx = -1;
        dy = 0;
    }

    private void down(){
        dx = 0;
        dy = 1;
    }

    private void up(){
        dx = 0;
        dy = -1;
    }

}
