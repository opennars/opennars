package nars.op.software.befunge;

import com.sun.xml.internal.bind.v2.TODO;

import java.util.Stack;

/**
 * Created by didrik on 30.12.2014.
 */
public class BefungeStack {
    //TODO replace with Long primitive
    Stack<Long> stack;

    public BefungeStack(){
        stack = new Stack();
    }

    void push(Long l){
        stack.push(l);
    }

    Long pop(){
        return stack.isEmpty() ? (long) 0 : stack.pop();
    }

    Long peek(){
        return stack.isEmpty() ? (long) 0 : stack.peek();
    }
}
