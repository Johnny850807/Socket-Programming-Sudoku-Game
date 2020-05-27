import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SudokuTest {
    Sudoku sudoku;

    @BeforeEach
    void setup() {
        sudoku = new Sudoku();
    }

    @Test
    void givenDiagonalSudoku_testStringPattern() throws Sudoku.InvalidException {
        givenDiagonalSudoku();
        final String expect = "1 - - | - - - | - - -\n" +
                "- 2 - | - - - | - - -\n" +
                "- - 3 | - - - | - - -\n" +
                "─────────────────────\n" +
                "- - - | 4 - - | - - -\n" +
                "- - - | - 5 - | - - -\n" +
                "- - - | - - 6 | - - -\n" +
                "─────────────────────\n" +
                "- - - | - - - | 7 - -\n" +
                "- - - | - - - | - 8 -\n" +
                "- - - | - - - | - - 9";
        assertEquals(expect, sudoku.toString());
    }

    @Test
    void givenDiagonalSudoku_whenPutDuplicateNumberViolatingTheRow_shouldThrow() throws Sudoku.InvalidException {
        givenDiagonalSudoku();
        for (int i = 0; i < 6; i++) {
            final int temp = i;
            assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(temp, 8, temp + 1));
        }
        for (int i = 6; i < 9; i++) {
            final int temp = i;
            assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(temp, 0, temp + 1));
        }
    }

    @Test
    void givenDiagonalSudoku_whenPutDuplicateNumberViolatingTheColumn_shouldThrow() throws Sudoku.InvalidException {
        givenDiagonalSudoku();
        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(0, 1, 1));
        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(0, 1, 1));
        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(0, 1, 1));
    }


    @Test
    void givenDiagonalSudoku_whenPutDuplicateNumberViolatingTheBox_shouldThrow() throws Sudoku.InvalidException {
        givenDiagonalSudoku();

        // violating the left-top box
        for (int k = 1; k <= 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (sudoku.isEmpty(i, j)) {
                        final int num = k;
                        final int row = i;
                        final int col = j;
                        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(row, col, num),
                                String.format("Should throw when put (row=%d, col=%d) with the number %d", row, col, num));
                    }
                }
            }
        }

        // violating the middle box
        for (int k = 4; k <= 6; k++) {
            for (int i = 3; i < 6; i++) {
                for (int j = 3; j < 6; j++) {
                    if (sudoku.isEmpty(i, j)) {
                        final int num = k;
                        final int row = i;
                        final int col = j;
                        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(row, col, num),
                                String.format("Should throw when put (row=%d, col=%d) with the number %d", row, col, num));
                    }
                }
            }
        }

        // violating the right-bottom box
        for (int k = 7; k <= 9; k++) {
            for (int i = 6; i < 9; i++) {
                for (int j = 6; j < 9; j++) {
                    if (sudoku.isEmpty(i, j)) {
                        final int num = k;
                        final int row = i;
                        final int col = j;
                        assertThrows(Sudoku.InvalidException.class, () -> sudoku.put(row, col, num),
                                String.format("Should throw when put (row=%d, col=%d) with the number %d", row, col, num));
                    }
                }
            }
        }
    }

    void givenDiagonalSudoku() throws Sudoku.InvalidException {
        for (int i = 0; i < 9; i++) {
            sudoku.put(i, i, i + 1);
        }
    }


    @Test
    void givenSolvableSudoku_whenGenerateAnswer_shouldBeValidAndSolved() {
        givenSolvableSudoku();
        sudoku.generateAnswer();
    }


    @Test
    @SuppressWarnings("JavacQuirks")
    void whenSetInvalidBoard_shouldThrow() {
        byte _ = Sudoku.EMPTY;
        byte[][] invalid = {
                {3, _, _, _, _, _, _, _, _},
                {_, _, _, _, _, _, _, _, _},
                {_, _, _, _, 1, _, _, _, _},
                {_, _, _, _, _, _, _, 2, _},
                {_, _, _, _, _, _, _, _, 3},
                {_, _, 4, _, _, _, _, _, _},
                {_, _, _, _, _, _, _, 5, _},
                {_, _, _, _, _, 7, _, _, _},
                {3, _, _, _, _, _, _, _, _}};
        assertThrows(Sudoku.BoardInvalidException.class, () -> sudoku.setBoard(invalid));

        byte[][] invalid2 = {
                {3, _, _, _, _, _, _, _, _},
                {_, _, _, _, _, _, _, _, _},
                {_, _, 3, _, 1, _, _, _, _},
                {_, _, _, _, _, _, _, 2, _},
                {_, _, _, _, _, _, _, _, 3},
                {_, _, 4, _, _, _, _, _, _},
                {_, _, _, _, _, _, _, 5, _},
                {_, _, _, _, _, 7, _, _, _},
                {_, _, _, _, _, _, _, _, _}};
        assertThrows(Sudoku.BoardInvalidException.class, () -> sudoku.setBoard(invalid2));

        byte[][] invalid3 = {
                {3, _, _, _, _, _, _, 3, _},
                {_, _, _, _, _, _, _, _, _},
                {_, _, _, _, 1, _, _, _, _},
                {_, _, _, _, _, _, _, 2, _},
                {_, _, _, _, _, _, _, _, 3},
                {_, _, 4, _, _, _, _, _, _},
                {_, _, _, _, _, _, _, 5, _},
                {_, _, _, _, _, 7, _, _, _},
                {_, _, _, _, _, _, _, _, _}};
        assertThrows(Sudoku.BoardInvalidException.class, () -> sudoku.setBoard(invalid3));
    }

    @SuppressWarnings("JavacQuirks")
    void givenSolvableSudoku() {
        byte _ = Sudoku.EMPTY;
        byte[][] solvable = {
                {5, 3, _, _, 7, _, _, _, _},
                {6, _, _, 1, 9, 5, _, _, _},
                {_, 9, 8, _, _, _, _, 6, _},
                {8, _, _, _, 6, _, _, _, 3},
                {4, _, _, 8, _, 3, _, _, 1},
                {7, _, _, _, 2, _, _, _, 6},
                {_, 6, _, _, _, _, 2, 8, _},
                {_, _, _, 4, 1, 9, _, _, 5},
                {_, _, _, _, 8, _, _, 7, 9}};

        sudoku.setBoard(solvable);
    }


    @Test
    void canGenerateSolvablePuzzle() {
        sudoku.generateSolvablePuzzle();
        sudoku.generateAnswer();
    }
}