package org.jamesgames.digitalrain.jython;

import org.jamesgames.jamesjavautils.general.IntPair;
import org.jamesgames.jamesjavautils.general.ObserverSet;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.util.function.Function;

/**
 * Represents a Jython function that will produce an x and y coordinate render offset based on a y coordinate.
 *
 * @author James Murphy
 */
public class JythonOffsetFunction {

    public static final String functionSignature = "def computeOffsets(y):";

    private String functionSourceCode;
    private PyFunction computeOffsetsPythonFunction;

    private final String functionName;
    private final ObserverSet<JythonOffsetFunctionListener> listeners = new ObserverSet<>();
    private final PythonInterpreter pythonInterpreter = new PythonInterpreter();


    public JythonOffsetFunction(String functionName, String functionSourceCode) throws JythonFunctionSyntaxError {
        this.functionName = functionName;
        changeSourceCode(functionSourceCode);

    }

    public void changeSourceCode(String newFunctionSourceCode) throws JythonFunctionSyntaxError {
        this.functionSourceCode = newFunctionSourceCode;
        try {
            pythonInterpreter.exec(functionSourceCode);
            PyObject potentialFunctionDefined = pythonInterpreter.get("computeOffsets");
            if (potentialFunctionDefined == null) {
                throw new JythonFunctionSyntaxError("Expected to find a function called computeOffsets");
            }
            if (!(potentialFunctionDefined instanceof PyFunction)) {
                throw new JythonFunctionSyntaxError("Expected to find a function called computeOffsets, " +
                        "but found something else with that name.");
            }
            computeOffsetsPythonFunction = (PyFunction) potentialFunctionDefined;
        } catch (PyException e) {
            throw new JythonFunctionSyntaxError(e.toString());
        }
    }

    public Function<Integer, IntPair> createFunction() {
        return yCoordinate -> {
            try {
                // Call the function made
                PyObject potentialReturnTuple = computeOffsetsPythonFunction
                        .__call__(new PyInteger(yCoordinate));
                if (!(potentialReturnTuple instanceof PyTuple)) {
                    throw new IllegalStateException("Expected function to return a tuples, " +
                            "but it did not.");
                }
                PyTuple returnTuple = (PyTuple) potentialReturnTuple;
                if (returnTuple.size() != 2) {
                    throw new IllegalStateException("Expected function to return a tuple of size 2, " +
                            "but it did not.");
                }
                Object potentialXOffset = returnTuple.get(0);
                Object potentialYOffset = returnTuple.get(1);
                if (!(potentialXOffset instanceof Number)) {
                    throw new IllegalStateException(
                            "Expected function's first tuple return value (x offset) to be a number, " +
                                    "but it was not.");
                }
                if (!(potentialYOffset instanceof Number)) {
                    throw new IllegalStateException(
                            "Expected function's second tuple return value (y offset) to be a number, " +
                                    "but it was not.");
                }
                // Get the values made
                return new IntPair(((Number) returnTuple.get(0)).intValue(),
                        ((Number) returnTuple.get(1)).intValue());
            } catch (PyException e) {
                listeners.forEach(listener -> listener.offsetFunctionError(e.toString()));
                return new IntPair(0, 0);
            } catch (IllegalStateException e) {
                listeners.forEach(listener -> listener.offsetFunctionError(e.toString()));
                return new IntPair(0, 0);
            }
        };
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionSourceCode() {
        return functionSourceCode;
    }

    public void addJythonOffsetFunctionListener(JythonOffsetFunctionListener listener) {
        listeners.addObserver(listener);
    }

    public void removeJythonOffsetFunctionListener(JythonOffsetFunctionListener listener) {
        listeners.removeObserver(listener);
    }
}
