package org.jamesgames.digitalrain.rain;

import net.jcip.annotations.ThreadSafe;
import org.jamesgames.jamesjavautils.graphics.Drawable;
import org.jamesgames.jamesjavautils.graphics.image.ImageCreator;
import org.jamesgames.jamesjavautils.graphics.image.ImageDescription;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RainDropCharacterImageStore creates and stores images that have rendered characters from the enum {@link
 * org.jamesgames.digitalrain.rain.RainDropCharacter} rendered on, and could potentially (but not yet) store any other
 * useful images related to a specific color and character from RainDropCharacter. Objects of
 * RainDropCharacterImageStore are queried for certain images by supplying a RainDropCharacter and color.
 *
 * @author James Murphy
 */
@ThreadSafe
class RainDropCharacterImageStore {

    private static final int largestAssumeFontWidthAndHeight = 100;

    private final Map<Color, Map<RainDropCharacter, Image>> rainDropCharacterImageStore = new HashMap<>();

    private final int fontHeight;
    private final int fontWidth;

    public RainDropCharacterImageStore(Set<Color> rainDropColors, Color lastRainDropInRainDropLineColor, Font font) {
        Set<Color> localRainColorSet = new HashSet<>(rainDropColors);
        localRainColorSet.add(lastRainDropInRainDropLineColor);

        /*
         * Calculates and sets the new width and height of the font. This however assumes that the font width and height
         * calculated from this graphics object instance created locally here in this method will have the same
         * measurements as the characters drawn in future graphics objects. This could be a source of a bug in the
         * future, for example, the application launches, but then the user of the computer changes their operating
         * system DPI scaling through accessibility settings. It's a lot simpler to create the font sizes initially in
         * one place and pass that info on.
         */
        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();
        BufferedImage createdImage = defaultConfiguration
                .createCompatibleImage(largestAssumeFontWidthAndHeight, largestAssumeFontWidthAndHeight,
                        Transparency.OPAQUE);
        FontMetrics metrics = createdImage.getGraphics().getFontMetrics(font);
        this.fontWidth = metrics.stringWidth("" + 'A');
        this.fontHeight = metrics.getAscent() - metrics.getDescent();

        // Create the images after knowing the font width and height
        ImageCreator imageCreator = new ImageCreator();
        for (Color color : localRainColorSet) {

            // Internal map in character image store to use for this color
            Map<RainDropCharacter, Image> internalCharacterImageMap = new HashMap<>();
            rainDropCharacterImageStore.put(color, internalCharacterImageMap);
            // Loop through all characters and add an image representing each character to the internal map
            for (RainDropCharacter character : RainDropCharacter.values()) {
                Drawable rainDropGraphics =
                        new DrawableRainDrop(color, character.getCharacter(), font, fontHeight);
                Image characterImage = imageCreator.createImage(
                        new ImageDescription(rainDropGraphics, fontWidth, fontHeight,
                                Transparency.TRANSLUCENT)
                );
                internalCharacterImageMap.put(character, characterImage);
            }

        }
    }

    /**
     * @return Image with the passed character drawn in it with the specified color, where both arguments should have
     * been represented during the construction of the RainDropCharacterImageStore.
     */
    public synchronized Image getCharacterImage(Color rainDropColor, RainDropCharacter character) {
        Map<RainDropCharacter, Image> internalCharacterImageMap = rainDropCharacterImageStore.get(rainDropColor);
        if (internalCharacterImageMap == null) {
            throw new IllegalArgumentException("No character image found with the supplied color: " + rainDropColor);
        }
        Image charImage = internalCharacterImageMap.get(character);
        if (charImage == null) {
            throw new IllegalArgumentException(
                    "No character image found with the supplied RainDropCharacter: " + rainDropColor);
        }
        return charImage;
    }


    public synchronized int getFontHeight() {
        return fontHeight;
    }

    public synchronized int getFontWidth() {
        return fontWidth;
    }

    private static class DrawableRainDrop implements Drawable {
        private final Color rainColor;
        private final Character rainDropChar;
        private final Font font;
        private final int fontHeight;

        private DrawableRainDrop(Color rainColor, Character rainDropChar, Font font, int fontHeight) {
            this.rainColor = rainColor;
            this.rainDropChar = rainDropChar;
            this.font = font;
            this.fontHeight = fontHeight;
        }

        @Override
        public void draw(Graphics2D g) {
            Graphics2D rainDropGraphics = (Graphics2D) g.create();

            rainDropGraphics.setFont(font);
            rainDropGraphics.setColor(rainColor);
            rainDropGraphics.drawString("" + rainDropChar, 0, fontHeight);

            rainDropGraphics.dispose();
        }

    }
}
