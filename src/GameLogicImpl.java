import javafx.util.Pair;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class GameLogicImpl implements GameLogic {

    private static final String CROSS_TAG = "CROSS";
    private static final int CROSS_TAG_ID = 1;
    private static int crossMoveCount = 0;

    private static final String ZERO_TAG = "ZERO";
    private static final int ZERO_TAG_ID = 2;
    private static int zeroMoveCount = 0;

    private static final int FIELD_CAPACITY = 10;
    private static int countOfPlayers = 0;
    private static String winnerId = null;

    private final Object obj = new Object();

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
            // Если данная клетка игрового поля занята
            if (cellValue != 0) {
                return false;
            }
            if (playerId.equals(CROSS_TAG)) {
                gameField.get(x).set(y, CROSS_TAG_ID);
                System.out.println("Player " + playerId + " made his move on point" + "(" + x + "," + y + ")");
                crossMoveCount++;
                // winnerId = checkForWin(x, y, BLACK_ID) ? BLACK : null;

            } else {
                gameField.get(x).set(y, ZERO_TAG_ID);
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

    private String whoseMove() {
        if (zeroMoveCount + 3 == crossMoveCount) {
            return ZERO_TAG;
        } else {
            return CROSS_TAG;
        }
    }

    public static int getFieldCapacity() {
        return FIELD_CAPACITY;
    }


}
