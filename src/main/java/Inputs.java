import java.util.Scanner;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class Inputs {
    private final static Scanner scanner = new Scanner(System.in);

    public static String inputName(String msg) {
        System.out.println(msg);
        String next = scanner.next();
        return next.isEmpty() ? inputName(msg) : next;
    }

    public static int inputNumInRange(String msg, int floor, int ceil) {
        System.out.println(msg);
        try {
            int next = scanner.nextInt();
            if (next < floor || next > ceil) {
                System.err.printf("Please input a number in %d ~ %d\n.", floor, ceil);
                return inputNumInRange(msg, floor, ceil);
            }
            return next;
        } catch (NumberFormatException ignored) {
            System.err.printf("Please input a number in %d ~ %d\n.", floor, ceil);
            return inputNumInRange(msg, floor, ceil);
        }
    }
}
