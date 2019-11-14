import javafx.util.Pair;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class GameLogicImpl implements GameLogic {

    private static final String CROSS_TAG = "CROSS";
    private static final int CROSS_TAG_ID = 1;
    private static final int CROSS_KILLED_ZERO = 12;
    private static int crossMoveCount = 0;

    private static final String ZERO_TAG = "ZERO";
    private static final int ZERO_TAG_ID = 2;
    private static final int ZERO_KILLED_CROSS = 21;
    private static int zeroMoveCount = 0;

    private static final int FIELD_CAPACITY = 10;
    private static int countOfPlayers = 0;
    private static String winnerId = null;

    private final Object obj = new Object();
    private static boolean checkCrossInitialMove = false;
    private static List<List<Integer>> gameField = new ArrayList<>(FIELD_CAPACITY);


    public GameLogicImpl() {
        for (int i = 0; i < FIELD_CAPACITY; i++) {
            List<Integer> tmp = new ArrayList<>();
            for (int j = 0; j < FIELD_CAPACITY; j++) {
                tmp.add(0);
            }
            gameField.add(tmp);
        }
    }


    @Override
    public String getId() throws RemoteException {
        String result;
        switch (countOfPlayers++) {
            case 0:
                result = CROSS_TAG;
                break;
            case 1:
                result = ZERO_TAG;
                break;
            default:
                return null;
        }
        System.out.printf("Player %s connected to the game! \n", result);
        if (result.equals(CROSS_TAG)) {
            while (countOfPlayers != 2) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        } else if (result.equals(ZERO_TAG)) {
            while (checkCrossInitialMove == false) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        System.out.println("The game started!");
        return result;
    }

    @Override
    public List getField() throws RemoteException {
        return gameField;
    }

    @Override
    public boolean makeMove(Pair<Integer, Integer> point, String playerId) throws RemoteException {
        synchronized (obj) {
            int x = point.getKey();
            int y = point.getValue();
            // Если выходим за границы игрового поля
            if (x < 0 || x > 9 || y < 0 || y > 9) {
                return false;
            }

            Integer cellValue = gameField.get(x).get(y);

            if (!checkNeighbours(x, y, playerId)) {
                return false;
            }
            if (playerId.equals(CROSS_TAG)) {
                // Если клетка занята крестиком
                if (cellValue == CROSS_TAG_ID) {
                    return false;
                } else if (cellValue == 0) {
                    // Ход в доступную клетку
                    gameField.get(x).set(y, CROSS_TAG_ID);
                } else if (wantToKill(x, y, playerId)) {
                    // Убийство нолика
                    gameField.get(x).set(y, CROSS_KILLED_ZERO);
                    System.out.println("CROSS KILLED ZERO");
                }
                System.out.println("Player " + playerId + " made his move on point" + "(" + x + "," + y + ")");
                crossMoveCount++;
                if (crossMoveCount == 3) {
                    checkCrossInitialMove = true;
                }
                // winnerId = checkForWin(x, y, BLACK_ID) ? BLACK : null;

            } else if (playerId.equals(ZERO_TAG)) {
                if (cellValue == ZERO_TAG_ID) {
                    return false;
                } else if (cellValue == 0) {
                    // Ход в доступную клетку
                    gameField.get(x).set(y, ZERO_TAG_ID);
                } else if (wantToKill(x, y, playerId)) {
                    // Убийство крестика
                    gameField.get(x).set(y, ZERO_KILLED_CROSS);
                    System.out.println("ZERO KILLED CROSS!");
                }
                System.out.println("Player " + playerId + " made his move on point" + "(" + x + "," + y + ")");
                zeroMoveCount++;
            }
            return true;
        }
    }

    @Override
    public List<List<Integer>> waitForOpponent(String playerId) throws RemoteException {
        while ((!playerId.equals(whoseMove()) && winnerId == null)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return gameField;
    }

    @Override
    public String whoWon(String playerId) throws RemoteException {
        if (winnerId != null) {
            System.out.println("Player " + playerId + " won!");
        }
        return winnerId;
    }


    private boolean checkNeighbours(int x, int y, String playerId) {
        // Нам не нужно проверять соседей для первых ходов
        if (playerId.equals(CROSS_TAG) && crossMoveCount == 0) {
            return true;
        }
        if (playerId.equals(ZERO_TAG) && zeroMoveCount == 0) {
            return true;
        }

        int right = x + 1;
        int left = x - 1;
        int top = y - 1;
        int bottom = y + 1;

        if (right == 10) {
            right--;
        }
        if (left == -1) {
            left++;
        }
        if (top == -1) {
            top++;
        }
        if (bottom == 10) {
            bottom--;
        }
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                if (playerId.equals(CROSS_TAG)) {
                    if (i != x || j != y) {
                        // System.out.println("Value [" + i + "][" + j + "] = " + gameField.get(i).get(j));
                        if (gameField.get(i).get(j) == CROSS_TAG_ID) {
                            return true;
                        }
                    }
                } else {
                    if (i != x || j != y) {
                        if (gameField.get(i).get(j) == ZERO_TAG_ID) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean wantToKill(int x, int y, String playerId) {
        int right = x + 1;
        int left = x - 1;
        int top = y - 1;
        int bottom = y + 1;
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                if (playerId.equals(CROSS_TAG)) {
                    if (i != x || j != y) {
                        if (gameField.get(i).get(j) == ZERO_TAG_ID) {
                            return true;
                        }
                    }
                } else {
                    if (i != x || j != y) {
                        if (gameField.get(i).get(j) == CROSS_TAG_ID) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String whoseMove() {
        if ((zeroMoveCount % 3 == 0 || crossMoveCount % 3 == 0) && (crossMoveCount / 3 > zeroMoveCount / 3)) {
            return ZERO_TAG;
        } else {
            return CROSS_TAG;
        }

    }

    public static int getFieldCapacity() {
        return FIELD_CAPACITY;
    }
}