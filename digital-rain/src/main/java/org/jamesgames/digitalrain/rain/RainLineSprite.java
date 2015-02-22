package org.jamesgames.digitalrain.rain;

import net.jcip.annotations.ThreadSafe;
import org.jamesgames.easysprite.Sprite;

import java.awt.*;
import java.util.*;

/**
 * A RainLineSprite is a {@link org.jamesgames.easysprite.Sprite} that contains one to many {@link
 * org.jamesgames.digitalrain.rain.RainDropSprite}s, and renders them in a vertical line. The number of RainDropSprites
 * within the sprite is a random amount, between 1 and minimum of how many can fit in the parent height wise and a max
 * number supplied via the constructor. The sprite has the ability to render row by row, where it slowly descends
 * displaying characters on specific rows (like rows in a text editor), or the sprite can simply render and slowly
 * descend visually following no specific fixed rendering positions (a pixel by pixel approach).
 *
 * @author James Murphy
 */
@ThreadSafe
class RainLineSprite extends Sprite {
    public static final Color bottomRainDropColor = Color.WHITE;
    private static final int unitsBetweenRainDrops = 1;

    /**
     * This deque of RainDropSprites are the RainDropSprites that are possibly child RainDropSprites of this
     * RainLineSprite. As the RainLineSprite grows it adds some sprites from the deque, and as it shrinks it adds some
     * sprites to the deque to use later when it grows again. This helps eliminate garbage collection.
     */
    private final Deque<RainDropSprite> reusableCachedRainDropSprites;

    /**
     * Used to modify the x and y coordinate drawing position of a RainDropSprite (offsets retrieved are based on the y
     * drawing coordinate of the Sprite
     */
    private final OffsetAnimationMap offsetAnimationMap;

    /**
     * OrderedStackOfRainDropSprites is used to maintain knowledge of what the order of the child RainDropSprites are
     * visually.
     */
    private OrderedStackOfRainDropSprites orderedStackOfRainDropSprites;

    /**
     * Handles keeping track of rendering offsets for RainLineSprites, as well as the transferring of the character
     * represented by one RainDropSprite to another when RainDropSprites descend a row.
     */
    private RainDropLineRowOffsetUpdater rowOffsetUpdater;

    /**
     * The size of a row in the RainLineSprite, which is also the spacing between each RainDropSprite y coordinate wise
     * as RainDropSprites are positioned to be on top of each other.
     */
    private final int unitsFromOneRowToAnother;

    /**
     * Width of font to pass to any created RainDropSprites.
     */
    private final int fontWidth;

    /**
     * Width of height to pass to any created RainDropSprites.
     */
    private final int fontHeight;

    /**
     * Color used for the RainDropSprites in this RainLineSprite.
     */
    private Color rainRainDropColor;

    /**
     * Image store to pass to any created RainDropSprites.
     */
    private final RainDropCharacterImageStore characterImageStore;

    /**
     * Reference is saved off here to help query when to transfer the character used by one sprite to another (which is
     * when it reaches a new row).
     */
    private final RainDropSprite bottomRainDropSprite;

    /**
     * Used to determine if this RainLineSprite should calculate and set y coordinate rendering offsets for it's child
     * RainDropSprites. These offsets help the child RainDropSprites to fall down aligned on rigid rows as if the
     * character of a RainDropSprite was moving from one row in a text processor to the next, versus a more fluid
     * descent in a pixel by pixel fashion.
     */
    private final boolean renderChildRainDropSpritesByRow;

    /**
     * Max number of child RainDropSprites this RainLineSprite should have.
     */
    private final int maxRainDropsAllowed;

    /**
     * Height of parent sprite, used to help determine a max length for the RainLineSprite.
     */
    private int parentSpriteHeight;

    /**
     * The number of RainDropSprites in the this RainLineSprite.
     */
    private int currentRainDropSpriteCount;

    /**
     * Used for random calculations, like computing a new random amount of RainDropSprites to use
     */
    private final Random random = new Random();

    public RainLineSprite(OffsetAnimationMap offsetAnimationMap, Color rainColor, int fontWidth, int fontHeight,
            RainDropCharacterImageStore characterImageStore, int parentSpriteHeight, int maxRainDropsAllowed,
            float yVelocity, boolean renderChildRainDropSpritesByRow) {
        super(fontWidth, 0);

        if (maxRainDropsAllowed <= 0) {
            throw new IllegalArgumentException(
                    "Max rain drops allowed must be greater than 0 (" + maxRainDropsAllowed + " was supplied as max)");
        }

        this.fontWidth = fontWidth;
        this.fontHeight = fontHeight;
        this.characterImageStore = characterImageStore;
        this.maxRainDropsAllowed = maxRainDropsAllowed;
        this.reusableCachedRainDropSprites = new ArrayDeque<>(this.maxRainDropsAllowed);
        this.unitsFromOneRowToAnother = fontHeight + unitsBetweenRainDrops;
        this.renderChildRainDropSpritesByRow = renderChildRainDropSpritesByRow;
        this.parentSpriteHeight = parentSpriteHeight;
        setYVelocity(yVelocity);
        this.rainRainDropColor = rainColor;
        this.offsetAnimationMap = offsetAnimationMap;


        // Create and add the RainDropSprite that will always be displayed at the end of the RainDropLine
        this.bottomRainDropSprite = new RainDropSprite(bottomRainDropColor, fontWidth, fontHeight, characterImageStore,
                this.offsetAnimationMap);
        addChildSprite(bottomRainDropSprite);
        // current size is one because we only added the bottom rain drop sprite
        this.currentRainDropSpriteCount = 1;
        updateHeight();

        // Helper objects
        this.orderedStackOfRainDropSprites =
                new OrderedStackOfRainDropSprites(bottomRainDropSprite, this.maxRainDropsAllowed);
        this.rowOffsetUpdater = new RainDropLineRowOffsetUpdater(bottomRainDropSprite, orderedStackOfRainDropSprites,
                unitsFromOneRowToAnother);

        // Add as many other RainDropSprites as needed
        addRainDropSprites(rainColor, calculateValidRandomRainDropSpriteCount());
        repositionBottomRainDropSpriteToBottomPosition();

    }

    /**
     * @return Largest height this RainLineSprite could be based on on the latest known parent sprite height and max
     * number of rain drops.
     */
    public synchronized int getMaxHeight() {
        return Math.min(maxNumberOfRainDropsThatCanFitInParentSpriteHeight(), maxRainDropsAllowed) *
                unitsFromOneRowToAnother;
    }

    private int calculateValidRandomRainDropSpriteCount() {
        return random.nextInt(
                Math.min(Math.max(maxNumberOfRainDropsThatCanFitInParentSpriteHeight(), 1), maxRainDropsAllowed)) + 1;
    }

    private int maxNumberOfRainDropsThatCanFitInParentSpriteHeight() {
        return parentSpriteHeight / unitsFromOneRowToAnother;
    }

    private void updateHeight() {
        this.setHeight(calculateHeightAtSpecificRainDropSpriteCount(currentRainDropSpriteCount));
    }

    private int calculateHeightAtSpecificRainDropSpriteCount(int rainDropSpriteCountToUse) {
        return (rainDropSpriteCountToUse * (fontHeight + unitsBetweenRainDrops)) - unitsBetweenRainDrops;
    }

    private void repositionBottomRainDropSpriteToBottomPosition() {
        bottomRainDropSprite.setYCoordinateTopLeft(this.getHeight() - unitsFromOneRowToAnother);
    }

    private void addReusableCachedSprite(Color rainColor) {
        reusableCachedRainDropSprites.add(new RainDropSprite(rainColor, fontWidth, fontHeight, characterImageStore,
                offsetAnimationMap));
    }

    private void addRainDropSprites(Color rainColor, int rainDropSpriteCountToAchieve) {
        // Adds the right number of RainDropSprites to this Sprite
        // Each iteration adds one RainDropSprite
        while (currentRainDropSpriteCount < rainDropSpriteCountToAchieve) {
            addRainDropSprite(rainColor);
        }
    }

    private void addRainDropSprite(Color rainColor) {
        if (reusableCachedRainDropSprites.size() == 0) {
            // No sprites to grab from deque, add another to the reusable deque
            addReusableCachedSprite(rainRainDropColor);
        }
        RainDropSprite s = reusableCachedRainDropSprites.removeLast();
        // Change the rain drop character each time a Sprite is reused just in case a series of characters
        // being reused could appear strange to the user
        s.setRainDropChar(RainDropCharacter.randomRainDropCharacter());
        // The sprite may need it's color changed as well if using a different one
        s.setRainDropColor(rainColor);
        // Reposition the sprite where needed as well
        s.setYCoordinateTopLeft(calculateYCoordinateForNextRainDropSpriteToAdd());
        // Add the sprite to both the ordered stack and the actual Sprite
        orderedStackOfRainDropSprites.pushRainDropSpriteToSecondTopPosition(s);
        this.addChildSprite(s);
        // Increment the number of RainDropSprites in this RainLineSprite
        currentRainDropSpriteCount++;
        // The number of RainDropSprites, also affects the height, so that must be updated as well
        updateHeight();
    }

    private int calculateYCoordinateForNextRainDropSpriteToAdd() {
        return (this.currentRainDropSpriteCount - 1) * unitsFromOneRowToAnother;
    }

    /**
     * Resizes the RainLineSprite's height based on a valid range, recolors it to a specified color, and resets the
     * sprite's position above it's parent sprite
     *
     * @param parentSpriteHeight
     *         This height helps determine the range of possible new heights to choose
     * @param newRainDropColor
     *         The color to change to
     */
    public synchronized void resetHeightColorAndPosition(int parentSpriteHeight, Color newRainDropColor) {
        this.parentSpriteHeight = parentSpriteHeight;
        int newRainDropSpriteCount = calculateValidRandomRainDropSpriteCount();
        if (newRainDropSpriteCount > maxRainDropsAllowed) {
            throw new IllegalArgumentException(
                    "Length of rain drop line (" + newRainDropSpriteCount + ") must not exceed the max " +
                            "length allowed (" + maxRainDropsAllowed + ")");
        }

        if (newRainDropSpriteCount < currentRainDropSpriteCount) {
            // Need to remove some RainDropSprites that no longer fit
            recolorAllSpritesAndRemoveRainDropSpritesThatDoNotFit(newRainDropColor,
                    currentRainDropSpriteCount - newRainDropSpriteCount);
        } else if (newRainDropSpriteCount > currentRainDropSpriteCount) {
            // More rainDropSprites can fit, so add some more
            addRainDropSprites(newRainDropColor, newRainDropSpriteCount);
            recolorAllSprites(newRainDropColor);
        }

        // Realign bottom rain drop sprite to be the last sprite visually
        repositionBottomRainDropSpriteToBottomPosition();

        // Replace the line back to the top of the panel after the resize to get the correct height
        setYCoordinateTopLeft(-getHeight());
    }

    private void recolorAllSprites(Color newRainDropColor) {
        for (RainDropSprite s : orderedStackOfRainDropSprites) {
            if (s != bottomRainDropSprite) {
                s.setRainDropColor(newRainDropColor);
            }
        }

    }

    private void recolorAllSpritesAndRemoveRainDropSpritesThatDoNotFit(Color newRainDropColor,
            int rainDropsToRemoveCount) {
        int rainDropsRemoved = 0;
        Set<Sprite> rainDropsToRemove = new HashSet<>(rainDropsToRemoveCount);
        for (RainDropSprite s : orderedStackOfRainDropSprites) {
            if (s != bottomRainDropSprite) {
                if (rainDropsRemoved < rainDropsToRemoveCount) {
                    rainDropsToRemove.add(s);
                    rainDropsRemoved++;
                    // Add it back to the deque
                    reusableCachedRainDropSprites.addLast(s);
                }
                s.setRainDropColor(newRainDropColor);
            }

        }
        // Remove child RainDropSprites from the actual Sprite, as well as the ordered stack
        this.removeChildSprites(rainDropsToRemove);
        this.orderedStackOfRainDropSprites.removeAll(rainDropsToRemove);
        // Need to maintain the count of RainDropSprites added
        currentRainDropSpriteCount -= rainDropsToRemoveCount;
        // The number of RainDropSprites also affects the height, so that must be updated as well
        updateHeight();
    }

    @Override
    protected synchronized void setDrawingDebugGraphicsToCachedSprites(boolean drawDebug) {
        for (Sprite s : reusableCachedRainDropSprites) {
            s.setDrawingDebugGraphics(drawDebug);
        }
    }


    @Override
    public synchronized void updateBeforeChildren(long elapsedTimeInMilliseconds) {
        if (renderChildRainDropSpritesByRow) {
            rowOffsetUpdater.updateRowOffsetsAndTransferCharsUpwardsIfNeeded(this.getYCoordinateTopLeft());
        }
    }

    @Override
    protected synchronized void debugDraw(Graphics2D g) {
        Graphics debugGraphics = g.create();
        debugGraphics.setColor(Color.WHITE);
        debugGraphics
                .drawRect(this.getXDrawingCoordinateTopLeft(), this.getYDrawingCoordinateTopLeft(), this.getWidth(),
                        this.getHeight());
        debugGraphics.dispose();
    }
}
