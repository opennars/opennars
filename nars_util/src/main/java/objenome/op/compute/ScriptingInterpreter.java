/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.op.compute;

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.source.SourceGenerator;
import objenome.util.Utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ScriptingInterpreter provides a generic interpreter for any language
 * which supports the javax.scripting API (JSR 223). Two constructors are
 * provided, one which receives the name of an installed scripting engine or one
 * of its aliases, for example "ruby", "jruby" and "javascript" would be valid
 * engine names. The second constructor receives a script engine directly which
 * makes it possible to easily plug in support for new languages by implementing
 * a ScriptEngine (or otherwise obtaining an implementation).
 *
 * <p>
 * It is often the case that performance can improved by handling evaluation and
 * execution in a language specific way, so subclasses of this class may be
 * available for some languages. Where available these language specific classes
 * should be used in preference to this general class.
 *
 * <p>
 * The javax.scripting API was added to Java in version 1.6 and as such this
 * class and any subclasses require a 1.6 compatible JRE.
 *
 * @see RubyInterpreter
 * @see GroovyInterpreter
 *
 * @since 2.0
 */
public class ScriptingInterpreter<I,T extends Organism,O> implements Computer<I,T,O> {

    // The language specific scripting engine.
    private final ScriptEngine engine;

    private SourceGenerator<T> generator;

    /**
     * Constructs a <code>ScriptingInterpreter</code> for a named scripting
     * engine. A list of installed ScriptEngine names can be obtained with the
     * following code:
     *
     * <blockquote><code>
     * ScriptEngineManager mgr = new ScriptEngineManager();
     * List<ScriptEngineFactory> factories = mgr.getEngineFactories();
     * List<String> engineNames = new ArrayList<String>(); for
     * (ScriptEngineFactory factory: factories) {
     * engineNames.addAll(factory.getNames()); }
     * </code></blockquote>
     *
     * @param generator the SourceGenerator to use to convert individuals to
     * source code
     * @param engineName the name of the scripting engine to use
     */
    public ScriptingInterpreter(SourceGenerator<T> generator, String engineName) {
        this.generator = generator;
        ScriptEngineManager manager = new ScriptEngineManager();

        engine = manager.getEngineByName(engineName);

        if (engine == null) {
            throw new IllegalArgumentException("no engine matching alias " + engineName);
        }
    }

    /**
     * Constructs a <code>ScriptingInterpreter</code> for the given
     * <code>ScriptEngine</code>.
     *
     * @param generator the SourceGenerator to use to convert individuals to
     * source code
     * @param engine the scripting engine to use
     */
    public ScriptingInterpreter(SourceGenerator<T> generator, ScriptEngine engine) {
        this.generator = generator;
        this.engine = engine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O[] eval(T program, String[] argNames, I[][] argValues) throws MalformedProgramException {
        int noParamSets = argValues.length;
        int noParams = argNames.length;

        //final O[] results = new O[noParamSets];
        List<O> results = new ArrayList(noParamSets);
        
        String expression = generator.getSource(program);

        // Evaluate each argument set.
        for (int i = 0; i < noParamSets; i++) {

            Object[] paramSet = argValues[i];

            try {
                //noinspection LoopConditionNotUpdatedInsideLoop,LoopConditionNotUpdatedInsideLoop
                for (int j = 0; j < noParams; i++) {
                    engine.put(argNames[j], paramSet[j]);
                }
                results.set(i, (O)engine.eval(expression));
            } catch (ScriptException e) {
                throw new MalformedProgramException();
            }
        }

        return Utils.toArray(results);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(T program, String[] argNames, I[][] argValues)
            throws MalformedProgramException {
        eval(program, argNames, argValues);
    }

    /**
     * Returns the scripting engine performing the evaluation and execution.
     *
     * @return the script engine
     */
    public ScriptEngine getEngine() {
        return engine;
    }

    /**
     * Returns the source generator being used to convert individuals to source
     * code.
     *
     * @return the current source generator
     */
    public SourceGenerator<T> getSourceGenerator() {
        return generator;
    }

    /**
     * Sets the source generator to use to convert individuals to source code
     *
     * @param the source generator to set
     */
    public void setSourceGenerator(SourceGenerator<T> generator) {
        this.generator = generator;
    }
}
