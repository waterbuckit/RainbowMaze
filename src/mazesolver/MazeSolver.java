/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mazesolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

import static mazesolver.Wall.*;

/**
 * @author waterbucket
 */
public class MazeSolver {
    /** unicorn puke */
//    private static final float HUE_STEP = 0.05f;
    /** tasteful rainbow fade */
//    private static final float HUE_STEP = 0.001f;
    /** quicker fade */
    private static final float HUE_STEP = 0.005f;

    /** default:75 faster:50 asap:0 */
    private static final int SLEEP_TIME = 75;

    private static final int MAZE_WIDTH = 250;
    private static final int MAZE_HEIGHT = 250;

    private static float hue = 0;

    private final JFrame frame;
    private Maze maze;
    private MazePanel panel;
    private boolean solving;

    public MazeSolver() {
        this.maze = new Maze(MAZE_WIDTH, MAZE_HEIGHT);
        this.panel = new MazePanel();
        this.panel.setBackground(Color.LIGHT_GRAY);
        this.frame = new JFrame();
        this.frame.setTitle("MazeGenerator");
        this.frame.setPreferredSize(new Dimension(800, 600));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLayout(new GridBagLayout());
        this.frame.add(this.panel);
        this.frame.pack();
    }

    public static void main(String[] args) {
        MazeSolver ms = new MazeSolver();
        ms.begin();
    }

    Optional<Maze.Cell> pickNext(Maze.Cell cell) {
        Maze.Cell[] list = maze.getNeighbours(cell)
                .filter(Are.NOT(Maze.Cell::isVisited))
                .toArray(Maze.Cell[]::new);
        return (list.length > 0)
                ? Optional.of(list[(int) (Math.random() * list.length)])
                : Optional.empty();
    }

    private void begin() {
        this.frame.setVisible(true);
        this.solving = true;
        solve();
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {

        }
    }

    private void solve() {
        int initialX = (int) (Math.random() * MAZE_WIDTH);
        int initialY = (int) (Math.random() * MAZE_HEIGHT);
        LinkedList<Maze.Cell> stack = new LinkedList<>();
        Maze.Cell current = maze.cells[initialY][initialX];
        current.setVisited(true);
        current.setIsActive(true);
        while (solving) {
            Optional<Maze.Cell> nextOpt = pickNext(current);
            final Maze.Cell temp_current = current;
            current = nextOpt.map(next -> {
                temp_current.setIsActive(false);
                next.setVisited(true);
                next.setIsActive(true);
                Wall.removeBetween(temp_current, next);
                stack.add(next);
                return next;
            }).orElseGet(() -> {
                temp_current.setIsActive(false);
                Maze.Cell temp_ret = stack.pop();
                if (stack.isEmpty()) solving = false;
                temp_ret.setIsActive(true);
                return temp_ret;
            });
            this.panel.repaint();
            sleep();
        }
//        System.out.println("Finished generating maze");
    }

    class MazePanel extends JPanel {

        private Dimension cellSize;

        @Override
        public Dimension getPreferredSize() {
            Container parent = getParent();
            int size = Math.min(parent.getHeight(), parent.getWidth());
            return new Dimension(size,size);
        }

        public MazePanel() {
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    recalculateCellSize();
                }
            });
            recalculateCellSize();
        }

        void recalculateCellSize() {
            int w = this.getWidth() / MAZE_WIDTH;
            int h = this.getHeight() / MAZE_HEIGHT;
            cellSize = new Dimension(w, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate((getWidth()-MAZE_WIDTH*cellSize.width)/2,
                    (getHeight()-MAZE_HEIGHT*cellSize.height)/2);
//            g2d.setRenderingHint(
//                    RenderingHints.KEY_ANTIALIASING,
//                    RenderingHints.VALUE_ANTIALIAS_ON);
            for (int y = 0; y < maze.cells.length; y++) {
                for (int x = 0; x < maze.cells.length; x++) {
                    Maze.Cell c = maze.cells[y][x];
                    c.drawBox(g2d, cellSize);
                    if(c.isVisited())
                    c.drawBorder(g2d, cellSize);
                }
            }
//            System.out.println("Endframe");
        }
    }

    class Maze {

        private Cell[][] cells;

        public Maze(int width, int height) {
            this.cells = setUpCells(width, height);
        }

        /**
         * Safely finds the cell at a position
         * @param x
         * @param y
         * @return cell if one is present at specificed location
         */
        public Optional<Cell> get(int x, int y) {
            //for edge safety
            //can be converted for wrapping if needed using modulus
            try {
                return Optional.ofNullable(cells[y][x]);
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        /**
         * Gets directly traversable cells from cell c
         * @param c for which to get the cells
         * @return neighbours
         */
        Stream<Cell> getNeighbours(Cell c) {
            Point loc = c.pos;
            return Direction.cardinal()
                    .map(s -> get(loc.y + s.y, loc.x + s.x))
                    .flatMap(Optional::stream);
        }

        /**
         * Initialise cells to zero, with all walls set
         */
        private Cell[][] setUpCells(int width, int height) {
            Cell[][] cells = new Cell[width][height];
            for (int y = 0; y < cells.length; y++) {
                for (int x = 0; x < cells.length; x++) {
                    cells[x][y] = new Cell(x, y);
                    cells[x][y].setWall(NORTH, SOUTH, EAST, WEST);
                }
            }
            return cells;
        }

        class Cell {

            private final Point pos;
            public BitSet walls;
            private boolean visited;
            private boolean active;
            private float hue;

            public Cell(int x, int y) {
                this.hue = 0;
                this.pos = new Point(x, y);
                this.visited = false;
                this.walls = new BitSet(4);
                this.active = false;
            }


            public Point getPos() {
                return pos;
            }

            public boolean isJunction() {
                return this.walls.cardinality() < 2;
            }

            public void setIsActive(boolean active) {
                this.active = active;
            }

            public boolean isActive() {
                return this.active;
            }

            private void controlWalls(boolean state, Wall... walls) {
                for (Wall w : walls) {
                    this.walls.set(w.ordinal(), state);
                }
            }

            public void setWall(Wall... walls) {
                controlWalls(true, walls);
            }

            public void unsetWall(Wall... walls) {
                controlWalls(false, walls);
            }

            public boolean isVisited() {
                return visited;
            }

            public void setVisited(boolean visited) {
                this.hue = MazeSolver.hue;
                MazeSolver.hue += HUE_STEP;
                this.visited = visited;
            }

            public void drawBorder(Graphics2D g, Dimension dim) {
                g.setColor(Color.black);
                this.walls.stream().forEach(i -> {
                    Rectangle r = Wall.values()[i].getRect();
                    g.drawLine((pos.x * dim.width) + (r.x * dim.width) + (-r.x),
                            (pos.y * dim.height) + (r.y * dim.height) + (-r.y),
                            (pos.x * dim.width) + (r.x * dim.width) + (r.width * dim.width) + (-r.x),
                            (pos.y * dim.height) + (r.y * dim.height) + (r.height * dim.height) + (-r.y));
                });
            }

            public void drawBox(Graphics2D g, Dimension dim) {
                if (isVisited()) {
                    g.setColor(Color.getHSBColor(MazeSolver.hue - this.hue, 1f, 0.8f));
                }
                if (isJunction()) {
                    g.setColor(Color.getHSBColor(MazeSolver.hue - this.hue, 0.8f, 0.8f));
                }
                if (isActive()) {
                    g.setColor(Color.getHSBColor(MazeSolver.hue - this.hue, 1f, 1f));
                }
                g.fillRect(pos.x * dim.width, pos.y * dim.height, dim.width, dim.height);
            }

            @Override
            public String toString() {
                return "[(" + pos.x + ":" + pos.y + ") = "
                        + (active ? "" : "in") + "active,"
                        + (visited ? "" : "not ") + "visited]";
            }

        }
    }
}
