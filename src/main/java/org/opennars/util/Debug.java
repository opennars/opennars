package org.opennars.util;

public class Debug {
    public static void debug(boolean enable, String name, String text) {
        if (!enable) {
            return;
        }
        System.out.println("[d] " + name + ": " + text);
    }
}
