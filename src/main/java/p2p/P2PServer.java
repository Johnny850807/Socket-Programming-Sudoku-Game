package p2p;

import sudoku.Inputs;
import sudoku.Sudoku;
import sudoku.Sudoku.Point;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("Duplicates")
public class P2PServer {
    private static ServerSocket server;
    private static Socket client;
    private static InputStream in;
    private static BufferedOutputStream out;
    private static String myName;
    private static String opponentName;
    private static Sudoku sudoku;

    public static void main(String[] args) throws IOException {
        server = new ServerSocket(50000);
        acceptClient();
    }

    private static void acceptClient() throws IOException {
        try {
            client = server.accept();
            in = client.getInputStream();
            out = new BufferedOutputStream(client.getOutputStream());
            play();
        } catch (IOException | IllegalStateException err) {
            client.close();
            acceptClient(); // serve the next client
        }
    }

    private static void play() throws IOException {
        sudoku = new Sudoku();
        myName = Inputs.inputName("Please input your name: ");
        System.out.println("Hello " + myName + ".");
        System.out.println("Waiting for your opponent submitting his name ...");
        writeMyName();
        opponentName = readOpponentSubmitName();
        startGame();
    }

    private static void writeMyName() throws IOException {
        out.write(OpCodes.SUBMIT_NAME);
        byte[] nameBytes = myName.getBytes();
        out.write(nameBytes.length);
        out.write(nameBytes);
        out.flush();
    }

    private static void startGame() throws IOException {
        System.out.println("Generating the puzzle ... ");
        sudoku.generateSolvablePuzzle();
        writeGameStarted();
        System.out.println("Game Started.");

        int round = 0;
        do {
            System.out.println(sudoku);
            if (round++ % 2 == 0) {  // server's turn
                yourTurn();
            } else { // client's turn
                clientTurn();
            }
        } while (!sudoku.isSolved());

        out.write(OpCodes.GAME_OVER);
        client.close();
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

    private static void clientTurn() throws IOException {
        System.out.println("Waiting for your opponent ...");
        readAndAssertOpCode(OpCodes.FILL_IN_NUMBER);
        byte row = (byte) in.read();
        byte col = (byte) in.read();
        byte num = (byte) in.read();
        sudoku.put(row, col, num);
        System.out.printf("%s put the number %d at (%d, %d).\n\n", opponentName, num, row, col);
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

    private static void writeGameStarted() throws IOException {
        out.write(OpCodes.GAME_STARTED);
        List<Point> puzzledPoints = sudoku.getPuzzledPoints();
        out.write(puzzledPoints.size());
        for (int i = 0; i < puzzledPoints.size(); i ++) {
            Point p = puzzledPoints.get(i);
            byte b = (byte) (p.row << 4);  // row and col share one byte
            b |= p.col;
            out.write(b);
        }
        for (int i = 0; i < puzzledPoints.size(); i+=2) {
            Point p = puzzledPoints.get(i);
            byte b = (byte) (sudoku.get(p.row, p.col) << 4);  // two numbers share one byte
            if (i+1 < puzzledPoints.size()) {
                Point p2 = puzzledPoints.get(i+1);
                b |= sudoku.get(p2.row, p2.col);
            }
            out.write(b);
        }
        out.flush();
    }


    private static void readAndAssertOpCode(byte expectedOpCode) throws IOException {
        byte nextOp = (byte) in.read();
        if (expectedOpCode != nextOp) {
            throw new IllegalStateException("OpCode incorrect.");
        }
    }

    private static void writeFillInNumber(int row, int col, int num) throws IOException {
        out.write(OpCodes.FILL_IN_NUMBER);
        out.write(row);
        out.write(col);
        out.write(num);
        out.flush();
    }

}
