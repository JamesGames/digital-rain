package org.jamesgames.digitalrain.main;

import org.jamesgames.digitalrain.gui.RainPanel;
import org.jamesgames.jamesjavautils.gui.swing.JFrameSizedAfterInsets;

import javax.swing.*;
import java.awt.*;

/**
 * Contains the main method that displays a JFrame with digital rain animation and related controls.
 *
 * @author James Murphy
 */
public class Main {

    private static final String version = "1.0";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Color base = new Color(0, 130, 0);
            UIManager.put("nimbusBase", base); // "nimbusBlueGrey" and "control" form two other overall colors in Nimbus
            for (UIManager.LookAndFeelInfo uiInfo : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(uiInfo.getName())) {
                    try {
                        UIManager.setLookAndFeel(uiInfo.getClassName());
                    } catch (Exception e) {
                        // Do nothing, continue with default look and feel
                    }
                }
            }

            JFrame frame = new JFrameSizedAfterInsets(1200, 800, true);
            frame.setTitle("Digital Rain (v" + version +
                    ") | Created by James Murphy | JamesGames.org | JamesGames.org@gmail.com");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(new RainPanel(frame));

            frame.setVisible(true);
        });

    }
}
