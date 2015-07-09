///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.io.in;
//
//import nars.task.Task;
//
//
///**
// * Wraps 1 pre-created Task as an Input
// */
//public class TaskInput implements Input {
//
//    public final Task task;
//    boolean finished = false;
//
//    public TaskInput(Task t) {
//        this.task = t;
//    }
//
//
//    @Override
//    public Task get()  {
//        if (!finished) {
//            finished = true;
//            return task;
//        }
//        return null;
//    }
//
//}
