package p2p;

import sudoku.Inputs;
import sudoku.Sudoku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class P2PClient {
    private static Socket socket;
    private static InputStream in;
    private static OutputStream out;
    private static Sudoku sudoku;
    private static String myName;
    private static String opponentName;

    public static void main(String[] args) throws IOException {
        connectToServer();
    }

    private static void connectToServer() throws IOException {
        socket = new Socket("127.0.0.1", 50000);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        play();
    }


    private static void play() throws IOException {
        sudoku = new Sudoku();
        myName = Inputs.inputName("Please input your name: ");
        writeMyName();
        System.out.println("Hello " + myName + ".");
        System.out.println("Waiting for your opponent submitting his name ...");
        opponentName = readOpponentSubmitName();
        startGame();
    }

    private static void writeMyName() throws IOException {
        out.write(OpCodes.SUBMIT_NAME);
        byte[] nameBytes = myName.getBytes();
        out.write(nameBytes.length);
        out.write(nameBytes);
    }

    private static void startGame() throws IOException {
        sudoku = readSudokuGameStarted();
        System.out.println("Game Started.");
        int round = 0;

        do {
            System.out.println(sudoku);
            if (round++ % 2 == 0) {
                serverTurn();
            } else {
                yourTurn();
            }
        } while (!sudoku.isSolved());

        readAndAssertOpCode(OpCodes.GAME_OVER);
        System.out.println("Game over :)");
        socket.close();
    }

    private static Sudoku readSudokuGameStarted() throws IOException {
        Sudoku sudoku = new Sudoku();
        readAndAssertOpCode(OpCodes.GAME_STARTED);
        for (int i = 0; i < 80; i += 2) {
            int combination = in.read();
            int frontPart = combination >>> 4;
            frontPart = (frontPart == 10) ? Sudoku.EMPTY : frontPart;
            int behindPart = combination & 0xf;
            behindPart = (behindPart == 10) ? Sudoku.EMPTY : behindPart;
            sudoku.put(i / 9, i % 9, frontPart);
            sudoku.put((i + 1) / 9, (i + 1) % 9, behindPart);
        }
        sudoku.put(8, 8, (byte) in.read());

//        for (int i = 0; i < 81; i++) {
//            byte b = (byte) in.read();
//            sudoku.put(i / 9, i % 9, b);
//        }

        return sudoku;
    }


    private static void yourTurn() throws IOException {
        do {
            try {
                int row = Inputs.inputNumInRange("Row index: ", 0, 8);
                int col = Inputs.inputNumInRange("Column index: ", 0, 8);
                int num = Inputs.inputNumInRange("Please input the number to fill in: ", 1, 9);
                sudoku.put(row, col, num);
                writeFillInNumber(row, col, num);
                System.out.printf("%s put the number %d at (%d, %d).\n\n", myName, num, row, col);
                break;
            } catch (Sudoku.InvalidException err) {
                System.err.println(err.getMessage() + "\n");
                System.out.println(sudoku);
            }
        } while (true);
    }

    private static void serverTurn() throws IOException {
        System.out.println("Waiting for your opponent ...");
        readAndAssertOpCode(OpCodes.FILL_IN_NUMBER);
        byte row = (byte) in.read();
        byte col = (byte) in.read();
        byte num = (byte) in.read();
        sudoku.put(row, col, num);
        System.out.printf("%s put the number %d at (%d, %d).\n\n", opponentName, num, row, col);
    }

    private static void writeFillInNumber(int row, int col, int num) throws IOException {
        out.write(OpCodes.FILL_IN_NUMBER);
        out.write(row);
        out.write(col);
        out.write(num);
    }

    private static String readOpponentSubmitName() throws IOException {
        readAndAssertOpCode(OpCodes.SUBMIT_NAME);
        byte nameLength = (byte) in.read();
        byte[] name = new byte[nameLength];
        for (int i = 0; i < nameLength; i++) {
            name[i] = (byte) in.read();
        }
        return new String(name, StandardCharsets.UTF_8);
    }


    private static byte readAndAssertOpCode(byte... expectedOpCodes) throws IOException {
        byte b = (byte) in.read();
        for (byte expectedOpCode : expectedOpCodes) {
            if (expectedOpCode == b) {
                return b;
            }
        }
        throw new IllegalStateException("OpCode incorrect, given " + b);
    }

}


