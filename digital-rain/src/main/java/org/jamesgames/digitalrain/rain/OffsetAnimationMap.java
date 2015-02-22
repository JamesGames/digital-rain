package org.jamesgames.digitalrain.rain;

import org.jamesgames.jamesjavautils.general.IntPair;

import java.util.HashMap;
import java.util.function.Function;

/**
 * OffsetAnimationMap stores the fixed x and y offset that some graphical related object should be moved by when it is at
 * a certain y coordinate. If such an offset is not known when queried, then the offsets are computed and used for all
 * future queries of that specific y coordinate.
 *
 * @author James Murphy
 */
public class OffsetAnimationMap {
    private final HashMap<Integer, IntPair> xOffsetMap = new HashMap<>();
    private final HashMap<Integer, IntPair> yOffsetMap = new HashMap<>();
    private final Function<Integer, IntPair> yCoordinateToXOffsetFunction;

    public OffsetAnimationMap(Function<Integer, IntPair> yCoordinateToXOffsetFunction) {
        this.yCoordinateToXOffsetFunction = yCoordinateToXOffsetFunction;
    }

    public int getXOffset(int yCoordinate) {
        return xOffsetMap.computeIfAbsent(yCoordinate, yCoordinateToXOffsetFunction).getX();
    }

    public int getYOffset(int yCoordinate) {
        return yOffsetMap.computeIfAbsent(yCoordinate, yCoordinateToXOffsetFunction).getY();
    }
}
