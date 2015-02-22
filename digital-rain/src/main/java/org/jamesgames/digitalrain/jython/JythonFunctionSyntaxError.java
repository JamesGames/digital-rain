package org.jamesgames.digitalrain.jython;

/**
 * JythonFunctionSyntaxError is an exception that represents an error message regarding the syntax of the source code used to build Jython objects.
 *
 * @author James Murphy
 */
public class JythonFunctionSyntaxError extends Exception {
    public JythonFunctionSyntaxError(String message) {
        super(message);
    }
}
