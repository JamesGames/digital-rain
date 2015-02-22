package org.jamesgames.digitalrain.gui.jython;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jamesgames.digitalrain.jython.JythonOffsetFunction;
import org.jamesgames.jamesjavautils.general.ObserverSet;
import org.jamesgames.jamesjavautils.gui.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;

/**
 * OffsetFunctionCreatorPanel is a Swing {@link javax.swing.JComponent} that can create JythonOffsetFunctions
 *
 * @author James Murphy
 */
class OffsetFunctionCreatorPanel extends JPanel {

    private static final String defaultSourceCode = JythonOffsetFunction.functionSignature + System.lineSeparator() +
            "\t# return a tuple of x,y offsets" + System.lineSeparator() + "\treturn (0,0)";
    private static final String defaultFunctionName = "Some Function";

    private final RSyntaxTextArea functionSourceCode = new RSyntaxTextArea(30, 110);
    private final JTextField nameOfFunction = new JTextField();
    private final ObserverSet<OffsetFunctionCreatorListener> listeners = new ObserverSet<>();

    public OffsetFunctionCreatorPanel() {
        this.setLayout(new BorderLayout());

        this.add(new JLabel("Code up a function matching the definition of " + JythonOffsetFunction.functionSignature +
                "that returns a tuple of a x and y coordinate"), BorderLayout.NORTH);
        functionSourceCode.setText(JythonOffsetFunction.functionSignature + System.lineSeparator() +
                "\t# return a tuple of x,y offsets" + System.lineSeparator() + "\treturn (0,0)");

        functionSourceCode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        functionSourceCode.setTabSize(4);
        functionSourceCode.setCodeFoldingEnabled(true);
        functionSourceCode.setText(defaultSourceCode);
        nameOfFunction.setColumns(12);
        RTextScrollPane sp = new RTextScrollPane(functionSourceCode);
        this.add(sp, BorderLayout.CENTER);

        JButton createFunction = new JButton("Create/Modify animation offset function");
        createFunction.addActionListener(e -> listeners
                .forEach(listener -> listener
                        .offsetFunctionToBeCreated(nameOfFunction.getText(), functionSourceCode.getText())));

        nameOfFunction.setText(defaultFunctionName);
        this.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT, false,
                new JLabel("Set the name of the function (a new name creates a new function):"), nameOfFunction,
                createFunction), BorderLayout.SOUTH);
    }

    public void setUpPanelToCreateNewFunction() {
        functionSourceCode.setText(defaultSourceCode);
        nameOfFunction.setText(defaultFunctionName);
    }

    public void setUpPanelToModifyExistingFunction(JythonOffsetFunction function) {
        functionSourceCode.setText(function.getFunctionSourceCode());
        nameOfFunction.setText(function.getFunctionName());
    }

    public void addJythonCreatorListener(OffsetFunctionCreatorListener listener) {
        listeners.addObserver(listener);
    }

    public void removeJythonCreatorListener(OffsetFunctionCreatorListener listener) {
        listeners.removeObserver(listener);
    }
}