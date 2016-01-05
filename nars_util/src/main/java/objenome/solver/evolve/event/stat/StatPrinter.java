package objenome.solver.evolve.event.stat;

///*
// * Copyright 2007-2013
// * Licensed under GNU Lesser General Public License
// * 
// * This file is part of EpochX
// * 
// * EpochX is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// * 
// * EpochX is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU Lesser General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License
// * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
// * 
// * The latest version is available from: http://www.epochx.org
// */
//package objenome.evolve.event.stat;
//
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import objenome.evolve.event.Event;
//import objenome.evolve.event.EventManager;
//import objenome.evolve.event.Listener;
//
///**
// * Utility class to output information of stat objects. The basic idea is
// * simple: create a new <code>StatPrinter</code> object, register the stats and
// * inform which event will trigger the output of the information.
// *
// * <p>
// * An example that outputs the generation number and the best generation fitness
// * is:
// * <pre>
// * StatPrinter printer = new StatPrinter();
// * printer.add(GenerationNumber.class);
// * printer.add(GenerationBestFitness.class);
// * printer.printOnEvent(EndGeneration.class);
// * </pre>
// * </p>
// * <p>
// * The above example will generate the following output:
// * <pre>
// * 1   12.0
// * 2   12.0
// * 3   11.0
// * 4   10.0
// * 5    9.0
// * </pre>
// * </p>
// */
//public class StatPrinter {
//
//    /**
//     * The default separator string.
//     */
//    public static final String SEPARATOR = "\t";
//
//    /**
//     * The list of <code>AbstractStat</code> to be printed.
//     */
//    private ArrayList<AbstractStat<?>> fields = new ArrayList<AbstractStat<?>>();
//
//    /**
//     * The mapping of listerners registered by this <code>StatPrinter</code>.
//     */
//    private Map<Class<?>, Listener<?>> listeners = new HashMap<Class<?>, Listener<?>>();
//
//    /**
//     * The current separator.
//     */
//    private String separator;
//
//    /**
//     * The output stream.
//     */
//    private PrintStream out;
//
//    /**
//     * Constructs a <code>StatPrinter</code>.
//     */
//    public StatPrinter() {
//        this(System.out, SEPARATOR);
//    }
//
//    /**
//     * Constructs a <code>StatPrinter</code>.
//     *
//     * @param out the output stream.
//     */
//    public StatPrinter(PrintStream out) {
//        this(out, SEPARATOR);
//    }
//
//    /**
//     * Constructs a <code>StatPrinter</code>.
//     *
//     * @param separator the delimiter string.
//     */
//    public StatPrinter(String separator) {
//        this(System.out, separator);
//    }
//
//    /**
//     * Constructs a <code>StatPrinter</code>.
//     *
//     * @param out the output stream.
//     * @param separator the separator string.
//     */
//    public StatPrinter(PrintStream out, String separator) {
//        this.out = out;
//        this.separator = separator;
//
//    }
//
//    /**
//     * Adds a new stat field to the printer.
//     *
//     * @param type the stat class to be added.
//     */
//    public <E extends Event> void add(Class<? extends AbstractStat<E>> type) {
//        AbstractStat.register(type);
//        
//        fields.add(getConfig().the(type));
//    }
//
//    /**
//     * Removes all fields from the printer.
//     */
//    public void clear() {
//        fields.clear();
//    }
//
//    /**
//     * Prints the fields to the printer's output stream.
//     */
//    public void print() {
//        if (!fields.isEmpty()) {
//            StringBuffer buffer = new StringBuffer();
//
//            for (AbstractStat<?> stat : fields) {
//                buffer.append(separator);
//                buffer.append(stat);
//            }
//
//            buffer.delete(0, separator.length());
//            out.println(buffer.toString());
//        }
//    }
//
//    /**
//     * Sets the event that triggers the output of the printer's fields.
//     *
//     * @param type the event class.
//     */
//    public <E extends Event> void printOnEvent(Class<E> type) {
//        // only creates a new listener if we do not have one already
//        if (!listeners.containsKey(type)) {
//            Listener<E> listener = new Listener<E>() {
//
//                @Override
//                public void onEvent(E event) {
//                    StatPrinter.this.print();
//                }
//            };
//
//            config.on(type, listener);
//            listeners.put(type, listener);
//        }
//    }
//
// }
