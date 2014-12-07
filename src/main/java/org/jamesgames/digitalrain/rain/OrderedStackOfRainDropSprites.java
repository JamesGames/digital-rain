package org.jamesgames.digitalrain.rain;

import org.jamesgames.easysprite.Sprite;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 * OrderedStackOfRainDropSprites is a helper class keeps track of the visual order of {@link
 * org.jamesgames.digitalrain.rain.RainDropSprite}s from a {@link org.jamesgames.digitalrain.rain.RainLineSprite }. This
 * is not a replacement of Sprite's internal list of child sprites. This class always contains at least one Sprite in
 * the OrderedStackOfRainDropSprites, which is the bottommost visual RainDropSprite, that sprite cannot be removed from
 * the stack.
 *
 * @author James Murphy
 */
class OrderedStackOfRainDropSprites implements Iterable<RainDropSprite> {

    /**
     * This deque is used as a stack of RainDropReferences where the bottom of the stack is the highest positioned
     * RainDropSprites visually, while the top of the stack is the bottommost RainDropSprite.
     */
    private final Deque<RainDropSprite> orderedStackOfRainDropSprites;

    /**
     * Reference to the bottommost visual RainDropSprite in order to know if one tries to remove it (so the code can
     * throw an exception)
     */
    private final RainDropSprite bottomRainDropSprite;

    /**
     * Constructs a OrderedStackOfRainDropSprites
     *
     * @param bottomRainDropSprite
     *         The initial bottomRainDropSprite
     * @param mostAmountOfRainDropSpritesExpected
     *         The max amount of RainDropSprites to expect
     */
    public OrderedStackOfRainDropSprites(RainDropSprite bottomRainDropSprite, int mostAmountOfRainDropSpritesExpected) {
        this.orderedStackOfRainDropSprites = new ArrayDeque<>(mostAmountOfRainDropSpritesExpected);
        this.bottomRainDropSprite = bottomRainDropSprite;
        orderedStackOfRainDropSprites.addLast(bottomRainDropSprite);
    }

    /**
     * This method adds a RainDropSprite to the ordered stack of RainDropSprites as the second bottommost RainDropSprite
     * (as the bottommost RainDropSprite is handled and repositioned separately).
     *
     * @param s
     *         A RainDropSprite to add to this RainLineSprite
     */
    public void pushRainDropSpriteToSecondTopPosition(RainDropSprite s) {
        // Add the sprite as the second most top element,
        // as it's assumed to be the second most bottommost RainDropSprite visually
        // Removing the last element (which is the bottomRainDropSprite)
        RainDropSprite shouldBeBottommostSprite = orderedStackOfRainDropSprites.removeLast();
        // Adding last the passed sprite
        orderedStackOfRainDropSprites.addLast(s);
        // Adding over the passed sprite the bottomRainDropSprite, effectively placing the passed sprite at
        // the required second position from top of the stack
        orderedStackOfRainDropSprites.addLast(shouldBeBottommostSprite);
    }

    /**
     * This method removes all Sprites from the ordered stack that also exist in the passed collection
     *
     * @param spritesToRemove
     *         RainDropSprites to remove
     */
    public void removeAll(Collection<Sprite> spritesToRemove) {
        if (spritesToRemove.contains(bottomRainDropSprite)) {
            throw new IllegalArgumentException(
                    "Cannot remove the bottom most RainDropSprite, class is designed for use to remove and add new " +
                            "RainDropSprites but never remove the bottom most RainDropSprite, as it should always " +
                            "exist in the stack as the stack should always have at least one remaining sprite.");
        }
        boolean atLeastOneOfTheSpritesExistedInCollection =
                orderedStackOfRainDropSprites.removeAll(spritesToRemove);

        if (!atLeastOneOfTheSpritesExistedInCollection) {
            throw new IllegalArgumentException("None of the sprites exist in the ordered stack of rain drop sprites");
        }
    }

    /**
     * Sets the RainDropCharacter each RainDropSprite has to the RainDropCharacter the sprite visually below it has. The
     * bottommost RainDropSprite is given a new random RainDropCharacter.
     */
    public void transferRainDropCharactersUpwards() {
        RainDropCharacter newCharacterToUse = RainDropCharacter.randomRainDropCharacter();
        // Iterate through all RainDropSprites and transfer the rain drop character from one sprite to
        // the sprite above it. First iteration will be the bottommost RainDropSprite as guaranteed by iterator() which
        // will be updated with the initial random character.
        for (RainDropSprite s : this) {
            RainDropCharacter currentSpriteChar = s.getRainDropChar();
            s.setRainDropChar(newCharacterToUse);
            // Let the next iteration use the old current rain drop char
            newCharacterToUse = currentSpriteChar;
        }
    }


    /**
     * @return An iterator where the first element iterated through is the bottommost RainDropSprite, while the last
     * element iterated is the highest RainDropSprite visually.
     */
    @Override
    public Iterator<RainDropSprite> iterator() {
        return orderedStackOfRainDropSprites.descendingIterator();
    }
}