package org.jamesgames.digitalrain.gui;

import org.jamesgames.digitalrain.rain.RainPaneSprite;
import org.jamesgames.easysprite.Sprite;
import org.jamesgames.easysprite.gui.swing.SpritePanel;
import org.jamesgames.easysprite.updater.SpriteUpdater;
import org.jamesgames.jamesjavautils.gui.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/**
 * RainPanel is the main user interface component in the digital rain application. It displays a {@link
 * org.jamesgames.easysprite.gui.swing.SpritePanel} with a {@link org.jamesgames.digitalrain.rain.RainPaneSprite} and
 * also displays a {@link org.jamesgames.digitalrain.gui.RainPaneCreatorPanel} to create RainPaneSprites for the
 * SpritePanel. RainPanel is also a ContainerListener, which if added as a listener to it's parent container will stop
 * or continue updating on removal and adding.
 *
 * @author James Murphy
 */
public class RainPanel extends JPanel implements RainPaneCreatorListener, RainPaneControlListener, ContainerListener {

    private static final int spriteUpdatePaceInMilliseconds = 16;

    /**
     * Last created RainPaneSprite. Reference is kept track of so one can remove it from the rootSprite when a new
     * RainPaneSprite is created.
     */
    private Sprite lastCreatedRainPaneSprite = new Sprite() {
        @Override
        protected void drawUnderChildren(Graphics2D g) {
            Graphics tempGraphics = g.create();
            tempGraphics.setColor(Color.WHITE);
            tempGraphics.drawString("Please use the controls to create a new digital rain scene.", 5,
                    g.getFontMetrics().getHeight() + 50);
            tempGraphics.dispose();
        }
    };

    /**
     * Root sprite to hold a {@link org.jamesgames.digitalrain.rain.RainPaneSprite}. Having a root sprite allows the use
     * of one SpriteUpdater to update any sprite added to this root sprite, as RainPaneSprites will be removed and added
     * as the user creates them.
     */
    private final Sprite rootSprite = new Sprite() {
        /**
         * The root sprite will be auto resized to SpritePanel's size, but lastCreatedRainPaneSprite will not,
         * so on resize of the root Sprite, also resize lastCreatedRainPaneSprite.
         */
        @Override
        protected void onResize(int newWidth, int newHeight, int oldWidth, int oldHeight) {
            lastCreatedRainPaneSprite.resize(newWidth, newHeight);
        }
    };

    /**
     * SpritePanel that displays the latest created RainPaneSprite.
     */
    private final SpritePanel spritePanel = new SpritePanel(rootSprite, true);

    /**
     * RainPaneCreatorPanel used to create a new {@link org.jamesgames.digitalrain.rain.RainPaneSprite}.
     */
    private final RainPaneCreatorPanel rainPaneCreator = new RainPaneCreatorPanel();

    /**
     * RainPaneControlPanel used to control a {@link org.jamesgames.digitalrain.rain.RainPaneSprite}
     */
    private final RainPaneControlPanel rainPaneController = new RainPaneControlPanel();

    /**
     * Holds the reference to the {@link org.jamesgames.easysprite.updater.SpriteUpdater} used to update the root
     * sprite.
     */
    private final SpriteUpdater spriteUpdater = new SpriteUpdater(rootSprite, spriteUpdatePaceInMilliseconds);

    /**
     * Holds reference to the split pane, used to set the divider location at a later time when the panel has a size
     * (after it's visible)
     */
    private final JSplitPane splitPane;

    /**
     * Holds the reference to the Frame that contains this RainPanel. Use it to provide controls to set the Frame full
     * screen.
     */
    private final Frame frameContainingRainPanel;

    /**
     * True if some of the one time actions that need to be done have been done, false otherwise. (Actions like setting
     * the split pane divider after it has a size set.
     */
    private boolean postComponentVisibleActionsComplete = false;

    private int defaultWindowWidth;
    private int defaultWindowHeight;

    public RainPanel(Frame frame) {
        frameContainingRainPanel = frame;
        this.setLayout(new BorderLayout());
        spritePanel.setBackground(Color.BLACK);
        spriteUpdater.addUpdateListener(spritePanel);
        spriteUpdater.addAdditionalActionPerUpdate(elapsedTimeInMilliseconds -> spritePanel.repaint());
        rootSprite.addChildSprite(lastCreatedRainPaneSprite);
        rainPaneCreator.addRainPaneCreatorListener(this);
        rainPaneController.addRainPaneControlListener(this);

        // Laying out components
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(rainPaneCreator, BorderLayout.NORTH);
        controlsPanel.add(rainPaneController, BorderLayout.CENTER);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                spritePanel, controlsPanel);
        this.add(splitPane, BorderLayout.CENTER);

        splitPane.setOneTouchExpandable(true);
    }

    @Override
    public void rainPaneSpriteCreated(RainPaneSprite sprite) {
        SwingUtilities.invokeLater(() -> {
            rootSprite.removeChildSprite(lastCreatedRainPaneSprite);
            lastCreatedRainPaneSprite = sprite;
            lastCreatedRainPaneSprite.resize(rootSprite.getWidth(), rootSprite.getHeight());
            rootSprite.addChildSprite(lastCreatedRainPaneSprite);
            // Reset the controller's component's to default values as well, as their
            // old values corresponded to the latest values of the last RainPaneSprite
            rainPaneController.resetPanelComponentsToDefault();
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // This is a sort of workaround that lets us reposition the split pane divider to the specific location we want.
        // This can't be done during construction because components have not yet been laid out and set visible so their
        // widths/heights can't be queried, but we do know that at the first time they are rendered that they now have
        // their widths and heights.
        if (!postComponentVisibleActionsComplete && this.isVisible()) {
            postComponentVisibleActionsComplete = true;
            repositionSplitPaneToFarRight();
            defaultWindowWidth = frameContainingRainPanel.getWidth();
            defaultWindowHeight = frameContainingRainPanel.getHeight();
        }
    }

    private void repositionSplitPaneToFarRight() {
        SwingUtilities.invokeLater(
                () -> splitPane.setDividerLocation(RainPanel.this.getWidth() -
                        rainPaneCreator.getMinimumSize().width - splitPane.getDividerSize()));
    }

    @Override
    public void toggleDrawingOFDebugGraphics() {
        lastCreatedRainPaneSprite.toggleDrawingDebugGraphicsIncludingChildSprites();
    }

    @Override
    public void setRainDropYVelocityInUnitsPerMillisecond(float yVelocityUnitsInMilliseconds) {
        if (lastCreatedRainPaneSprite instanceof RainPaneSprite) {
            ((RainPaneSprite) lastCreatedRainPaneSprite).changeRainLineYVelocities(yVelocityUnitsInMilliseconds);
        }
    }

    @Override
    public void toggleDrawingOfFPSUPS() {
        spritePanel.toggleSetDisplayingTimeValues();
    }

    @Override
    public void setWindowFullScreenOverAllDisplays() {
        SwingHelper.expandWindowAcrossAllDisplays(frameContainingRainPanel);
        repositionSplitPaneToFarRight();
    }

    @Override
    public void restoreWindowSize() {
        frameContainingRainPanel.setSize(defaultWindowWidth, defaultWindowHeight);
        SwingHelper.centerWindow(frameContainingRainPanel);
        repositionSplitPaneToFarRight();
    }

    @Override
    public void componentAdded(ContainerEvent e) {
        if (e.getChild() == this) {
            spriteUpdater.scheduleSpriteUpdate(spriteUpdatePaceInMilliseconds);
        }
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        if (e.getChild() == this) {
            spriteUpdater.stopUpdating();
        }
    }
}
