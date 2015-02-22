package org.jamesgames.digitalrain.jython;

/**
 * JythonOffsetFunctionListener is an interface that defines an event from when a {@link JythonOffsetFunction} has an
 * issue running (like an exception).
 *
 * @author James Murphy
 */
public interface JythonOffsetFunctionListener {
    public void offsetFunctionError(String message);
}
