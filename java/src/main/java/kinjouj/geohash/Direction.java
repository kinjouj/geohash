package kinjouj.geohash;

/**
 * kinjouj.getohash.Direction
 * @version 0.1
 * @author kinjouj
 */
public enum Direction {

    TOP(0),
    RIGHT(1),
    BOTTOM(2),
    LEFT(3);

    private int value;

    private Direction(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }
}