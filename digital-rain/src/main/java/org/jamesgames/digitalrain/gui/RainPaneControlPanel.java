package org.jamesgames.digitalrain.gui;

import org.jamesgames.digitalrain.rain.RainPaneSprite;
import org.jamesgames.jamesjavautils.general.ObserverSet;
import org.jamesgames.jamesjavautils.gui.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * RainPaneControlPanel is a {@link javax.swing.JPanel} that can create events to control a {@link
 * org.jamesgames.digitalrain.rain.RainPaneSprite}.
 *
 * @author James Murphy
 */
class RainPaneControlPanel extends JPanel {
    // Speeds are units per millisecond times 100, so 20 would represent 0.20
    private final static int maxRainSpeed = 100;
    private final static int minRainSpeed = 0;
    private final static int defaultRainSpeed = Math.round(RainPaneSprite.defaultYVelocityUnitsPerMillisecond * 100);

    private final ObserverSet<RainPaneControlListener> listeners = new ObserverSet<>();

    private final JSlider rainSpeed = new JSlider(JSlider.VERTICAL,
            minRainSpeed, maxRainSpeed, defaultRainSpeed);

    public RainPaneControlPanel() {
        this.setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Control current rain scene"));

        rainSpeed.setMajorTickSpacing(10);
        rainSpeed.setMinorTickSpacing(2);
        rainSpeed.setPaintTicks(true);
        rainSpeed.setPaintLabels(true);
        Hashtable labelTable = new Hashtable();
        labelTable.put(minRainSpeed, new JLabel("Stopped"));
        labelTable.put(maxRainSpeed, new JLabel("Fast"));
        rainSpeed.setLabelTable(labelTable);
        rainSpeed.addChangeListener(e -> {
            for (RainPaneControlListener listener : listeners)
                // Convert the integer speed to units per millisecond
                listener.setRainDropYVelocityInUnitsPerMillisecond(rainSpeed.getValue() / 100.0f);
        });
        JPanel speedPanel = new JPanel(new BorderLayout());
        speedPanel.add(new JLabel("Set rain speed"), BorderLayout.NORTH);
        speedPanel.add(rainSpeed, BorderLayout.CENTER);
        this.add(speedPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton displayUpdateSpeeds = new JButton("Toggle display of FPS/UPS");
        displayUpdateSpeeds.addActionListener(e -> {
            for (RainPaneControlListener listener : listeners)
                listener.toggleDrawingOfFPSUPS();
        });
        buttonPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT, false, displayUpdateSpeeds));
        JButton displayDebugGraphics = new JButton("Toggle display of debug graphics");
        displayDebugGraphics.addActionListener(e -> {
            for (RainPaneControlListener listener : listeners)
                listener.toggleDrawingOFDebugGraphics();
        });
        buttonPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT, false, displayDebugGraphics));
        JButton fullScreen = new JButton("Full Screen");
        fullScreen.addActionListener(e -> {
            for (RainPaneControlListener listener : listeners)
                listener.setWindowFullScreenOverAllDisplays();
        });
        JButton restoreScreen = new JButton("Restore");
        restoreScreen.addActionListener(e -> {
            for (RainPaneControlListener listener : listeners)
                listener.restoreWindowSize();
        });
        buttonPanel.add(SwingHelper.putComponentsInFlowLayoutPanel(FlowLayout.LEFT, false, fullScreen, restoreScreen));
        this.add(buttonPanel, BorderLayout.CENTER);
    }

    public void resetPanelComponentsToDefault() {
        rainSpeed.setValue(defaultRainSpeed);
    }

    public void addRainPaneControlListener(RainPaneControlListener listener) {
        listeners.addObserver(listener);
    }

    public void removeRainPaneControlLListener(RainPaneControlListener listener) {
        listeners.removeObserver(listener);
    }
}
