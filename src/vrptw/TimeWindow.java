package vrptw;

public class TimeWindow {
    public final int start;
    public final int end;

    public TimeWindow(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Fenetre temporelle invalide: start > end");
        }
        this.start = start;
        this.end = end;
    }
}
