
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

@FunctionalInterface
interface Movement {
    // Moves the given point around
    Point move(Point point);
}

// game rules
class Player {
    public char type;

    /*
     * r is right
     * l is left
     * u is up
     * d is down
     */
    static Movement r = (Point a) -> new Point(a.getX() + 1, a.getY());
    static Movement l = (Point a) -> new Point(a.getX() - 1, a.getY());
    static Movement d = (Point a) -> new Point(a.getX(), a.getY() + 1);
    static Movement u = (Point a) -> new Point(a.getX(), a.getY() - 1);

    static Movement downLeft = (Point a) -> new Point(a.getX() - 1, a.getY() + 1);
    static Movement upLeft = (Point a) -> new Point(a.getX() - 1, a.getY() - 1);

    static Movement downRight = (Point a) -> new Point(a.getX() + 1, a.getY() + 1);
    static Movement upRight = (Point a) -> new Point(a.getX() + 1, a.getY() - 1);


    static List<Movement> movements = new ArrayList<Movement>() {
        // list of movements so we can check them using List methods
    };

    public Player(char type) {
        this.type = type;
        movements.clear();
        movements.add(r);
        movements.add(l);
        movements.add(d);
        movements.add(u);
        movements.add(downLeft);
        movements.add(downRight);
        movements.add(upRight);
        movements.add(upLeft);
    }

    public boolean isPointFilled(char[][] table, Point p) {
        return (getType(p, table) == 'O') || (getType(p, table) == 'I');
    }

    public boolean isNotFilled(char[][] table, Point p) {
        return (getType(p, table) == '.');
    }

    public Player whoIsNext(Player currentPlayer, Player blackPlayer, Player whitePlayer) {
        if (currentPlayer == blackPlayer)
            return whitePlayer;
        return blackPlayer;
    }

    public boolean isMoveValid(char[][] table, Point point) {

        if (isNotInTable(point, table) || isPointFilled(table, point))
            return false;

        Point tmp = point;

        for (Movement movingPlayer : movements) {
            if (isChoiceValid(table, point, movingPlayer)) {
                do {
                    setType(table, point);
                    point = movingPlayer.move(point);
                } while (getType(point, table) != type);

                point = tmp;
                return true;
            }
        }
        return false;
    }

    public boolean isPointInTable(char[][] table, Point p) {
        if (p.getY() >= 1 && p.getX() >= 1) {
            return p.getX() <= 8 && p.getY() <= 8;
        }

        return false;
    }

    public boolean isChoiceValid(char[][] table, Point point, Movement movingPoint) {

        char moveType = reverseType();
        point = movingPoint.move(point);
        Point temp = point;
        if (isNotInTable(point, table))
            return false;
        if (getType(point, table) != moveType)
            return false;

        do {
            point = movingPoint.move(point);
        } while (!isNotInTable(point, table) && getType(point, table) == moveType);

        return !isNotInTable(point, table) && getType(point, table) == type;

    }

    public boolean arePointsEqual(Point p1, Point p2) {
        if (p1.getX() == p2.getX()) {
            return p1.getY() == p2.getY();
        }
        return false;
    }

    public char getType(Point point, char[][] table) {
        return table[point.getX() - 1][point.getY() - 1];
    }

    public char reverseType() {
        if (type == 'I') return 'O';
        else return 'I';
    }


    public void setType(char[][] table, Point point) {
        table[point.getX() - 1][point.getY() - 1] = this.type;
    }

    public boolean cantMakeMove(char[][] table) {
        /*
         * checks possible moves for next player if
         * the player can't make a valid move
         * skip that turn
         */

        foreachMovement:
        for (Movement movement : movements) {
            rows:
            for (int i = 1; i <= 8; i++) {
                cols:
                for (int j = 1; j <= 8; j++) {
                    Point point = new Point(i, j);
                    if (isNotFilled(table, point) ) {
                        if (isChoiceValid(table, point, movement))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    public int getCount(char[][] table) {
        int count = 1;
        for (char[] i : table) {
            for (char j : i) {
                if (j == type)
                    count++;
            }
        }
        --count;
        return count;
    }

    public boolean isNotInTable(Point point, char[][] table) {
        return (point.getX() <= 0 || point.getY() <= 0 || point.getX() > table.length || point.getY() > table.length);
    }


}

class Game {
    enum PointStatus {
        EMPTY,
        White,
        Black
    }

    public static void main(String[] args) {

        /*
         * I is White
         * O is Black
         */

        Scanner sc = new Scanner(System.in);
        boolean isGameRunning = true;
        Player Iplayer = new Player('O');
        Player Oplayer = new Player('I');
        Player currentPlayer = Iplayer;
        char[][] table = getTable();

        gameLoop:
        while (isGameRunning) {
            print(table, Oplayer, Iplayer);
            if (Oplayer.getCount(table) == 0 || Iplayer.getCount(table) == 0) {
                isGameRunning = false;
                break gameLoop;
            }

            if (currentPlayer.cantMakeMove(table)) {
                System.out.println("You can't make any move!");
                currentPlayer = changeTurn(currentPlayer, Iplayer, Oplayer);
                continue gameLoop;
            }


            if (currentPlayer.equals(Iplayer))
                System.out.println("Black player It's your turn !  Enter x then y :");
            else
                System.out.println("White player It's your turn !  Enter x then y :");


            int x = 0, y = 0;
            boolean validInput = false;
            Point point;

            do {
                try {
                    y = sc.nextInt();
                    x = sc.nextInt();
                    sc.nextLine();

                    validInput = true;
                } catch (InputMismatchException e) {
                    System.out.println("Your input must be number!");
                    System.out.println("Enter x then y :");
                    sc.nextLine();
                }
            } while (!validInput);

            point = new Point(x, y);
            if (!currentPlayer.isMoveValid(table, point))
                System.out.println("Not a valid move");
            else {
                currentPlayer = changeTurn(currentPlayer, Iplayer, Oplayer);
            }

            if (isTableFull(table)) {
                print(table, Oplayer, Iplayer);
                isGameRunning = false;
                break gameLoop;

                // Ends the game
            }
        }

        // Game is Finished

        if (Iplayer.getCount(table) < Oplayer.getCount(table)) {
            System.out.println("Congrats White Player ! You Won");
        } else
            System.out.println("Congrats Black Player ! You Won");

    }

    public static boolean isTableFull(char[][] table) {
        rows:
        for (int i = 0; i < 8; i++) {
            cols:
            for (int j = 0; j < 8; j++)
                if (table[i][j] == '.')
                    return false;
        }
        return true;
    }

    public static Player changeTurn(Player currentPlayer, Player blackPlayer, Player whitePlayer) {
        if (currentPlayer.equals(whitePlayer)) {
            return blackPlayer;
        }
        return whitePlayer;

    }
    
    private static void setDefaultPositions(char[][] table)
    {
        table[4][4] = 'I';
        table[3][3] = 'I';

        table[4][3] = 'O';
        table[3][4] = 'O';
    }

    public static char[][] getTable() {

        char[][] table = new char[8][8];
        rows:
        for (int i = 0; i < 8; i++) {
            cols:
            for (int j = 0; j < 8; j++)
                table[i][j] = '.';
        }

        setDefaultPositions(table);

        return table;
    }

    static void print(char[][] table,Player white, Player black) {
        /*
         * printing table after movements
         */
        System.out.print(" ");
        for (int i = 1; i < 9; i++) {
            System.out.print(" " + i);
        }
        System.out.println();
        rows:
        for (int i = 0; i < 8; i++) {
            System.out.print(i + 1);
            cols:
            for (int j = 0; j < 8; j++) {
                System.out.print(" " + table[i][j]);
            }
            System.out.println();
        }
        System.out.print("Black : " + black.getCount(table) + "\t" + "White : " + white.getCount(table) + "\n");
    }
}
