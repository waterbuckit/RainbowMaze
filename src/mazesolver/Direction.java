package mazesolver;

import java.awt.*;
import java.util.stream.Stream;

public enum Direction {
    NORTH(0, -1),
    SOUTH(0, +1),
    EAST(+1, 0),
    WEST(-1, 0);



    public static Direction betweenPoints(Point a, Point b) {
        int xdiff = (int) Math.signum(b.x - a.x),
                ydiff = (int) Math.signum(b.y - a.y);
        if (xdiff == 0) {
            switch (ydiff) {
                case 1:
                    return SOUTH;
                case -1:
                    return NORTH;
            }

        }
        if (ydiff == 0) {
            switch (xdiff) {
                case 1:
                    return EAST;
                case -1:
                    return WEST;
            }
        }
        throw new RuntimeException("Can't make a direction between " + a + " and " + b);
    }

    public static Stream<Point> compass() {
        return Stream.concat(cardinal(), intercardinal());
    }

    public static Stream<Point> cardinal() {
        return Stream.of(Direction.values()).map(Direction::getP);
    }

    public static Stream<Point> intercardinal() {
        return Stream.of(
                NORTH.plus(EAST),
                NORTH.plus(WEST),
                SOUTH.plus(EAST),
                SOUTH.plus(WEST));
    }

    public Direction opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
        }
        throw new RuntimeException("Illegal enum "+this);
    }

    public Point getP() {
        return p;
    }

    public Point plus(Direction other) {
        return new Point(p.x + other.p.x, p.y + other.p.y);
    }

    private final Point p;

    Direction(int x, int y) {
        p = new Point(x, y);
    }
}
