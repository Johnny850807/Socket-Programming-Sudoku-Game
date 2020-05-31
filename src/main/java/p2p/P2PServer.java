package p2p;

import helpers.Countdown;
import helpers.CountdownAlarm;
import helpers.ExceptionalRunnable;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("Duplicates")
public class P2PServer {
    private static final int HOST_PLAYER_NUMBER = 1;
    private static final int CLIENT_PLAYER_NUMBER = 2;
    private static ServerSocket server;
    private static Socket client;
    private static InputStream in;
    private static BufferedOutputStream out;
    private static String myName;
    private static String opponentName;
    private static Sudoku sudoku;
    private static CountdownAlarm countdownAlarm;

    public static void main(String[] args) throws IOException {
        server = new ServerSocket(50000);
        countdownAlarm = new CountdownAlarm(TimeUnit.MINUTES.toMillis(1));
        acceptClient();
    }

    private static void acceptClient() throws IOException {
        try {
            client = server.accept();
            in = client.getInputStream();
            out = new BufferedOutputStream(client.getOutputStream());
            play();
        } catch (IOException | RuntimeException err) {
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
                countdownTurn(P2PServer::myTurn, HOST_PLAYER_NUMBER);
            } else { // client's turn
                countdownTurn(P2PServer::clientTurn, CLIENT_PLAYER_NUMBER);
            }
        } while (!countdownAlarm.timeExpires() && !sudoku.isSolved());

        out.write(OpCodes.GAME_OVER);
        out.flush();
        client.close();
    }

    private static void countdownTurn(ExceptionalRunnable turnRunnable,
                                      int playerNumber) {
        Thread turnThread = new Thread(() -> {
            try {
                // run the turn asynchronously as a thread and so it can be preempted
                turnRunnable.run();
                countdownAlarm.stopCountdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        turnThread.start();

        Countdown countdown = countdownAlarm.countdown(() -> {
            turnThread.interrupt();  // stop (preempt) the turn
            try {
                System.out.printf("Time's out, %s win!\n",
                        playerNumber == HOST_PLAYER_NUMBER ? opponentName : myName);
                writeTimeout(playerNumber);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        countdown.await();
    }

    private static void myTurn() throws IOException {
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
        for (int i = 0; i < puzzledPoints.size(); i++) {
            Point p = puzzledPoints.get(i);
            byte b = (byte) (p.row << 4);  // row and col share one byte
            b |= p.col;
            out.write(b);
        }
        for (int i = 0; i < puzzledPoints.size(); i += 2) {
            Point p = puzzledPoints.get(i);
            byte b = (byte) (sudoku.get(p.row, p.col) << 4);  // two numbers share one byte
            if (i + 1 < puzzledPoints.size()) {
                Point p2 = puzzledPoints.get(i + 1);
                b |= sudoku.get(p2.row, p2.col);
            }
            out.write(b);
        }
        out.flush();
    }

    private static void writeTimeout(int loserPlayerNumber) throws IOException {
        out.write(OpCodes.TIME_OUT);
        out.write(loserPlayerNumber);
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
