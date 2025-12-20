import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class Tetris extends JFrame {
    private static final Color BORDER_COLOR = Color.decode("#D33058");
    private static final Color HEADER_COLOR = Color.decode("#FFC0CB");
    private static final Color BODY_COLOR = Color.decode("#FAEBD7");

    public class StandardBoardRenderer implements BoardRenderer {
        private final int CELL_SIZE = 24;
        private final int COLS = 12;
        private final int ROWS = 20;
        private final int PANEL_W = COLS * CELL_SIZE;
        private final int PANEL_H = ROWS * CELL_SIZE;

        @Override
        public void render(Graphics g, TetrisBoard board) {
            g.setColor(new Color(210, 200, 190));
            for (int x = 0; x <= PANEL_W; x += CELL_SIZE) g.drawLine(x, 0, x, PANEL_H);
            for (int y = 0; y <= PANEL_H; y += CELL_SIZE) g.drawLine(0, y, PANEL_W, y);

            if (board == null) return;

            for (int i = 0; i < ROWS; ++i) {
                for (int j = 0; j < COLS; ++j) {
                    TetrisBoard.Tetrominoes shape = board.shapeAt(j, ROWS - i - 1);
                    if (shape != TetrisBoard.Tetrominoes.NoShape)
                        drawSquare(g, j * CELL_SIZE, i * CELL_SIZE, shape, CELL_SIZE);
                }
            }

            Tetromino curPiece = board.getCurrentPiece(); 
            if (curPiece != null && curPiece.getType() != TetrisBoard.Tetrominoes.NoShape) {
                for (int i = 0; i < 4; i++) {
                    int x = board.getCurX() + curPiece.x(i);
                    int y = board.getCurY() - curPiece.y(i);
                    drawSquare(g, x * CELL_SIZE, (ROWS - y - 1) * CELL_SIZE, curPiece.getType(), CELL_SIZE);
                }
            }
        }

        private void drawSquare(Graphics g, int x, int y, TetrisBoard.Tetrominoes shape, int CELL) {
            Color[] colors = {
                new Color(0,0,0), new Color(204,102,102),
                new Color(102,204,102), new Color(102,102,204),
                new Color(204,204,102), new Color(204,102,204),
                new Color(102,204,204), new Color(218,170,0)
            };
            Color color = colors[shape.ordinal()];
            g.setColor(color);
            g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
            g.setColor(color.brighter());
            g.drawLine(x, y + CELL - 1, x, y);
            g.drawLine(x, y, x + CELL - 1, y);
            g.setColor(color.darker());
            g.drawLine(x + 1, y + CELL - 1, x + CELL - 1, y + CELL - 1);
            g.drawLine(x + CELL - 1, y + CELL - 1, x + CELL - 1, y + 1);
        }
    }

    public Tetris() {
        setTitle("Tetris – Pastel Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BODY_COLOR);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BoardRenderer renderer = new StandardBoardRenderer();
        GamePanel gamePanel = new GamePanel(renderer);

        RoundedPanel side = new RoundedPanel(18, BODY_COLOR, BORDER_COLOR, 2, null);
        side.setPreferredSize(new Dimension(200, 600));
        side.setLayout(null);

        JLabel title = new JLabel("opatetris", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(BORDER_COLOR);
        title.setBounds(10, 10, 180, 40);
        side.add(title);

        JLabel nextLabel = new JLabel("next");
        nextLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nextLabel.setForeground(BORDER_COLOR);
        nextLabel.setBounds(10, 60, 80, 20);
        side.add(nextLabel);

        JPanel previewBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Tetromino next = gamePanel.getBoard().getNextPiece(); 
                if (next != null && next.getType() != TetrisBoard.Tetrominoes.NoShape) {
                    int CELL = 18;
                    int offsetX = (getWidth() - CELL*4)/2;
                    int offsetY = (getHeight() - CELL*4)/2;
                    
                    StandardBoardRenderer r = (StandardBoardRenderer) renderer; 
                   
                    for (int i = 0; i < 4; i++) {
                        int x = 1 + next.x(i);
                        int y = 1 - next.y(i);
                      
                        Color[] colors = {
                            new Color(0,0,0), new Color(204,102,102),
                            new Color(102,204,102), new Color(102,102,204),
                            new Color(204,204,102), new Color(204,102,204),
                            new Color(102,204,204), new Color(218,170,0)
                        };
                        Color color = colors[next.getType().ordinal()];
                        g.setColor(color);
                        g.fillRect(offsetX + x*CELL + 1, offsetY + y*CELL + 1, CELL-2, CELL-2);
                    }
                }
            }
        };
        previewBox.setBackground(BODY_COLOR);
        previewBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        previewBox.setBounds(25, 85, 150, 120);
        side.add(previewBox);

        JLabel scoreLabel = new JLabel("score");
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        scoreLabel.setForeground(BORDER_COLOR);
        scoreLabel.setBounds(10, 220, 80, 20);
        side.add(scoreLabel);

        JLabel scoreValue = new JLabel("0", SwingConstants.CENTER);
        scoreValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        scoreValue.setForeground(BORDER_COLOR);
        scoreValue.setOpaque(true);
        scoreValue.setBackground(Color.WHITE);
        scoreValue.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        scoreValue.setBounds(25, 245, 150, 45);
        side.add(scoreValue);

        JButton startBtn = pastelButton("Start");
        JButton pauseBtn = pastelButton("Pause");
        JButton restartBtn = pastelButton("Restart");
        JButton quitBtn = pastelButton("Quit");

        startBtn.setBounds(40, 320, 120, 40);
        pauseBtn.setBounds(40, 370, 120, 40);
        restartBtn.setBounds(40, 420, 120, 40);
        quitBtn.setBounds(40, 470, 120, 40);

        side.add(startBtn);
        side.add(pauseBtn);
        side.add(restartBtn);
        side.add(quitBtn);

        JLabel hint = new JLabel("Use arrow keys to move/rotate", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(new Color(211, 48, 88));
        hint.setBounds(10, 520, 180, 20);
        side.add(hint);

        startBtn.addActionListener(e -> {
            gamePanel.ensurePlayerName(this);
            gamePanel.startGame();
            previewBox.repaint();
            scoreValue.setText(String.valueOf(gamePanel.getScore()));
            gamePanel.requestFocusInWindow();
        });

        pauseBtn.addActionListener(e -> {
            if (gamePanel.isPaused()) {
                gamePanel.resumeGame();
                pauseBtn.setText("Pause");
            } else {
                gamePanel.pauseGame();
                pauseBtn.setText("Resume");
            }
            gamePanel.requestFocusInWindow();
        });

        restartBtn.addActionListener(e -> {
            gamePanel.restartGame();
            previewBox.repaint();
            scoreValue.setText(String.valueOf(gamePanel.getScore()));
            gamePanel.requestFocusInWindow();
            pauseBtn.setText("Pause");
        });

        quitBtn.addActionListener(e -> showQuitDialog());

        gamePanel.setScoreListener(score -> SwingUtilities.invokeLater(() -> scoreValue.setText(String.valueOf(score))));

        gamePanel.setPreviewRepaintListener(() -> SwingUtilities.invokeLater(previewBox::repaint));

        JPanel leftWrap = new JPanel(new GridBagLayout()); 
        leftWrap.setOpaque(false);
        leftWrap.add(gamePanel);
        root.add(leftWrap, BorderLayout.CENTER);
        root.add(side, BorderLayout.EAST);

        add(root);
    }

    private void showQuitDialog() {
        JDialog dialog = new JDialog(this, "Confirm", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setLayout(null);

        Color red = Color.decode("#D33058");
        Color pink = Color.decode("#F7DAD9");
        Color beige = Color.decode("#FAEBD7");

        JPanel bg = new JPanel(null);
        bg.setBackground(beige);
        bg.setBorder(BorderFactory.createLineBorder(red, 3));
        bg.setBounds(0, 0, 300, 150);
        dialog.add(bg);

        JLabel title = new JLabel("Quit game?", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(red);
        title.setBounds(20, 20, 260, 30);
        bg.add(title);

        JButton yes = new JButton("Yes");
        yes.setFont(new Font("SansSerif", Font.BOLD, 16));
        yes.setForeground(red);
        yes.setBackground(pink);
        yes.setBorder(BorderFactory.createLineBorder(red, 2));
        yes.setFocusPainted(false);
        yes.setBounds(40, 80, 90, 40);
        yes.addActionListener(ev -> System.exit(0));
        bg.add(yes);

        JButton no = new JButton("No");
        no.setFont(new Font("SansSerif", Font.BOLD, 16));
        no.setForeground(red);
        no.setBackground(pink);
        no.setBorder(BorderFactory.createLineBorder(red, 2));
        no.setFocusPainted(false);
        no.setBounds(170, 80, 90, 40);
        no.addActionListener(ev -> dialog.dispose());
        bg.add(no);

        dialog.setVisible(true);
    }

    private JButton pastelButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(HEADER_COLOR);
        b.setForeground(BORDER_COLOR);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        return b;
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        private Color borderColor;
        private int borderThickness;

        public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderThickness, LayoutManager layout) {
            super(layout);
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.borderThickness = borderThickness;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, radius, radius));

            if (borderThickness > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderThickness));
                g2.draw(new RoundRectangle2D.Double(
                        borderThickness / 2.0,
                        borderThickness / 2.0,
                        w - borderThickness,
                        h - borderThickness,
                        radius, radius
                ));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class GamePanel extends RoundedPanel {
        private final int COLS = 12;             
        private final int ROWS = 20;             
        private final int CELL_SIZE = 24;       
        private final int PANEL_W = COLS * CELL_SIZE;
        private final int PANEL_H = ROWS * CELL_SIZE;

        private TetrisBoard board;
        private Timer timer;
        private boolean isStarted, isPaused;
        private int lastScore = -1;
        private String playerName = null;
        private final Leaderboard leaderboard = new Leaderboard(System.getProperty("user.home") + java.io.File.separator + "tetris-leaderboard.txt");

        private final BoardRenderer renderer;

        private java.util.function.IntConsumer scoreListener;
        private Runnable previewRepaintListener;

        public GamePanel(BoardRenderer renderer) {
            super(18, BODY_COLOR, BORDER_COLOR, 3, null);
            this.renderer = renderer;
            
            setPreferredSize(new Dimension(PANEL_W + 8, PANEL_H + 8)); 
            setLayout(null);

            JPanel boardArea = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                   
                    if (renderer != null) {
                        renderer.render(g, board);
                    }
                }
            };
            boardArea.setBounds(4, 4, PANEL_W, PANEL_H);
            boardArea.setBackground(BODY_COLOR);
            add(boardArea);

            setFocusable(true);
            addKeyListener(new InputController());

            board = new TetrisBoard(COLS, ROWS);
        }

        public TetrisBoard getBoard() { return board; }
        public int getScore() { return board.getScore(); }
        public boolean isPaused() { return isPaused; }

        public void setScoreListener(java.util.function.IntConsumer listener) { this.scoreListener = listener; }
        public void setPreviewRepaintListener(Runnable r) { this.previewRepaintListener = r; }

        public void startGame() {
            board.start();
            isStarted = true;
            isPaused = false;
            lastScore = board.getScore();
            if (scoreListener != null) scoreListener.accept(lastScore);
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(400, e -> gameLoop());
            timer.start();
            requestFocusInWindow();
        }

        public void pauseGame() {
            if (!isStarted) return;
            isPaused = true;
            if (timer != null) timer.stop();
        }

        public void resumeGame() {
            if (!isStarted) return;
            isPaused = false;
            if (timer != null) timer.start();
        }

        public void restartGame() {
            if (timer != null) timer.stop();
            board.restart();
            isStarted = true;
            isPaused = false;
            lastScore = board.getScore();
            if (scoreListener != null) scoreListener.accept(lastScore);
            timer = new Timer(400, e -> gameLoop());
            timer.start();
            requestFocusInWindow();
        }

        public void ensurePlayerName(Component parent) {
            if (playerName != null && !playerName.trim().isEmpty()) return;

            JDialog dialog = new JDialog(Tetris.this, "Player Name", true);
            dialog.setSize(330, 180);
            dialog.setLocationRelativeTo(parent);
            dialog.setUndecorated(true);
            dialog.setLayout(null);

            Color red = BORDER_COLOR;
            Color pink = Color.decode("#F7DAD9");
            Color beige = BODY_COLOR;

            JPanel bg = new JPanel(null);
            bg.setBackground(beige);
            bg.setBorder(BorderFactory.createLineBorder(red, 4));
            bg.setBounds(0, 0, 330, 180);
            dialog.add(bg);

            JLabel title = new JLabel("Enter Your Name", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            title.setForeground(red);
            title.setBounds(20, 18, 290, 28);
            bg.add(title);

            JTextField input = new JTextField("Player");
            input.setFont(new Font("SansSerif", Font.PLAIN, 16));
            input.setBackground(Color.WHITE);
            input.setForeground(Color.DARK_GRAY);
            input.setBorder(BorderFactory.createLineBorder(red, 2));
            input.setBounds(30, 60, 270, 32);
            bg.add(input);

            JButton ok = new JButton("OK");
            ok.setFont(new Font("SansSerif", Font.BOLD, 16));
            ok.setForeground(red);
            ok.setBackground(pink);
            ok.setBorder(BorderFactory.createLineBorder(red, 2));
            ok.setFocusPainted(false);
            ok.setBounds(40, 110, 110, 40);
            ok.addActionListener(e -> {
                String name = input.getText();
                if (name == null || name.trim().isEmpty()) name = "Player";
                playerName = name.trim();
                dialog.dispose();
            });
            bg.add(ok);

            JButton cancel = new JButton("Cancel");
            cancel.setFont(new Font("SansSerif", Font.BOLD, 16));
            cancel.setForeground(red);
            cancel.setBackground(pink);
            cancel.setBorder(BorderFactory.createLineBorder(red, 2));
            cancel.setFocusPainted(false);
            cancel.setBounds(180, 110, 110, 40);
            cancel.addActionListener(e -> {
                if (playerName == null || playerName.trim().isEmpty()) playerName = "Player";
                dialog.dispose();
            });
            bg.add(cancel);

            dialog.setVisible(true);
        }

        private void gameLoop() {
            if (isPaused) return;
            board.tick();
            updateScoreIfChanged();
            if (!board.isStarted()) {
                if (timer != null) timer.stop();
                isStarted = false;
                SwingUtilities.invokeLater(() -> showGameOverDialog());
            }
            if (previewRepaintListener != null) previewRepaintListener.run();
            repaint();
        }

        private void updateScoreIfChanged() {
            int s = board.getScore();
            if (s != lastScore) {
                lastScore = s;
                if (scoreListener != null) scoreListener.accept(s);
            }
        }
        
        private void showGameOverDialog() {
             JDialog dialog = new JDialog(Tetris.this, "Game Over", true);
            dialog.setSize(330, 220);
            dialog.setLocationRelativeTo(this);
            dialog.setUndecorated(true); 
            dialog.setLayout(null);

            Color red = Color.decode("#D33058");
            Color pink = Color.decode("#F7DAD9");
            Color beige = Color.decode("#FAEBD7");

            JPanel bg = new JPanel(null);
            bg.setBackground(beige);
            bg.setBorder(BorderFactory.createLineBorder(red, 4));
            bg.setBounds(0, 0, 330, 220);
            dialog.add(bg);

            JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            title.setForeground(red);
            title.setBounds(20, 20, 290, 40);
            bg.add(title);

            JLabel score = new JLabel("Score: " + board.getScore(), SwingConstants.CENTER);
            score.setFont(new Font("SansSerif", Font.PLAIN, 18));
            score.setForeground(red.darker());
            score.setBounds(20, 65, 290, 30);
            bg.add(score);

            JButton restart = new JButton("Restart");
            restart.setFont(new Font("SansSerif", Font.BOLD, 16));
            restart.setForeground(red);
            restart.setBackground(pink);
            restart.setBorder(BorderFactory.createLineBorder(red, 2));
            restart.setFocusPainted(false);
            restart.setBounds(40, 180, 110, 40);
            restart.addActionListener(e -> {
                dialog.dispose();
                restartGame();
            });
            bg.add(restart);

            JButton quit = new JButton("Quit");
            quit.setFont(new Font("SansSerif", Font.BOLD, 16));
            quit.setForeground(red);
            quit.setBackground(pink);
            quit.setBorder(BorderFactory.createLineBorder(red, 2));
            quit.setFocusPainted(false);
            quit.setBounds(180, 180, 110, 40);
            quit.addActionListener(e -> System.exit(0));
            bg.add(quit);

            if (playerName != null && board != null) {
                leaderboard.add(playerName, board.getScore());
            }
            java.util.List<Leaderboard.Entry> top = leaderboard.top(5);
            StringBuilder sb = new StringBuilder("<html>Top 5:<br/>");
            for (int i = 0; i < top.size(); i++) {
                Leaderboard.Entry en = top.get(i);
                sb.append(i + 1).append(". ").append(en.name).append(" (").append(en.score).append(")");
                if (i < top.size() - 1) sb.append("<br/>");
            }
            sb.append("</html>");
            JLabel leaderboardLabel = new JLabel(sb.toString(), SwingConstants.CENTER);
            leaderboardLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            leaderboardLabel.setForeground(red.darker());
            int lines = Math.max(top.size(), 1) + 1; 
            int labelHeight = 18 * lines + 8; 
            leaderboardLabel.setBounds(20, 100, 290, labelHeight);
            bg.add(leaderboardLabel);

            int buttonsY = 100 + labelHeight + 10;
            restart.setBounds(40, buttonsY, 110, 40);
            quit.setBounds(180, buttonsY, 110, 40);
            int neededHeight = buttonsY + 40 + 20;
            if (neededHeight > dialog.getHeight()) {
                dialog.setSize(dialog.getWidth(), neededHeight);
                bg.setBounds(0, 0, dialog.getWidth(), neededHeight);
            }

            dialog.setVisible(true);
        }

        private class InputController extends KeyAdapter {
            public void keyPressed(KeyEvent e) {
                if (!isStarted) return;
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_LEFT:
                        board.moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        board.moveRight();
                        break;
                    case KeyEvent.VK_DOWN:
                        board.rotateRight();
                        break;
                    case KeyEvent.VK_UP:
                        board.rotateLeft();
                        break;
                    case KeyEvent.VK_SPACE:
                        board.dropDown();
                        break;
                    case 'D':
                        board.softDrop();
                        break;
                    default:
                        break;
                }
                repaint();
                updateScoreIfChanged();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tetris t = new Tetris();
            t.setVisible(true);
        });
    }
}