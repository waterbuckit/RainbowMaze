package mazesolver;

import java.awt.*;

public enum Wall {
    NORTH(new Rectangle(0,0,1,0)),
    EAST(new Rectangle(1,0,0,1)),
    SOUTH(new Rectangle(0,1,1,0)),
    WEST(new Rectangle(0,0,0,1));


    Wall(Rectangle rect) {
        this.rect = rect;
    }

    private final Rectangle rect;

    public Rectangle getRect() {
        return rect;
    }
    public static Wall fromDirection(Direction dir){
        switch(dir){
            case NORTH: return Wall.NORTH;
            case SOUTH: return Wall.SOUTH;
            case EAST: return Wall.EAST;
            case WEST: return Wall.WEST;
            default: throw new RuntimeException(dir+" is not a enum member");
        }
    }
    public static void removeBetween(MazeSolver.Maze.Cell current, MazeSolver.Maze.Cell next) {
        Direction connected = Direction.betweenPoints(current.getPos(),next.getPos());
        current.unsetWall(fromDirection(connected));
        next.unsetWall(fromDirection(connected.opposite()));
    }
}
