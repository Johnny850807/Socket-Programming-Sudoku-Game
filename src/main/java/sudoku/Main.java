package sudoku;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Main {
    public static Sudoku sudoku = new Sudoku();

    public static void main(String[] args) {
        System.out.println("Welcome to the sudoku.Sudoku game.");
        String name = Inputs.inputName("Enter your name: ");

        System.out.println("\nHello, " + name + ", let's start a new puzzle.");
        System.out.println("\nGenerating puzzle ...");
        sudoku.generateSolvablePuzzle();
        System.out.println("The puzzle has been generated, the game started!\n");

        while (!sudoku.isSolved()) {
            System.out.println(sudoku);
            int choice = Inputs.inputNumInRange("\n[1] Fill a number [2] See the answer: ", 1 , 2);
            if (choice == 1) {
                fillInNumber();
            } else if (choice == 2) {
                sudoku.generateAnswer();
                System.out.println("You have given up!\nLet's see the answer below.\n");
                System.out.println(sudoku);
                break;
            }
        }
    }

    private static void fillInNumber() {
        do {
            try {
                int row = Inputs.inputNumInRange("Row index: ", 0, 8);
                int col = Inputs.inputNumInRange("Column index: ", 0, 8);
                int num = Inputs.inputNumInRange("Please input the number to fill in: ", 1, 9);
                sudoku.put(row, col, num);
                System.out.printf("You've put the number %d at (%d, %d).\n\n", num, row, col);
                break;
            } catch (Sudoku.InvalidException err) {
                System.err.println(err.getMessage() + "\n");
                System.out.println(sudoku);
            }
        } while (true);
    }
}

