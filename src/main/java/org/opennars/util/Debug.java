package org.opennars.util;

public class Debug {
    public static void debug(boolean enable, String name, String text) {
        if (!enable) {
            return;
        }
        System.out.println("[d] " + name + ": " + text);
    }

    public static void warning(boolean enable, String name, String text) {
        if (!enable) {
            return;
        }
    }

    // used for instrumentation
    // instrumentation is the tracking of internal information for the purpose of diagnose errors or tracing
    public static void instrumentate(boolean enable, String name, String text) {
        if (!enable) {
            return;
        }

        // add debug code here <===
    }
}
