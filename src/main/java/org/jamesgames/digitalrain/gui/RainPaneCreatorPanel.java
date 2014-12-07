package org.jamesgames.digitalrain.gui;

import org.jamesgames.digitalrain.rain.RainPaneSprite;
import org.jamesgames.jamesjavautils.general.ObserverSet;
import org.jamesgames.jamesjavautils.gui.swing.ColorSetChooser;
import org.jamesgames.jamesjavautils.gui.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * RainPaneCreatorPanel is a {@link javax.swing.JPanel} that has various options to create {@link
 * org.jamesgames.digitalrain.rain.RainPaneSprite}s.
 *
 * @author James Murphy
 */
class RainPaneCreatorPanel extends JPanel {
    private final ObserverSet<RainPaneCreatorListener> listeners = new ObserverSet<>();
    private final JComboBox<String> fontComboBox =
            new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private final JCheckBox useDefaultMonospacedFont = new JCheckBox("Use default monospaced font");
    private final JComboBox<String> fontStyles =
            new JComboBox<>(new String[]{"Plain", "Bold", "Italic", "Bold and Italic"});
    private final SpinnerNumberModel validFontSizes = new SpinnerNumberModel(
            new Integer(16), // default value
            new Integer(6), // min value
            new Integer(72), // max value
            new Integer(1) // step, value from one to next
    );
    private final JSpinner fontSizeSpinner = new JSpinner(validFontSizes);
    private final ColorSetChooser colorList = new ColorSetChooser("Digital Rain colors", 100, 100);
    private final JLabel warningLabel = new JLabel();

    public RainPaneCreatorPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Setup or recreate new scene"));
        useDefaultMonospacedFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontComboBox.setEnabled(!useDefaultMonospacedFont.isSelected());
            }
        });
        boolean isUsingDefaultMonospacedFontAtStart = true;
        useDefaultMonospacedFont.setSelected(true);
        fontComboBox.setEnabled(!isUsingDefaultMonospacedFontAtStart);


        // Laying out of components
        JPanel topPanel = new JPanel(new BorderLayout());
        // Top of top
        JPanel topOfTopPanel = new JPanel();
        topOfTopPanel.setLayout(new BoxLayout(topOfTopPanel, BoxLayout.Y_AXIS));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("Rain Font: "), fontComboBox));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("Rain Font: "), useDefaultMonospacedFont));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("Font Style:"), fontStyles));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("Font Size: "), fontSizeSpinner));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("Note, not all fonts may support the characters used")));
        topOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT,
                false, new JLabel("In addition, try to pick a monospaced font")));
        topPanel.add(topOfTopPanel, BorderLayout.NORTH);
        // Center of top
        topPanel.add(colorList, BorderLayout.CENTER);
        // Bottom of top
        JPanel bottomOfTopPanel = new JPanel();
        bottomOfTopPanel.setLayout(new BoxLayout(bottomOfTopPanel, BoxLayout.Y_AXIS));
        JButton createNewRainPaneSprite = new JButton("Create Digital Rain Scene");
        bottomOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.CENTER, false,
                createNewRainPaneSprite), BorderLayout.SOUTH);
        bottomOfTopPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.CENTER, false, warningLabel),
                BorderLayout.SOUTH);
        createNewRainPaneSprite.addActionListener(e -> createNewRainPaneSprite());
        warningLabel.setForeground(Color.RED);
        topPanel.add(bottomOfTopPanel, BorderLayout.SOUTH);
        this.add(topPanel, BorderLayout.NORTH);
    }

    private void createNewRainPaneSprite() {
        Set<Color> colorsSelected = colorList.getColors();
        if (colorsSelected.isEmpty()) {
            warningLabel.setText("Must supply at least one color");
        } else {
            warningLabel.setText("");
            int fontStyle = getFontStyle();
            RainPaneSprite rainPaneSprite = useDefaultMonospacedFont.isSelected() ?
                    new RainPaneSprite(colorsSelected, fontStyle, (Integer) fontSizeSpinner.getModel().getValue())
                    :
                    new RainPaneSprite(colorsSelected, new Font(fontComboBox.getItemAt(fontComboBox.getSelectedIndex()),
                            fontStyle, (Integer) fontSizeSpinner.getModel().getValue()));
            for (RainPaneCreatorListener listener : listeners) {
                listener.rainPaneSpriteCreated(rainPaneSprite);
            }
        }
    }

    private int getFontStyle() {
        switch (fontStyles.getItemAt(fontStyles.getSelectedIndex())) {
            case "Plain":
                return Font.PLAIN;
            case "Bold":
                return Font.BOLD;
            case "Italic":
                return Font.ITALIC;
            case "Bold and Italic":
                return Font.BOLD + Font.ITALIC;
            default:
                throw new IllegalStateException("Unknown font style chosen");
        }
    }


    public void addRainPaneCreatorListener(RainPaneCreatorListener listener) {
        listeners.addObserver(listener);
    }

    public void removeRainPaneCreatorListener(RainPaneCreatorListener listener) {
        listeners.removeObserver(listener);
    }
}
