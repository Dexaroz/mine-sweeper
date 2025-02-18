package software.ulpgc.MineSweeper.app.Swing;

import software.ulpgc.MineSweeper.arquitecture.io.FileImageLoader;
import software.ulpgc.MineSweeper.arquitecture.model.Cell;
import software.ulpgc.MineSweeper.arquitecture.model.Game;
import software.ulpgc.MineSweeper.arquitecture.view.BoardDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

public class SwingBoardDisplay extends JPanel implements BoardDisplay {
    private Game game;
    private final Map<String, ImageIcon> images;
    private final Map<String, Clicked> eventListeners = new HashMap<>();
    private int row;
    private int col;

    public SwingBoardDisplay(Game game) {
        this.game = game;
        FileImageLoader loader = new FileImageLoader("src/images/");
        this.images = loader.load();
        setDoubleBuffered(true);
        addMouseListener(createMouseListener());
    }

    @Override
    public void show(Game game) {
        this.game = game;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
    }

    private void drawBoard(Graphics g) {
        int cellWidth = getCellWidth();
        int cellHeight = getCellHeight();

        for (int row = 0; row < game.board().rows(); row++) {
            for (int col = 0; col < game.board().columns(); col++) {
                Cell cell = game.board().cells()[row][col];
                Square square = new Square(col * cellWidth + 5, row * cellHeight + 5, cellWidth);
                drawCell(g, cell, square);
            }
        }
    }

    private void drawCell(Graphics g, Cell cell, Square square) {
        ImageIcon icon;

        if (cell.isRevealed()) {
            if (cell.hasMine() && cell == game.board().cells()[row][col]) {
                icon = images.get("mineSelected.png");
            } else if (cell.hasMine()) {
                icon = images.get("mine.png");
            } else {
                String adjacent = String.valueOf(cell.adjacentMines());
                icon = images.getOrDefault(adjacent + ".png", images.get("Revealed.png"));
            }
        } else if (cell.isFlagged()) {
            icon = images.get("flag.png");
        } else {
            icon = images.get("Default.png");
        }

        if (icon != null) {
            g.drawImage(icon.getImage(), square.x(), square.y(), square.length(), square.length(), this);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(square.x(), square.y(), square.length(), square.length());
        }
    }

    private MouseListener createMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                row = toRow(e.getY());
                col = toColumn(e.getX());

                if (row >= 0 && col >= 0 && row < game.board().rows() && col < game.board().columns()) {
                    Clicked leftClick = eventListeners.get("cell-click");
                    Clicked rightClick = eventListeners.get("cell-right-click");

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        leftClick.on(new Point(col, row));
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        rightClick.on(new Point(col, row));
                    }
                }
            }
        };
    }

    private int getCellWidth() {
        return (getWidth() - 10) / game.board().columns();
    }

    private int getCellHeight() {
        return (getHeight() - 10) / game.board().rows();
    }

    private int toRow(int y) {
        return (y - 5) / getCellHeight();
    }

    private int toColumn(int x) {
        return (x - 5) / getCellWidth();
    }

    @Override
    public void showWin() {
        JOptionPane.showMessageDialog(this, "You win!");
    }

    @Override
    public void showLose() {
        this.game = game.revealMines();
    }

    @Override
    public void on(String event, Clicked clicked) {
        eventListeners.put(event, clicked);
    }

    private record Square(int x, int y, int length) {
    }
}