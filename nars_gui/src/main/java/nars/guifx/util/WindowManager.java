/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nars.guifx.util;

import javafx.event.EventHandler;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import nars.guifx.demo.NARide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controls a set of windows that belong to the application.
 * <p/>
 * Windows that are controlled by a WindowManager can be shown/hidden using a custom strategy ({@code WindowDisplayHandler})
 *
 * @author Andres Almiray
 * @see griffon.javafx.WindowDisplayHandler
 */
public final class WindowManager /*implements ShutdownHandler*/ {
    private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);
    //private final JavaFXGriffonApplication app;
    private final OnWindowHidingHelper onWindowHiding = new OnWindowHidingHelper();
    private final OnWindowShownHelper onWindowShown = new OnWindowShownHelper();
    private final OnWindowHiddenHelper onWindowHidden = new OnWindowHiddenHelper();
    private final Map<String, Window> windows = new ConcurrentHashMap<String, Window>();
    private final NARide app;

    /**
     * Creates a new WindowManager tied to an specific application.
     *
     * @param app an application
     */
    public WindowManager(NARide app) {
        this.app = app;
    }

    /**
     * Finds a Window by name.
     *
     * @param name the value of the name: property
     * @return a Window if a match is found, null otherwise.
     */
    public Window findWindow(String name) {
        //if (!GriffonNameUtils.isBlank(name)) {
            return windows.get(name);
        //}
        //return null;
    }

    public String findWindowName(Window window) {
        if (window != null) {
            for (Map.Entry<String, Window> entry : windows.entrySet()) {
                if (entry.getValue() == window) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Finds the Window that should be displayed during the Ready phase of an application.
     * <p/>
     * The WindowManager expects a configuration flag <code>javafx.windowManager.startingWindow</code> to be present in order to
     * determine which Window will be displayed during the Ready phase. If no configuration is found the WindowManmager will pick the
     * first Window found in the list of managed windows.
     * <p/>
     * The configuration flag accepts two value types:
     * <ul>
     * <li>a String that defines the name of the Window. You must make sure the Window has a matching name property.</li>
     * <li>a Number that defines the index of the Window in the list of managed windows.</li>
     * </ul>
     *
     * @return a Window that matches the given criteria or null if no match is found.
     */
    public Window getStartingWindow() {
        Window window = null;
        //Object value = ConfigUtils.getConfigValue(app.getConfig(), "javafx.windowManager.startingWindow");
        //if (LOG.isDebugEnabled()) {
            //LOG.debug("javafx.windowManager.startingWindow configured to " + value);
        //}
        //if (value == null || value instanceof ConfigObject) {
            if (windows.size() > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No startingWindow configured, selecting the first one in the list of windows");
                }
                window = windows.values().iterator().next();
            }
//        } else if (value instanceof String) {
//            String windowName = (String) value;
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Selecting window " + windowName + " as starting window");
//            }
//            window = findWindow(windowName);
//        } else if (value instanceof Number) {
//            int index = ((Number) value).intValue();
//            if (index >= 0 && index < windows.size()) {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("Selecting window at index " + index + " as starting window");
//                }
//                int i = 0;
//                for (Iterator<Window> iter = windows.values().iterator(); iter.hasNext(); i++) {
//                    if (i == index) {
//                        window = iter.next();
//                        break;
//                    }
//                }
//            }
//        }
//
//        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting window is " + window);
//        }

        return window;
    }

    /**
     * Returns the list of windows managed by this manager.
     *
     * @return a List of currently managed windows
     */
    public Collection<Window> getWindows() {
        return Collections.unmodifiableCollection(windows.values());
    }

    /**
     * Registers a window on this manager if an only if the window is not null
     * and it's not registered already.
     *
     * @param window the window to be added to the list of managed windows
     */
    public void attach(String name, Window window) {
        if (window == null || windows.values().contains(window)) {
            return;
        }
        window.setOnHiding(onWindowHiding);
        window.setOnShown(onWindowShown);
        window.setOnHidden(onWindowHidden);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attaching window with name: '" + name + ' ' + window);
        }
        windows.put(name, window);
    }

    /**
     * Removes the window from the list of manages windows if and only if it
     * is registered with this manager.
     *
     * @param window the window to be removed
     */
    public void detach(Window window) {
        if (window == null) {
            return;
        }
        if (windows.values().contains(window)) {
            window.setOnHiding(null);
            window.setOnShown(null);
            window.setOnHidden(null);
            String name = findWindowName(window);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detaching window with name: '" + name + ' ' + window);
            }
            windows.remove(name);
        }
    }

//    /**
//     * Shows the window.
//     * <p/>
//     * This method is executed <b>SYNCHRONOUSLY</b> in the UI thread.
//     *
//     * @param window the window to show
//     */
//    public void show(final Window window) {
//        if (window == null) {
//            return;
//        }
//
//        runLater(() -> {
//            app.resolveWindowDisplayHandler().show(window, app);
//
//        });
////        app.execInsideUISync(new Runnable() {
////            public void run() {
////                if (LOG.isDebugEnabled()) {
////                    LOG.debug("Showing window with name: '" + findWindowName(window) + " " + window);
////                }
////                app.resolveWindowDisplayHandler().show(window, app);
////            }
////        });
//    }

//    /**
//     * Hides the window.
//     * <p/>
//     * This method is executed <b>SYNCHRONOUSLY</b> in the UI thread.
//     *
//     * @param window the window to hide
//     */
    public void hide(final Window window) {
//        if (window == null) {
//            return;
//        }
//        app.execInsideUISync(new Runnable() {
//            public void run() {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("Hiding window with name: '" + findWindowName(window) + " " + window);
//                }
//                app.resolveWindowDisplayHandler().hide(window, app);
//            }
//        });
    }

//    public boolean canShutdown(GriffonApplication app) {
//        return true;
//    }

//    /**
//     * Hides all visible windows
//     */
//    public void onShutdown(GriffonApplication app) {
//        for (Window window : windows.values()) {
//            if (window.isShowing()) {
//                hide(window);
//            }
//        }
//    }

    public void handleClose(Window widget) {
//        if (app.getPhase() == ApplicationPhase.SHUTDOWN) {
//            return;
//        }
        int visibleWindows = 0;
        for (Window window : windows.values()) {
            if (window.isShowing()) {
                visibleWindows++;
            }
        }

//        Boolean autoShutdown = (Boolean) app.getConfig().flatten().get("application.autoShutdown");
//        if (visibleWindows <= 1 && autoShutdown != null && autoShutdown.booleanValue()) {
//            if (!app.shutdown())
//                show((Window) widget);
//        }
    }

    /**
     * WindowAdapter that invokes hide() when the window is about to be closed.
     *
     * @author Andres Almiray
     */
    private class OnWindowHidingHelper implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            hide((Window) event.getSource());
            handleClose((Window) event.getSource());
        }
    }

    /**
     * Listener that triggers application events when a window is shown.
     *
     * @author Andres Almiray
     */
    private class OnWindowShownHelper implements EventHandler<WindowEvent> {
        /**
         * Triggers a <tt>WindowShown</tt> event with the window as sole argument
         */
        @Override
        public void handle(WindowEvent windowEvent) {
            //app.event(GriffonApplication.Event.WINDOW_SHOWN.getName(), Arrays.asList(windowEvent.getSource()));
        }
    }

    /**
     * Listener that triggers application events when a window is hidden.
     *
     * @author Andres Almiray
     */
    private class OnWindowHiddenHelper implements EventHandler<WindowEvent> {
        /**
         * Triggers a <tt>WindowHidden</tt> event with the window as sole argument
         */
        @Override
        public void handle(WindowEvent windowEvent) {
            //app.event(GriffonApplication.Event.WINDOW_HIDDEN.getName(), Arrays.asList(windowEvent.getSource()));
        }
    }
}