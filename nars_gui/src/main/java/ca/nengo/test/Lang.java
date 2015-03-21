package ca.nengo.test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by you on 20.3.15.
 */



public class Lang {
    // SYMBOLS - LANGUAGE UNDERLAY WIDGETS

    public class Symbol{};

    public class List extends Symbol{};
    public class Syntaxed extends Symbol{};
    public class Word extends Symbol{};
    public class Number extends Word{};

    //ELEMENTS OF A GRAMMAR RULE (OF ONE CHOICE), BESIDES STRING:

    private static class Sym {
        public static enum Param {LIST_ELEM_TYPE, SRSTR};
        Map<Param, Object> params; // like the type of items of a List or min/max items
        Type type;
        public Sym(Type type, String name, Map<Param, Object> params){
            type = type;
        }
    };

    private Object some(Object sym){
        //return new Sym(List, "", ..);//whats wrong with List?
        return sym;
    }

/*
private class optional
 */


    private class Sequence extends ArrayList<Object> {}; // can contain String, Sym,
    private class Choices extends ArrayList<Sequence> {};
    private class Grammar extends HashMap<Object, Choices> {};


    Grammar g;




    public Lang(){


    }




    public static void main(String[] args) {


    }


}