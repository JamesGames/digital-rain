package org.jamesgames.digitalrain.gui;

import org.jamesgames.digitalrain.rain.RainPaneSprite;

/**
 * RainPaneCreatorListener is an interface that defines an event from when a new {@link
 * org.jamesgames.digitalrain.rain.RainPaneSprite} is created.
 *
 * @author James Murphy
 */
interface RainPaneCreatorListener {

    /**
     * Called when a new RainPaneSprite has been created
     *
     * @param sprite
     *         The new RainPaneSprite created.
     */
    public void rainPaneSpriteCreated(RainPaneSprite sprite);
}
