package org.jamesgames.digitalrain.gui.jython;

import org.jamesgames.digitalrain.jython.JythonOffsetFunctionListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * OffsetFunctionErrorLogPanel is a Swing component that represents a log of data regarding the execution and creation
 * of {@link org.jamesgames.digitalrain.jython.JythonOffsetFunction}s.
 *
 * @author James Murphy
 */
class OffsetFunctionErrorLogPanel extends JPanel implements JythonOffsetFunctionListener {

    private static final int maxNumberOfTimesUniqueErrorMessageIsLogged = 3;

    private final JTextArea log = new JTextArea();
    // This HashMap counts how often a certain error message is passed, after a while
    // the error message is no longer printed to the log
    private final HashMap<String, Integer> errorMessages = new HashMap<>();

    public OffsetFunctionErrorLogPanel() {
        this.setLayout(new BorderLayout());
        this.add(new JLabel("Log of executing and initialization of a selected function"), BorderLayout.NORTH);

        log.setRows(2);
        log.setFont(Font.getFont(Font.MONOSPACED));
        log.setTabSize(4);
        log.setOpaque(false);
        log.setEditable(false);
        log.setWrapStyleWord(true);
        // Work around for Nimbus not correctly using opaque status
        log.setBackground(new Color(0, 0, 0, 0));

        JScrollPane logScrollPane = new JScrollPane(log);
        logScrollPane.setPreferredSize(new Dimension(500, 250));

        this.add(logScrollPane, BorderLayout.NORTH);
    }

    /**
     * Unused but should be used. Improvement is to have some sort of event that is observed here in this class or in
     * {@link org.jamesgames.digitalrain.gui.jython.OffsetFunctionPanel} that knows when a new rain scene is created.
     * That way you can have a new log of errors per running rain scene. Perhaps though that there should be two errors
     * logs, runtime error log and a function creation error log.
     */
    public void clearLog() {
        log.setText("");
        errorMessages.clear();
    }

    @Override
    public void offsetFunctionError(String message) {
        appendError(message, false);
    }

    public void appendError(String error, boolean logErrorAlways) {
        String errorToWrite = "Error: " + error + System.lineSeparator();

        if (!logErrorAlways) {
            Integer numberOfTimesErrorAppeared = errorMessages.putIfAbsent(errorToWrite, 0);
            if (numberOfTimesErrorAppeared == null) {
                numberOfTimesErrorAppeared = 0;
            }
            if (numberOfTimesErrorAppeared >= maxNumberOfTimesUniqueErrorMessageIsLogged) {
                return; // Don't add anything to the log
            }
            if (numberOfTimesErrorAppeared == maxNumberOfTimesUniqueErrorMessageIsLogged - 1) {
                log.append(" - Last time the following error message will appear, as it has appeared too often:" +
                        System.lineSeparator());
            }
            errorMessages.put(errorToWrite, numberOfTimesErrorAppeared + 1);
        }
        log.append(errorToWrite);
    }
}
