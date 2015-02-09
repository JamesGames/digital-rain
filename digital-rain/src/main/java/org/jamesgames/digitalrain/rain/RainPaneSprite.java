package org.jamesgames.digitalrain.rain;

import net.jcip.annotations.ThreadSafe;
import org.jamesgames.easysprite.Sprite;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * RainPaneSprite is a {@link org.jamesgames.easysprite.Sprite} that renders and positions zero to many {@link
 * RainLineSprite}s across it's boundaries. The number of RainLineSprites contained within the sprite is how ever many
 * can fit across the sprite width wise, where one column of available space for a RainLineSprite is only used by one
 * RainLineSprite. The number of RainLineSprites changes when the sprite changes size.
 *
 * @author James Murphy
 */
@ThreadSafe
public class RainPaneSprite extends Sprite {

    public static final float defaultYVelocityUnitsPerMillisecond = 0.2f;
    private static final String defaultMonoSpacedFond = Font.MONOSPACED;
    private static final int unitsBetweenRainDropLines = 1;
    /**
     * There's currently no way through the RainPaneSprite interface for a user to supply this value, but a max of 300
     * was decided as an okay amount of RainDropSprites for most screen setups.
     */
    private static final int defaultMaxNumberOfRainDropSpritesInLine = 300;
    private static final Random random = new Random();

    private final List<Color> availableRainColors;
    private final RainDropCharacterImageStore characterImageStore;
    private final int spaceNeededToAddAnotherRainDropLine;
    private final int fontWidth;
    private final int fontHeight;

    private float yVelocityToUseForRainLines = defaultYVelocityUnitsPerMillisecond;


    /**
     * Useful to keep track of the furthest x coordinate drawn to so one can add in more rain drop lines during a
     * resize. Without keeping track of this one would have to count how many child sprites there are that are rain drop
     * rain drop lines, or assume the count of child sprites is only counting rain drop rain drop lines, and then
     * multiple one of those two numbers by the width of the font plus 1, subtracting 1 at the end.
     */
    private int closetXCoordinateFromLeftForAnotherRainDropLine;

    public RainPaneSprite(Set<Color> rainColors, int fontStyle, int fontSize) {
        this(rainColors, new Font(defaultMonoSpacedFond, fontStyle, fontSize));
    }

    public RainPaneSprite(Set<Color> rainColors, Font rainFont) {
        super(0, 0);
        this.availableRainColors = new ArrayList<>(rainColors);
        this.characterImageStore =
                new RainDropCharacterImageStore(rainColors, RainLineSprite.bottomRainDropColor,
                        rainFont);
        this.fontWidth = characterImageStore.getFontWidth();
        this.fontHeight = characterImageStore.getFontHeight();
        this.closetXCoordinateFromLeftForAnotherRainDropLine = 0;
        this.spaceNeededToAddAnotherRainDropLine = fontWidth + unitsBetweenRainDropLines;
        addNewRainLineSpritesToFarRight();
    }

    public synchronized void changeRainLineYVelocities(float newYVelocityInUnitsPerMillisecond) {
        this.yVelocityToUseForRainLines = newYVelocityInUnitsPerMillisecond;
        for (Sprite s : this) {
            if (s instanceof RainLineSprite) {
                s.setYVelocity(yVelocityToUseForRainLines);
            }
        }
    }

    private void addNewRainLineSpritesToFarRight() {
        for (int nextXCoordinateToAddRainDropLineTo = closetXCoordinateFromLeftForAnotherRainDropLine;
             nextXCoordinateToAddRainDropLineTo + spaceNeededToAddAnotherRainDropLine <= this.getWidth();
             nextXCoordinateToAddRainDropLineTo += spaceNeededToAddAnotherRainDropLine) {

            addChildSprite(generateRandomRainLineSprite(nextXCoordinateToAddRainDropLineTo));
            closetXCoordinateFromLeftForAnotherRainDropLine += spaceNeededToAddAnotherRainDropLine;
        }
    }

    private Sprite generateRandomRainLineSprite(int xCoordinate) {
        RainLineSprite s = new RainLineSprite(getRandomRainColorFromSpecifiedList(), fontWidth, fontHeight,
                characterImageStore,
                getHeight(), defaultMaxNumberOfRainDropSpritesInLine, yVelocityToUseForRainLines, true);
        s.setXCoordinateTopLeft(xCoordinate);
        s.setYCoordinateTopLeft(getRandomRainDropLineYCoordinatePosition(s));

        return s;
    }

    private Color getRandomRainColorFromSpecifiedList() {
        return availableRainColors.get(random.nextInt(availableRainColors.size()));
    }

    private int getRandomRainDropLineYCoordinatePosition(RainLineSprite s) {
        // The random amount is multiplied by 2 so that the spread of falling sprites is big enough that the first half
        // (which would otherwise have been all of the sprites) of the sprites are done falling, there is not a
        // large gap of no sprites falling as the sprites that fell through are still higher in their own initial
        // y coordinate positions. It's possible to think of this like two waves, where the second wave blends the
        // first wave, and the first wave's return that takes a while to come back to screen, as the return position
        // is not simply right above position 0, but some random amount off screen higher than that.
        return -s.getMaxHeight() + (2 * -random.nextInt(s.getMaxHeight()) + 1);
    }

    @Override
    protected synchronized void onResize(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        if (newWidth > oldWidth) {
            addNewRainLineSpritesToFarRight();
        } else if (newWidth < oldWidth) {
            removeNonVisibleRainDropLinesFromFarRight();
        }
    }

    private void removeNonVisibleRainDropLinesFromFarRight() {
        // Set to keep track of non-visible Lines due to new screen size
        Set<Sprite> nonVisibleRainDropLines = new HashSet<>();

        // Find sprites that need to be removed due to not appearing anymore on the screen anymore
        for (Sprite s : this) {
            if (s instanceof RainLineSprite) {
                if (s.getRoundedXCoordinateTopLeft() > this.getWidth()) {
                    nonVisibleRainDropLines.add(s);
                }
            }
        }
        this.removeChildSprites(nonVisibleRainDropLines);

        closetXCoordinateFromLeftForAnotherRainDropLine -=
                nonVisibleRainDropLines.size() * spaceNeededToAddAnotherRainDropLine;
    }


    @Override
    protected synchronized void updateBeforeChildren(long elapsedTimeInMilliseconds) {
        for (Sprite s : this) {
            if (s instanceof RainLineSprite) {
                if (s.getRoundedYCoordinateTopLeft() > this.getHeight()) {
                    ((RainLineSprite) s)
                            .resetHeightColorAndPosition(this.getHeight(),
                                    getRandomRainColorFromSpecifiedList());
                }
            }
        }
    }

    @Override
    protected synchronized void drawUnderChildren(Graphics2D g) {
        Graphics localGraphics = g.create();
        localGraphics.setColor(Color.BLACK);
        localGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        localGraphics.dispose();
    }

    @Override
    protected synchronized void debugDraw(Graphics2D g) {
        Graphics debugGraphics = g.create();
        debugGraphics.setColor(Color.WHITE);
        debugGraphics.drawLine(closetXCoordinateFromLeftForAnotherRainDropLine, 0,
                closetXCoordinateFromLeftForAnotherRainDropLine, this.getHeight());
        debugGraphics.dispose();
    }
}
