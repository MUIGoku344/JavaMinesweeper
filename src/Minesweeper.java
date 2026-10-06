import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Minesweeper {

    private int gridSize = 8;
    private int numMines = 10;

    private int revealedCount = 0;
    private boolean gameOver = false;

    private final JFrame frame;
    private final JPanel boardPanel;
    private final JComboBox<String> difficultyBox;
    private final JLabel timerLabel;
    private final JLabel mineLabel;
    private final Timer timer;

    private JButton[][] buttons;
    private boolean[][] mines;
    private int[][] neighborCounts;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int secondsElapsed = 0;
    private int flagCount = 0;

    public Minesweeper() {
        frame = new JFrame("Minesweeper");
        boardPanel = new JPanel();

        JButton restartButton = new JButton("New Game");

        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyBox = new JComboBox<>(difficulties);

        timerLabel = new JLabel("Time: 0");
        mineLabel = new JLabel("Mines: " + numMines);

        timer = new Timer(1000, e -> {
            secondsElapsed++;
            timerLabel.setText("Time: " + secondsElapsed);
        });

        JPanel topPanel = new JPanel();

        topPanel.add(new JLabel("Difficulty:"));
        topPanel.add(difficultyBox);
        topPanel.add(restartButton);
        topPanel.add(mineLabel);
        topPanel.add(timerLabel);

        restartButton.addActionListener(e -> startNewGame());

        difficultyBox.addActionListener(e -> {
            setDifficulty();
            startNewGame();
        });

        frame.setSize(650, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);

        startNewGame();

        frame.setVisible(true);
    }

    private void setDifficulty() {
        String difficulty = (String) difficultyBox.getSelectedItem();

        if ("Easy".equals(difficulty)) {
            gridSize = 8;
            numMines = 10;
        } else if ("Medium".equals(difficulty)) {
            gridSize = 12;
            numMines = 25;
        } else if ("Hard".equals(difficulty)) {
            gridSize = 16;
            numMines = 40;
        }
    }

    private void startNewGame() {
        gameOver = false;
        revealedCount = 0;
        secondsElapsed = 0;
        flagCount = 0;

        timer.stop();

        timerLabel.setText("Time: 0");
        mineLabel.setText("Mines: " + numMines);

        timer.start();

        buttons = new JButton[gridSize][gridSize];
        mines = new boolean[gridSize][gridSize];
        neighborCounts = new int[gridSize][gridSize];
        revealed = new boolean[gridSize][gridSize];
        flagged = new boolean[gridSize][gridSize];

        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(gridSize, gridSize));

        createBoard();
        placeMines();
        calculateNeighborCounts();

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void createBoard() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {

                JButton button = new JButton();

                button.setMargin(new Insets(0, 0, 0, 0));
                button.setFont(new Font("Arial", Font.BOLD, 14));

                int currentRow = row;
                int currentCol = col;

                button.addActionListener(
                        e -> revealCell(currentRow, currentCol)
                );

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(currentRow, currentCol);
                        }
                    }
                });

                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }
    }

    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;

        while (minesPlaced < numMines) {
            int row = random.nextInt(gridSize);
            int col = random.nextInt(gridSize);

            if (!mines[row][col]) {
                mines[row][col] = true;
                minesPlaced++;
            }
        }
    }

    private void calculateNeighborCounts() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {

                if (!mines[row][col]) {
                    neighborCounts[row][col] =
                            countNearbyMines(row, col);
                }
            }
        }
    }

    private int countNearbyMines(int row, int col) {
        int count = 0;

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {

                int newRow = row + rowOffset;
                int newCol = col + colOffset;

                if (newRow >= 0 && newRow < gridSize
                        && newCol >= 0 && newCol < gridSize
                        && mines[newRow][newCol]) {

                    count++;
                }
            }
        }

        return count;
    }

    private void revealCell(int row, int col) {
        if (gameOver) {
            return;
        }

        if (flagged[row][col]) {
            return;
        }

        if (revealed[row][col]) {
            return;
        }

        revealed[row][col] = true;
        revealedCount++;

        JButton button = buttons[row][col];

        button.setBackground(Color.WHITE);
        button.setOpaque(true);

        if (mines[row][col]) {
            gameOver = true;
            timer.stop();
            revealAllMines();

            JOptionPane.showMessageDialog(
                    frame,
                    "Game Over!"
            );

            return;
        }

        int count = neighborCounts[row][col];

        if (count > 0) {
            button.setText(String.valueOf(count));
            setNumberColor(button, count);

            checkWin();
            return;
        }


        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {

                int newRow = row + rowOffset;
                int newCol = col + colOffset;

                if (newRow >= 0 && newRow < gridSize
                        && newCol >= 0 && newCol < gridSize
                        && !revealed[newRow][newCol]) {

                    revealCell(newRow, newCol);
                }
            }
        }

        checkWin();
    }

    private void toggleFlag(int row, int col) {
        if (gameOver) {
            return;
        }

        if (revealed[row][col]) {
            return;
        }

        flagged[row][col] = !flagged[row][col];

        if (flagged[row][col]) {
            buttons[row][col].setText("F");
            flagCount++;
        } else {
            buttons[row][col].setText("");
            flagCount--;
        }

        mineLabel.setText("Mines: " + (numMines - flagCount));
    }

    private void revealAllMines() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {

                if (mines[row][col]) {
                    buttons[row][col].setText("X");
                }

                buttons[row][col].setEnabled(false);
            }
        }
    }

    private void checkWin() {
        int safeCells = (gridSize * gridSize) - numMines;

        if (!gameOver && revealedCount == safeCells) {
            gameOver = true;
            timer.stop();

            JOptionPane.showMessageDialog(
                    frame,
                    "You Win!"
            );
        }
    }

    private void setNumberColor(JButton button, int count) {
        switch (count) {
            case 1 -> button.setForeground(Color.BLUE);
            case 2 -> button.setForeground(new Color(0, 128, 0));
            case 3 -> button.setForeground(Color.RED);
            case 4 -> button.setForeground(new Color(0, 0, 128));
            case 5 -> button.setForeground(new Color(128, 0, 0));
            case 6 -> button.setForeground(new Color(0, 128, 128));
            case 7 -> button.setForeground(Color.BLACK);
            case 8 -> button.setForeground(Color.DARK_GRAY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}