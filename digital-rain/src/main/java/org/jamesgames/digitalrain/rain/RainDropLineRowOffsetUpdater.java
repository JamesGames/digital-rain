package org.jamesgames.digitalrain.rain;

import net.jcip.annotations.Immutable;

/**
 * RainDropLineOffsetUpdater handles the calculation of rendering offsets for {@link
 * org.jamesgames.digitalrain.rain.RainLineSprite}s and the transferring of the character represented by one {@link
 * org.jamesgames.digitalrain.rain.RainDropSprite} to another when RainDropSprites descend a row.
 *
 * @author James Murphy
 */
@Immutable
class RainDropLineRowOffsetUpdater {

    private final RainDropSprite exampleRainDropSpriteBeingUsed;
    private final OrderedStackOfRainDropSprites orderedStackOfRainDropSprites;
    private final int unitsFromOneRowToAnother;

    /**
     * Constructs a RainDropLineRowOffsetUpdater.
     *
     * @param exampleRainDropSprite
     *         RainDropSprite that is used to compare it's old rendering offset to a new one, in order to know when all
     *         other RainDropSprites should be moved to another row and have their RainDropCharacter transfer between
     *         them. This should be a sprite that is always used (having it's position updated)
     * @param orderedStackOfRainDropSprites
     *         The RainDropSprites that are within a specific RainLineSprite.
     * @param unitsFromOneRowToAnother
     *         The number of units that separate one row that a RainDropSprite is rendered on to another.
     */
    RainDropLineRowOffsetUpdater(RainDropSprite exampleRainDropSprite,
            OrderedStackOfRainDropSprites orderedStackOfRainDropSprites, int unitsFromOneRowToAnother) {
        this.exampleRainDropSpriteBeingUsed = exampleRainDropSprite;
        this.orderedStackOfRainDropSprites = orderedStackOfRainDropSprites;
        this.unitsFromOneRowToAnother = unitsFromOneRowToAnother;
    }

    private int lastDrawingYCoordinateOfBottomRainDropSprite;

    /**
     * Updates the rendering offset for each RainDropSprite, and potentially moves the RainDropCharacter from one
     * RainDropSprite to another if the RainDropSprites were determined to be drawn on a row different than the last row
     * they were drawn to.
     *
     * @param yCoordinateTopLeftOfRainLineSprite
     *         The y coordinate location of the RainDropSpriteLine, used to compute offsets, as the RainDropSpriteLine
     *         has a velocity and actually moves, not the individual RainDropSprites who have a stationary position
     *         within the RainDropSpriteLine.
     */
    public void updateRowOffsetsAndTransferCharsUpwardsIfNeeded(float yCoordinateTopLeftOfRainLineSprite) {
        int oldOffset = exampleRainDropSpriteBeingUsed.getYCoordinateRenderingOffset();
        int currentYCoordinate = exampleRainDropSpriteBeingUsed.getYDrawingCoordinateTopLeft();

        updateRainDropSpriteRenderingOffsets(yCoordinateTopLeftOfRainLineSprite);

        int currentOffset = exampleRainDropSpriteBeingUsed.getYCoordinateRenderingOffset();
        if (spritesHaveJustBeenOffsetToNewRow(oldOffset, currentOffset,
                lastDrawingYCoordinateOfBottomRainDropSprite, currentYCoordinate)) {
            for (int transferCount = calculateHowManyRowsSpritesMoved(oldOffset, currentOffset,
                    lastDrawingYCoordinateOfBottomRainDropSprite, currentYCoordinate); transferCount > 0;
                 transferCount--) {
                orderedStackOfRainDropSprites.transferRainDropCharactersUpwards();
            }
        }

        lastDrawingYCoordinateOfBottomRainDropSprite = currentYCoordinate;
    }

    /**
     * Computes how many units a RainDropSprite should draw itself off from it's real y coordinate location, and saves
     * that value to each RainDropSprite. Rendering at a y coordinate location plus offset causes the RainDropSprite to
     * be drawn at a specific row.
     */
    private void updateRainDropSpriteRenderingOffsets(
            float yCoordinateTopLeftOfRainLineSprite) {
        // Negate the method call, only want to render the sprites until they reach
        // the next row or are past it
        int yRenderingOffset = -calculateUnitsUntilRainDropSpritesAreOnNewRow(yCoordinateTopLeftOfRainLineSprite);
        for (RainDropSprite s : orderedStackOfRainDropSprites) {
            s.setYCoordinateRenderingOffset(yRenderingOffset);
        }
    }

    /**
     * @return The number of units each RainDropSprite is from from naturally being drawn at a new row position if the
     * option to render RainDropSprites at specific rows was turned off (even though RainDropSprites are being drawn at
     * specific rows, their y coordinate is slowly updated as if it was moving in a more pixel by pixel approach).
     */
    private int calculateUnitsUntilRainDropSpritesAreOnNewRow(float yCoordinateTopLeftOfRainLineSprite) {
        if (yCoordinateTopLeftOfRainLineSprite >= 0) {
            return Math.round(((yCoordinateTopLeftOfRainLineSprite % unitsFromOneRowToAnother)));
        } else {
            return (unitsFromOneRowToAnother +
                    Math.round(((yCoordinateTopLeftOfRainLineSprite % unitsFromOneRowToAnother))));
        }
    }

    /**
     * @return True if RainDropSprites are going to be rendered a new row in the next render, false otherwise
     */
    private boolean spritesHaveJustBeenOffsetToNewRow(int oldOffset, int currentOffset, int oldYCoordinate,
            int currentYCoordinate) {
        return oldOffset + oldYCoordinate != currentOffset + currentYCoordinate;
    }

    /**
     * @return A count of how many rows the sprites moved, so if the sprites were on one row, but were updated to
     * display two rows further, it would return 2. Ideally this usually returns 1, but if an update did not come
     * through for a long time the sprites could jump multiple rows.
     */
    private int calculateHowManyRowsSpritesMoved(int oldOffset, int currentOffset, int oldYCoordinate,
            int currentYCoordinate) {
        return ((currentOffset + currentYCoordinate) - (oldOffset + oldYCoordinate)) / unitsFromOneRowToAnother;
    }
}
