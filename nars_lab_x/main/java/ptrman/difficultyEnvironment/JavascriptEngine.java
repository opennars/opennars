package ptrman.difficultyEnvironment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 *
 */
public class JavascriptEngine {
    public JavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public void addBinding(String name, Object value) {
        bindings.put(name, value);
    }

    public void resetBindings() {
        bindings = new SimpleBindings();
    }

    public void loadFile(String filename) {
        try {
            engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            engine.eval(new FileReader(filename) /* "function loaded() { print('Hello, '); }"*/);
        } catch (ScriptException e) {
            throw new RuntimeException("JavascriptEngine.loadFile(): ScriptException: " + e.getMessage());
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("JavascriptEngine.loadFile(): FileNotFoundException");
        }
    }

    public void loadString(String script) {
        try {
            engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            engine.eval(script);
        } catch (ScriptException e) {
            throw new RuntimeException("JavascriptEngine.loadString(): ScriptException: " + e.getMessage());
        }
    }

    public Object invokeFunction(String name, List<Object> arguments) {
        Invocable invocable = (Invocable)engine;

        try {
            if( arguments.size() == 0 ) {
                return invocable.invokeFunction(name);
            }
            else if( arguments.size() == 1 ) {
                return invocable.invokeFunction(name, arguments.get(0));
            }
            else if( arguments.size() == 2 ) {
                return invocable.invokeFunction(name, arguments.get(0), arguments.get(1));
            }
            else if( arguments.size() == 3 ) {
                return invocable.invokeFunction(name, arguments.get(0), arguments.get(1), arguments.get(2));
            }
            else if( arguments.size() == 4 ) {
                return invocable.invokeFunction(name, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3));
            }
            else if( arguments.size() == 5 ) {
                return invocable.invokeFunction(name, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4));
            }
            else {
                throw new RuntimeException("JavascriptEngine.invokeFunction(): Too many arguments");
            }
        } catch (ScriptException e) {
            throw new RuntimeException("JavascriptEngine.invokeFunction(): ScriptException: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("JavascriptEngine.invokeFunction(): NoSuchMethodException: " + e.getMessage());
        }
    }

    private static Object[] translateListOfVariadicToObjectArray(List<Object> list) {
        Object[] result = new Object[list.size()];

        for( int i = 0; i < list.size(); i++ ) {
            result[i] = list.get(i);
        }

        return result;
    }

    private ScriptEngine engine;
    private Bindings bindings = new SimpleBindings();
}
