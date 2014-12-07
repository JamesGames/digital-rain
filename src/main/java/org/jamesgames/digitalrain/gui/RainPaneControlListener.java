package org.jamesgames.digitalrain.gui;

/**
 * RainPaneControlListener defines events that can come from a {@link RainPaneControlPanel}.
 *
 * @author James Murphy
 */
interface RainPaneControlListener {
    /**
     * When called with true, expects the potential associated RainPaneSprite's debug graphics to be drawn
     */
    public void toggleDrawingOFDebugGraphics();

    /**
     * Set's the potential associated RainPaneSprite's y velocity to the passed speed.
     */
    public void setRainDropYVelocityInUnitsPerMillisecond(float yVelocityUnitsInMilliseconds);

    /**
     * When called with true, expects the timing stats to be drawn to the screen.
     */
    public void toggleDrawingOfFPSUPS();

    /**
     * Make the window full screen across all displays
     */
    public void setWindowFullScreenOverAllDisplays();

    /**
     * Restore the window to a default size
     */
    public void restoreWindowSize();


}
