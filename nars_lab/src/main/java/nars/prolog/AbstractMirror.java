///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package nars.prolog;
//
//import nars.NAR;
//import nars.budget.Budget;
//import nars.util.event.NARReaction;
//
//import nars.task.DefaultTask;
//import nars.task.Sentence;
//import nars.task.Task;
//
///**
// * Interface with which to implement a "mirror" - a mental prosthetic which
// * reflects NAR activity into an enhanced or accelerated representation.
// * Usually these violate NARS theory and principles as the expense of
// * improved performance.  However these can be uesd for comparing results.
// *
// */
//abstract public class AbstractMirror extends NARReaction {
//
//    private final NAR nar;
//
//    public AbstractMirror(NAR n, boolean active, Class... events) {
//        super(n, active, events);
//        this.nar = n;
//    }
//
//    public static enum InputMode {
//        /** normal input, ie. nar.addInput */
//        Perceive,
//
//        /** bypass input buffers, directly as a new memory Task, ie. memory.addTask */
//        InputTask,
//
//        /** instance an ImmediateProcess and run it immediately */
//        ImmediateProcess,
//
//        /** insert the sentence directly into a concept, attempt to create the concept if one does not exist */
//        Concept
//    }
//
//    public boolean input(Sentence s, InputMode mode, Task parent) {
//        if (mode == InputMode.Perceive) {
//            throw new RuntimeException("not updated yet");
//            //nar.input(s);
//            //return true;
//        }
//        else if ((mode == InputMode.InputTask)|| (mode == InputMode.ImmediateProcess)) {
//
//            Task t = new DefaultTask(s, Budget.newDefault(s, nar.memory), parent, null );
//
//            //System.err.println("  " + t);
//
//            if (mode == InputMode.InputTask)
//                nar.memory.input(t);
//            else if (mode == InputMode.ImmediateProcess)
//                TaskProcess.run(nar.memory, t);
//
//            return true;
//
//        }
//        else if (mode == InputMode.Concept) {
//            throw new RuntimeException("unimpl yet");
//        }
//        return false;
//    }
// }
