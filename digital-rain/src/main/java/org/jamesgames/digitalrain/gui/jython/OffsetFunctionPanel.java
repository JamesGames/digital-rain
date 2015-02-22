package org.jamesgames.digitalrain.gui.jython;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jamesgames.digitalrain.jython.JythonFunctionSyntaxError;
import org.jamesgames.digitalrain.jython.JythonOffsetFunction;
import org.jamesgames.jamesjavautils.gui.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;

/**
 * OffsetFunctionPanel is a Swing component that allows the user to select a certain {@link
 * org.jamesgames.digitalrain.jython.JythonOffsetFunction}.
 *
 * @author James Murphy
 */
public class OffsetFunctionPanel extends JPanel implements OffsetFunctionCreatorListener {

    private final static String noOffsetFunctionBody =
            JythonOffsetFunction.functionSignature + System.lineSeparator() + "\treturn (0, 0)";
    private final static String sineWaveFunctionBody =
            JythonOffsetFunction.functionSignature + System.lineSeparator() + "\tfrom math import sin" +
                    System.lineSeparator() + "\treturn (sin(y)*15, 0)";

    private final JComboBox<JythonOffsetFunction> functionSelector = new JComboBox<>();
    private final RSyntaxTextArea functionSourceCodeArea = new RSyntaxTextArea();
    private final JFrame errorLogFrame = new JFrame();
    private final OffsetFunctionCreatorPanel createOffsetFunctionPanel = new OffsetFunctionCreatorPanel();
    private final JFrame createFunctionFrame = new JFrame();
    private final OffsetFunctionErrorLogPanel errorLog = new OffsetFunctionErrorLogPanel();


    private JythonOffsetFunction selectedJythonFunction;


    public OffsetFunctionPanel() {
        setBorder(BorderFactory.createTitledBorder("Animation offset function (Python 2.7)"));

        // Make sure this listener is added before the selected item is set, otherwise this event will not
        // occur on the default setSelectedItem() (from first itemAdd or if setSelected is called here
        // in this constructor)
        functionSelector.addActionListener(e -> {
            selectedJythonFunction = functionSelector.getItemAt(functionSelector.getSelectedIndex());
            functionSourceCodeArea.setText(selectedJythonFunction.getFunctionSourceCode());
        });

        // One default function of no offsets
        try {
            selectedJythonFunction = new JythonOffsetFunction("None", noOffsetFunctionBody);
            selectedJythonFunction.addJythonOffsetFunctionListener(errorLog);
        } catch (JythonFunctionSyntaxError jythonFunctionSyntaxError) {
            // Should not happen, behavior is known with set data passed, function called is side effect free
            errorLog.appendError(jythonFunctionSyntaxError.getMessage(), true);
        }
        functionSelector.addItem(selectedJythonFunction);


        // Second default function of a sine wave
        try {
            JythonOffsetFunction sineWaveOffsetFunction = new JythonOffsetFunction("Sine Wave", sineWaveFunctionBody);
            functionSelector.addItem(sineWaveOffsetFunction);
            sineWaveOffsetFunction.addJythonOffsetFunctionListener(errorLog);
        } catch (JythonFunctionSyntaxError jythonFunctionSyntaxError) {
            // Should not happen, behavior is known with set data passed, function called is side effect free
            errorLog.appendError(jythonFunctionSyntaxError.getMessage(), true);
        }
        functionSelector.setMaximumRowCount(8);
        functionSelector.setRenderer(new ListCellRenderer<JythonOffsetFunction>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends JythonOffsetFunction> list,
                    JythonOffsetFunction value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(value.getFunctionName());
            }
        });

        functionSourceCodeArea.setText(selectedJythonFunction.getFunctionSourceCode());
        functionSourceCodeArea.setRows(4);
        functionSourceCodeArea.setFocusable(false);
        functionSourceCodeArea.setTabSize(4);
        functionSourceCodeArea.setEditable(false);
        functionSourceCodeArea.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        functionSourceCodeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);

        errorLogFrame.setTitle("Error log");
        errorLogFrame.add(errorLog);
        errorLogFrame.setLocationByPlatform(true);
        errorLogFrame.pack();

        createFunctionFrame.setTitle("Create an animation offset function in Python");
        createFunctionFrame.add(createOffsetFunctionPanel);
        createFunctionFrame.setLocationByPlatform(true);
        createFunctionFrame.pack();

        createOffsetFunctionPanel.addJythonCreatorListener(this);


        this.setLayout(new BorderLayout());
        JPanel functionSelectionPanel = new JPanel(new BorderLayout());
        functionSelectionPanel.add(new JLabel("Select offset function:"), BorderLayout.WEST);
        functionSelectionPanel.add(functionSelector, BorderLayout.CENTER);
        this.add(functionSelectionPanel, BorderLayout.NORTH);


        JPanel functionDisplayPanel = new JPanel(new BorderLayout());
        JScrollPane functionBodyScrollPane = new JScrollPane(functionSourceCodeArea);
        functionBodyScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        functionDisplayPanel.add(functionBodyScrollPane, BorderLayout.CENTER);
        this.add(functionDisplayPanel, BorderLayout.CENTER);


        JPanel createNewFunctionAndViewLogPanel = new JPanel(new BorderLayout());
        JButton createNewFunction = new JButton("Create New");
        createNewFunction.addActionListener(e -> {
            createOffsetFunctionPanel.setUpPanelToCreateNewFunction();
            errorLogFrame.setVisible(true);
            createFunctionFrame.setVisible(true);
        });

        JButton modifyFunction = new JButton("Modify");
        modifyFunction.addActionListener(e -> {
            createOffsetFunctionPanel.setUpPanelToModifyExistingFunction(selectedJythonFunction);
            errorLogFrame.setVisible(true);
            createFunctionFrame.setVisible(true);
        });

        JButton viewLog = new JButton("Error Log");
        viewLog.addActionListener(e -> errorLogFrame.setVisible(true));

        createNewFunctionAndViewLogPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.CENTER, true,
                createNewFunction, modifyFunction, viewLog), BorderLayout.NORTH);

        this.add(createNewFunctionAndViewLogPanel, BorderLayout.SOUTH);


    }

    public JythonOffsetFunction getSelectedJythonFunction() {
        return selectedJythonFunction;
    }

    @Override
    public void offsetFunctionToBeCreated(String name, String sourceCode) {
        try {
            boolean modifyFunction = selectedJythonFunction.getFunctionName().equals(name);
            if (modifyFunction) {
                selectedJythonFunction.changeSourceCode(sourceCode);
                functionSourceCodeArea.setText(selectedJythonFunction.getFunctionSourceCode());
            } else {
                JythonOffsetFunction newFunction = new JythonOffsetFunction(name, sourceCode);
                functionSelector.addItem(newFunction);
                newFunction.addJythonOffsetFunctionListener(errorLog);
            }
        } catch (JythonFunctionSyntaxError jythonFunctionSyntaxError) {
            errorLog.appendError(jythonFunctionSyntaxError.getMessage(), true);
        }
    }
}
