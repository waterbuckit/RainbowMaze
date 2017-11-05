/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mazesolver;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author waterbucket
 */
public class MazeSolver {

    private static float hue;

    private final int WIDTH = 50;
    private final int HEIGHT = 50;
    private final JFrame frame;
    private static Maze maze;
    private static MazePanel panel;
    private final static int SCALEFACTOR = 15;

    public MazeSolver() {
        this.hue = 0;
        this.frame = new JFrame();
        MazeSolver.maze = new Maze(this.WIDTH, this.HEIGHT);
        MazeSolver.panel = new MazePanel();
        this.frame.setTitle("MazeGenerator");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MazeSolver.panel.setBackground(Color.LIGHT_GRAY);
        this.frame.add(MazeSolver.panel);
        this.frame.pack();
    }

    public static void main(String[] args) {
        MazeSolver ms = new MazeSolver();
        ms.frame.setVisible(true);
        MazeSolver.maze.startGeneration();
    }

    class MazePanel extends JPanel {

        public MazePanel() {
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            /*
            Will draw the maze based on its index within the 2D array
             */
            for (int y = 0; y < MazeSolver.maze.cells.length; y++) {
                for (int x = 0; x < MazeSolver.maze.cells.length; x++) {
                    maze.cells[x][y].draw(g2d, x, y);
                    MazeSolver.hue += 0.1;
                }
            }
        }
    }

    class Maze {
        
        private Cell[][] cells;
        private Cell currentCell;

        public Maze(int width, int height) {
            this.cells = new Cell[width][height];
            this.setUpCells();
        }

        /**
         * Set all walls to zero
         */
        private void setUpCells() {
            for (int y = 0; y < cells.length; y++) {
                for (int x = 0; x < cells.length; x++) {
                    this.cells[x][y] = new Cell(x, y);
                    MazeSolver.hue += 0.1;
                    boolean[] wallsTemp = {true, true, true, true};
                    this.cells[x][y].setWalls(wallsTemp);
                }
            }
        }

        public void startGeneration() {
            int initialX = (int) (Math.random() * this.cells.length - 1);
            int initialY = (int) (Math.random() * this.cells.length - 1);
            LinkedList<Cell> stack = new LinkedList<>();
            this.currentCell = this.cells[initialX][initialY];
            this.currentCell.setVisited(true);
            this.currentCell.setIsActive(true);
            stack.add(currentCell);
            while (true) {
                Cell next = this.currentCell.getNeighbours();
                if (next != null) {
                    next.setVisited(true);
                    this.removeWall(next);
                    this.checkIsJunction();
                    this.currentCell.setIsActive(false);
                    this.currentCell = next;
                    this.currentCell.setIsActive(true);
                    stack.add(this.currentCell);
                } else {
                    this.currentCell.setIsActive(false);
                    this.currentCell = stack.pop();
                    if(stack.isEmpty()){
                        break;
                    }
                    this.currentCell.setIsActive(true);
                }
                try {
                    Thread.sleep(75);
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted!");
                }
                MazeSolver.panel.repaint();
                MazeSolver.panel.revalidate();
            }
        }

        private void removeWall(Cell next) {
            if (next.getI() > this.currentCell.getI()) {
                this.currentCell.walls[1] = false;
                next.walls[3] = false;
            } else if (next.getI() < this.currentCell.getI()) {
                this.currentCell.walls[3] = false;
                next.walls[1] = false;
            } else if (next.getJ() > this.currentCell.getJ()) {
                this.currentCell.walls[2] = false;
                next.walls[0] = false;
            } else if (next.getJ() < this.currentCell.getJ()) {
                this.currentCell.walls[0] = false;
                next.walls[2] = true;
            }
        }

        private void checkIsJunction() {
            int sum = 0;
            for(int i = 0; i < this.currentCell.walls.length; i++){
                if(this.currentCell.walls[i] == false){
                    sum++;
                }
            }
            if(sum > 2){
                this.currentCell.setIsJunction(true);
            }
        }

        class Cell {

            private final int i;
            private final int j;
            public boolean[] walls;
            private boolean visited;
            private boolean isActive;
            private boolean isJunction;

            public Cell(int i, int j) {
                this.i = i;
                this.j = j;
                this.visited = false;
                // walls are up, right, down, left
                this.walls = new boolean[4];
                this.isActive = false;
                this.isJunction = false;
            } 

            public int getI() {
                return this.i;
            }

            public int getJ() {
                return this.j;
            }

            public boolean[] getWalls() {
                return walls;
            }

            public void setIsJunction(boolean junction) {
                this.isJunction = junction;
            }

            public boolean isJunction() {
                return this.isJunction;
            }

            public void setIsActive(boolean active) {
                this.isActive = active;
            }

            public boolean isActive() {
                return this.isActive;
            }

            public void setWalls(boolean[] walls) {
                this.walls = walls;
            }

            public boolean isVisited() {
                return visited;
            }

            public void setVisited(boolean visited) {
                this.visited = visited;
            }

            public void draw(Graphics2D g2d, int x, int y) {
                
                if (isVisited()) {
                    g2d.setColor(Color.getHSBColor(MazeSolver.hue, 1f, 1f));
                    g2d.fillRect(x * SCALEFACTOR, y * SCALEFACTOR,
                            SCALEFACTOR, SCALEFACTOR);
                }
                if (isActive()) {
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x * SCALEFACTOR, y * SCALEFACTOR, SCALEFACTOR,
                            SCALEFACTOR);
                }
//                if (isJunction()) {
//                    g2d.setColor(Color.red);
//                    g2d.fillRect(x * SCALEFACTOR, y * SCALEFACTOR, SCALEFACTOR,
//                            SCALEFACTOR);
//                }
                g2d.setColor(Color.BLACK);
                if (this.walls[0]) {
                    g2d.drawLine(x * SCALEFACTOR, y * SCALEFACTOR,
                            (x * SCALEFACTOR) + SCALEFACTOR, y * SCALEFACTOR);
                }
                if (this.walls[1]) {
                    g2d.drawLine((x * SCALEFACTOR) + SCALEFACTOR,
                            y * SCALEFACTOR, (x * SCALEFACTOR) + SCALEFACTOR,
                            (y * SCALEFACTOR) + SCALEFACTOR);
                }
                if (this.walls[2]) {
                    g2d.drawLine(x * SCALEFACTOR,
                            (y * SCALEFACTOR) + SCALEFACTOR,
                            (x * SCALEFACTOR) + SCALEFACTOR,
                            (y * SCALEFACTOR) + SCALEFACTOR);
                }
                if (this.walls[3]) {
                    g2d.drawLine(x * SCALEFACTOR, y * SCALEFACTOR,
                            x * SCALEFACTOR, (y * SCALEFACTOR) + SCALEFACTOR);
                }
            }

            @Override
            public String toString() {
                return this.i + " " + this.j;
            }

            /**
             * checks the neighbours surrounding the cell, returns false if
             * there are any neighbours that have not been visited.
             *
             * @return
             */
            private Cell getNeighbours() {
                ArrayList<Cell> neighbours = new ArrayList<>();
                if (this.j - 1 >= 0) {
                    if (!MazeSolver.maze.cells[this.i][this.j - 1].isVisited()) {
                        // top middle neighbour
                        neighbours.add(MazeSolver.maze.cells[this.i][this.j - 1]);
                    }
                }
                if (this.i - 1 >= 0) {
                    if (!MazeSolver.maze.cells[this.i - 1][this.j].isVisited()) {
                        // middle left neighbour
                        neighbours.add(MazeSolver.maze.cells[this.i - 1][this.j]);
                    }
                }
                if (this.i + 1 < MazeSolver.maze.cells.length) {
                    if (!MazeSolver.maze.cells[this.i + 1][this.j].isVisited()) {
                        // middle right neighbour
                        neighbours.add(MazeSolver.maze.cells[this.i + 1][this.j]);
                    }
                }
                if (this.j + 1 < MazeSolver.maze.cells.length) {
                    if (!MazeSolver.maze.cells[this.i][this.j + 1].isVisited()) {
                        // bottom middle neighbour
                        neighbours.add(MazeSolver.maze.cells[this.i][this.j + 1]);
                    }
                }
                System.out.println(neighbours.toString());
                // get a random element
                if (!neighbours.isEmpty()) {
                    return neighbours.get((int) (Math.random() * neighbours.size()));
                } else {
                    return null;
                }
            }
        }
    }
}
