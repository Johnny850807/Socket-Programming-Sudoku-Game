package sudoku;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Sudoku implements Serializable {
    private static final long serialVersionUID = 1314520L;
    public final static byte EMPTY = -128;
    private byte[][] board = new byte[9][9];
    private List<Point> puzzledPoints;

    public Sudoku() {
        fillEmptyBoard();
    }

    private void fillEmptyBoard() {
        for (byte[] bytes : board) {
            Arrays.fill(bytes, EMPTY);
        }
    }

    public byte get(int row, int col) {
        return board[row][col];
    }

    public void generateSolvablePuzzle() {
        Random random = new Random();
        boolean canPut;
        boolean solvable;

        do {
            fillEmptyBoard();
            int randomPointsOfPlacement = random.nextInt(25) + 6;
            puzzledPoints = generateRandomDistinctPoints(randomPointsOfPlacement);
            canPut = dfsPutRandomly(puzzledPoints, 0);
            List<Point> emptyPoints = getEmptyPoints();
            solvable = dfsPut(emptyPoints, 0);  // try dfs over the puzzle
            if (solvable) {
                emptyPoints.forEach(p -> board[p.row][p.col] = EMPTY);  // rollback to be unsolved if it's solvable
            }
        } while (!canPut || !solvable);
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private List<Point> generateRandomDistinctPoints(int numPoints) {
        List<Integer> indices = IntStream.range(0, 81).boxed().collect(Collectors.toList());
        Collections.shuffle(indices);
        return indices.stream()
                .map(index -> new Point(index / 9, index % 9))
                .limit(numPoints)
                .collect(Collectors.toList());
    }

    public void setBoard(byte[][] board) {
        byte[][] oldBoard = this.board;
        this.board = board;
        if (!isBoardValid()) {
            this.board = oldBoard;
            throw new BoardInvalidException();
        }
    }

    private boolean isBoardValid() {
        if (board.length != 9) {
            return false;
        }
        for (int i = 0; i < board.length; i++) {
            if (board[i].length != 9) {
                return false;
            }
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != EMPTY) {
                    int num = board[i][j];
                    board[i][j] = EMPTY;  // try re-put every cell to validate the whole board
                    if (isPutViolating(i, j, num)) {
                        return false;
                    }
                    board[i][j] = (byte) num;
                }
            }
        }
        return true;
    }

    public void put(int row, int col, int num) throws InvalidException {
        if (num != EMPTY) {
            validatePut(row, col, num);
            board[row][col] = (byte) num;
        }
    }

    private void validatePut(int row, int col, int num) throws InvalidException {
        if (num < 1 || num > 9) {
            throw new InvalidException(row, col, num);
        } else if (puzzledPoints != null &&
                puzzledPoints.stream().anyMatch(p -> p.row == row && p.col == col)) {
            throw new InvalidException(row, col, num);
        } else {
            byte oldValue = board[row][col];
            board[row][col] = EMPTY;  // replace it with the new value if it's been put
            if (isPutViolating(row, col, num)) {
                board[row][col] = oldValue;
                throw new InvalidException(row, col, num);
            }
        }
    }

    private boolean isPutViolating(final int row, final int col, final int num) throws InvalidException {
        return Arrays.<Supplier<Boolean>>asList(() -> isPutViolatingInRow(row, num),
                () -> isPutViolatingInColumn(col, num),
                () -> isPutViolatingInBox(row, col, num))
                .parallelStream()
                .anyMatch(Supplier::get);
    }

    private boolean isPutViolatingInRow(int row, int num) throws InvalidException {
        for (int i = 0; i < board[row].length; i++) {
            if (!isEmpty(row, i) && board[row][i] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean isPutViolatingInColumn(int col, int num) throws InvalidException {
        for (int i = 0; i < board.length; i++) {
            if (!isEmpty(i, col) && board[i][col] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean isPutViolatingInBox(int row, int col, int num) throws InvalidException {
        int startRow = row / 3 * 3;
        int startCol = col / 3 * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (!isEmpty(i, j) && board[i][j] == num) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEmpty(int row, int col) {
        return board[row][col] == EMPTY;
    }

    public static class Point {
        public int row, col;

        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public void generateAnswer() throws NoSolutionsFoundException {
        if (!dfsPut(getEmptyPoints(), 0)) {
            throw new NoSolutionsFoundException();
        }
    }

    private boolean dfsPutRandomly(List<Point> points, int pos) {
        Point p = points.get(pos);
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Collections.shuffle(numbers);
        for (int num : numbers) {
            if (!isPutViolating(p.row, p.col, num)) {
                board[p.row][p.col] = (byte) num;
                if (pos + 1 >= points.size()) {
                    return true;
                }
                if (dfsPutRandomly(points, pos + 1)) {
                    return true;
                }
                board[p.row][p.col] = EMPTY;
            }
        }
        return false;
    }

    private boolean dfsPut(List<Point> points, int pos) {
        Point p = points.get(pos);
        for (int num = 1; num <= 9; num++) {
            if (!isPutViolating(p.row, p.col, num)) {
                board[p.row][p.col] = (byte) num;
                if (pos + 1 >= points.size()) {
                    return true;
                }
                if (dfsPut(points, pos + 1)) {
                    return true;
                }
                board[p.row][p.col] = EMPTY;
            }
        }
        return false;
    }


    public List<Point> getEmptyPoints() {
        List<Point> emptyPoints = new LinkedList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (isEmpty(i, j)) {
                    emptyPoints.add(new Point(i, j));
                }
            }
        }
        return emptyPoints;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            if (i != 0 && i % 3 == 0) {
                stringBuilder.append("─────────────────────\n");  // Horizontal split line
            }
            for (int j = 0; j < board[i].length; j++) {
                if (j != 0 && j % 3 == 0) {
                    stringBuilder.append("| ");
                }
                if (board[i][j] == EMPTY) {
                    stringBuilder.append('-');
                } else {
                    stringBuilder.append(board[i][j]);
                }
                stringBuilder.append(" ");
            }
            // delete the tailing space
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());

            // replace the tail with the new line if this is not the final row
            if (i != board.length - 1) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public boolean isSolved() {
        return getEmptyPoints().isEmpty();
    }

    public static class SudokuException extends RuntimeException {
        public SudokuException() {
        }

        public SudokuException(String message) {
            super(message);
        }
    }

    public static class BoardInvalidException extends SudokuException {
    }

    public static class NoSolutionsFoundException extends SudokuException {
    }

    public static class InvalidException extends SudokuException {
        InvalidException(int row, int col, int num) {
            super(String.format("The play at (row=%d, col=%d) with the number %d is invalid.", row, col, num));
        }
    }
}

