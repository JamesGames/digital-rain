package org.jamesgames.digitalrain.rain;

import net.jcip.annotations.ThreadSafe;
import org.jamesgames.easysprite.Sprite;
import org.jamesgames.jamesjavautils.time.ElapsedTimeTimer;

import java.awt.*;

/**
 * A RainDropSprite is a {@link org.jamesgames.easysprite.Sprite} that renders a special character. Note that the
 * special character rendered is a character represented by the package encapsulated enum of {@link
 * org.jamesgames.digitalrain.rain.RainDropCharacter}, if one wants to change what characters are rendered by
 * RainDropSprites that would be the enum to change. A potential useful update in the future is to have some kind of
 * ability to supply the characters one wishes to draw.
 *
 * @author James Murphy
 */
@ThreadSafe
class RainDropSprite extends Sprite {

    private static final long initialShortWaitTimeForCharChangeInMS = 0;
    private static final long shortestWaitTimeForCharChangeInMS = 1000;
    private static final long longestWaitTimeForCharChangeInMS = 5000;

    /**
     * Holds the various images that match to a RainDropCharacter that this sprite will use to draw it's
     * RainDropCharacter.
     */
    private final RainDropCharacterImageStore characterImageStore;

    /**
     * Timer to keep track of when the sprite should change what RainDropCharacter it renders.
     */
    private final ElapsedTimeTimer timerToChangeLetterRandomly;

    /**
     * The color used to render this RainDropCharacter
     */
    private Color rainDropColor;

    /**
     * The RainDropCharacter this sprite will render
     */
    private RainDropCharacter rainDropChar;

    /**
     * Used to modify the y coordinate drawing position of this RainDropSprite. The RainDropSprite will be drawn at it's
     * y coordinate location plus the value of this instance variable.
     */
    private int yCoordinateRenderingOffset;

    /**
     * Used to modify the x and y coordinate drawing position of a RainDropSprite (offsets retrieved are based on the y
     * drawing coordinate of the Sprite
     */
    private final OffsetAnimationMap offsetAnimationMap;


    /**
     * @param rainDropColor
     *         Color of the rain
     * @param fontWidth
     *         Width of the font, which also is used as the width of the sprite
     * @param fontHeight
     *         Height of the font, which is also used as the height of the sprite
     * @param characterImageStore
     *         Object to query backdrop and character images from
     * @param offsetAnimationMap
     *         Object to query x and y render animation offsets from
     */
    public RainDropSprite(Color rainDropColor, int fontWidth, int fontHeight,
            RainDropCharacterImageStore characterImageStore, OffsetAnimationMap offsetAnimationMap) {
        super(fontWidth, fontHeight);
        this.characterImageStore = characterImageStore;
        this.rainDropColor = rainDropColor;
        this.offsetAnimationMap = offsetAnimationMap;
        this.timerToChangeLetterRandomly =
                new ElapsedTimeTimer(initialShortWaitTimeForCharChangeInMS, longestWaitTimeForCharChangeInMS);
        setRainDropCharToRandomValidChar();
    }

    public synchronized int getYCoordinateRenderingOffset() {
        return yCoordinateRenderingOffset;
    }

    public synchronized void setYCoordinateRenderingOffset(int yCoordinateRenderingOffset) {
        this.yCoordinateRenderingOffset = yCoordinateRenderingOffset;
    }

    public synchronized RainDropCharacter getRainDropChar() {
        return rainDropChar;
    }

    public synchronized void setRainDropChar(RainDropCharacter rainDropChar) {
        this.rainDropChar = rainDropChar;
    }

    public synchronized void setRainDropColor(Color rainDropColor) {
        this.rainDropColor = rainDropColor;
    }

    private void setRainDropCharToRandomValidChar() {
        rainDropChar = RainDropCharacter.randomRainDropCharacter();
    }

    private void updateTimerToChangeLetterRandomly(long elapsedTimeInMilliseconds) {
        timerToChangeLetterRandomly.addElapsedTimeInMilliseconds(elapsedTimeInMilliseconds);
        if (timerToChangeLetterRandomly.isTimerFinished()) {
            // Time to change the rain drop char to another random character!
            setRainDropCharToRandomValidChar();
            timerToChangeLetterRandomly.resetElapsedTimeToTimePastCurrentTarget();
            timerToChangeLetterRandomly.resetTargetTimeToRandomTimeSpecifiedInMilliseconds(
                    shortestWaitTimeForCharChangeInMS,
                    longestWaitTimeForCharChangeInMS);
        }
    }

    @Override
    public synchronized void updateBeforeChildren(long elapsedTimeInMilliseconds) {
        updateTimerToChangeLetterRandomly(elapsedTimeInMilliseconds);
    }

    @Override
    public synchronized void drawUnderChildren(Graphics2D g) {
        Graphics2D spriteGraphics = (Graphics2D) g.create();

        int drawingY = getYDrawingCoordinateTopLeft() + yCoordinateRenderingOffset;
        int drawingX = getXDrawingCoordinateTopLeft() + offsetAnimationMap.getXOffset(drawingY);
        drawingY += offsetAnimationMap.getYOffset(drawingY);
        spriteGraphics.drawImage(characterImageStore.getCharacterImage(rainDropColor, rainDropChar),
                drawingX, drawingY, null);

        spriteGraphics.dispose();
    }

    @Override
    protected synchronized void debugDraw(Graphics2D g) {
        Graphics debugGraphics = g.create();
        debugGraphics.setColor(Color.WHITE);
        debugGraphics.drawRect(this.getXDrawingCoordinateTopLeft(), this.getYDrawingCoordinateTopLeft(),
                this.getWidth(), this.getHeight());
        debugGraphics.dispose();
    }


}
