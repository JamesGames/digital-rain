package org.jamesgames.digitalrain.gui.jython;

/**
 * OffsetFunctionCreatorListener is an interface that defines the event of the attempted creation of a {@link
 * org.jamesgames.digitalrain.jython.JythonOffsetFunction}.
 *
 * @author James Murphy
 */
interface OffsetFunctionCreatorListener {
    public void offsetFunctionToBeCreated(String name, String sourceCode);
}
