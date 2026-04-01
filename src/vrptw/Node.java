package vrptw;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Node {
    public final int id;
    public final String name;
    public final double x;
    public final double y;
    public final int readyTime;
    public final int dueTime;
    public final int demand;
    public final int serviceTime;
    public final boolean depot;
    public final List<TimeWindow> availabilityWindows;

    public Node(
            int id,
            String name,
            double x,
            double y,
            int readyTime,
            int dueTime,
            int demand,
            int serviceTime,
            boolean depot,
            List<TimeWindow> availabilityWindows) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
        this.demand = demand;
        this.serviceTime = serviceTime;
        this.depot = depot;
        this.availabilityWindows = normalizeWindows(availabilityWindows, readyTime, dueTime);
    }

    private static List<TimeWindow> normalizeWindows(List<TimeWindow> windows, int defaultStart, int defaultEnd) {
        List<TimeWindow> source = (windows == null || windows.isEmpty())
                ? List.of(new TimeWindow(defaultStart, defaultEnd))
                : windows;

        List<TimeWindow> normalized = new ArrayList<>();
        for (TimeWindow tw : source) {
            int start = Math.max(defaultStart, tw.start);
            int end = Math.min(defaultEnd, tw.end);
            if (start <= end) {
                normalized.add(new TimeWindow(start, end));
            }
        }

        if (normalized.isEmpty()) {
            normalized.add(new TimeWindow(defaultStart, defaultEnd));
        }

        normalized.sort(Comparator.comparingInt(tw -> tw.start));
        return List.copyOf(normalized);
    }

    public double earliestFeasibleServiceStart(double arrivalTime) {
        for (TimeWindow tw : availabilityWindows) {
            if (arrivalTime <= tw.end) {
                return Math.max(arrivalTime, tw.start);
            }
        }
        return -1.0;
    }

    public int latestAvailabilityEnd() {
        return availabilityWindows.get(availabilityWindows.size() - 1).end;
    }
}
