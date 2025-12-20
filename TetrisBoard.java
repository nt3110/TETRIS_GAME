import java.util.Random;

interface Tetromino {
    int x(int index);
    int y(int index);
    int minY();
    Tetromino rotateLeft();
    Tetromino rotateRight();
    TetrisBoard.Tetrominoes getType();
}

class BasicTetromino implements Tetromino {
    protected TetrisBoard.Tetrominoes type;
    protected int[][] coords;

    public BasicTetromino(TetrisBoard.Tetrominoes type, int[][] coords) {
        this.type = type;
        this.coords = coords;
    }

    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }
    public TetrisBoard.Tetrominoes getType() { return type; }
    
    public int minY() {
        int m = coords[0][1];
        for (int i = 0; i < 4; i++) m = Math.min(m, coords[i][1]);
        return m;
    }

    public Tetromino rotateLeft() {
        int[][] newCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            newCoords[i][0] = coords[i][1];
            newCoords[i][1] = -coords[i][0];
        }
        return new BasicTetromino(this.type, newCoords);
    }

    public Tetromino rotateRight() {
        int[][] newCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            newCoords[i][0] = -coords[i][1];
            newCoords[i][1] = coords[i][0];
        }
        return new BasicTetromino(this.type, newCoords);
    }
}

class SquareTetromino extends BasicTetromino {
    public SquareTetromino() {
        super(TetrisBoard.Tetrominoes.SquareShape, new int[][] {{0,0}, {1,0}, {0,1}, {1,1}});
    }

    @Override
    public Tetromino rotateLeft() { return this; } 

    @Override
    public Tetromino rotateRight() { return this; } 
}

class TetrominoFactory {
    private static final int[][][] COORDS_TABLE = new int[][][]{
            {{0,0},{0,0},{0,0},{0,0}}, 
            {{0,-1},{0,0},{-1,0},{-1,1}},
            {{0,-1},{0,0},{1,0},{1,1}},   
            {{0,-1},{0,0},{0,1},{0,2}},  
            {{-1,0},{0,0},{1,0},{0,1}},  
            {{0,0},{1,0},{0,1},{1,1}},    
            {{-1,-1},{0,-1},{0,0},{0,1}},
            {{1,-1},{0,-1},{0,0},{0,1}}   
    };

    public static Tetromino createRandom() {
        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        TetrisBoard.Tetrominoes type = TetrisBoard.Tetrominoes.values()[x];
        
        if (type == TetrisBoard.Tetrominoes.SquareShape) {
            return new SquareTetromino();
        }
        
        int[][] coords = new int[4][2];
        for(int i=0; i<4; i++) {
            coords[i][0] = COORDS_TABLE[type.ordinal()][i][0];
            coords[i][1] = COORDS_TABLE[type.ordinal()][i][1];
        }
        return new BasicTetromino(type, coords);
    }
    
    public static Tetromino createNoShape() {
        return new BasicTetromino(TetrisBoard.Tetrominoes.NoShape, new int[][]{{0,0},{0,0},{0,0},{0,0}});
    }
}

public class TetrisBoard {
    public enum Tetrominoes { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape }

    private final int cols;
    private final int rows;
    private Tetrominoes[] board;
    
    private Tetromino curPiece, nextPiece;
    
    private int curX, curY;
    private boolean isFallingFinished, isStarted, isPaused;
    private int numLinesRemoved;

    public TetrisBoard(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.board = new Tetrominoes[cols * rows];
        clearBoard();
    }

    public int getScore() { return numLinesRemoved; }
    public boolean isPaused() { return isPaused; }
    public boolean isStarted() { return isStarted; }
    public Tetromino getCurrentPiece() { return curPiece; }
    public Tetromino getNextPiece() { return nextPiece; }
    public int getCurX() { return curX; }
    public int getCurY() { return curY; }
    public Tetrominoes shapeAt(int x, int y) { return board[(y * cols) + x]; }

    public void start() {
        clearBoard();
        nextPiece = TetrominoFactory.createRandom(); 
        numLinesRemoved = 0;
        isStarted = true;
        isPaused = false;
        isFallingFinished = false;
        newPiece();
    }

    public void restart() { start(); }
    public void pause() { if (isStarted) isPaused = true; }
    public void resume() { if (isStarted) isPaused = false; }

    public void tick() {
        if (isPaused || !isStarted) return;
        if (isFallingFinished) { isFallingFinished = false; newPiece(); }
        else oneLineDown();
    }

    public void moveLeft() { tryMove(curPiece, curX - 1, curY); }
    public void moveRight() { tryMove(curPiece, curX + 1, curY); }
    
    public void rotateLeft() { tryMove(curPiece.rotateLeft(), curX, curY); }
    public void rotateRight() { tryMove(curPiece.rotateRight(), curX, curY); }
    
    public void dropDown() { int newY = curY; while (tryMove(curPiece, curX, newY - 1)) newY--; pieceDropped(); }
    public void softDrop() { oneLineDown(); }

    private void oneLineDown() { if (!tryMove(curPiece, curX, curY - 1)) pieceDropped(); }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * cols) + x] = curPiece.getType();
        }
        removeFullLines();
        if (!isFallingFinished) newPiece();
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = rows - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < cols; j++) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) { lineIsFull = false; break; }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < rows - 1; k++) {
                    for (int j = 0; j < cols; j++) board[(k * cols) + j] = shapeAt(j, k + 1);
                }
                for (int j = 0; j < cols; j++) board[((rows - 1) * cols) + j] = Tetrominoes.NoShape;
                i++;
            }
        }
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            isFallingFinished = true;
            curPiece = TetrominoFactory.createNoShape(); 
        }
    }

    private void newPiece() {
        curPiece = nextPiece;
        nextPiece = TetrominoFactory.createRandom();
        curX = cols / 2;
        curY = rows - 1 + curPiece.minY();
        if (!tryMove(curPiece, curX, curY)) {
            curPiece = TetrominoFactory.createNoShape();
            isStarted = false;
        }
    }

    private boolean tryMove(Tetromino newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= cols || y < 0 || y >= rows) return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape) return false;
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        return true;
    }

    private void clearBoard() { for (int i = 0; i < cols * rows; i++) board[i] = Tetrominoes.NoShape; }
}