package vrptw;

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

    public Node(
            int id,
            String name,
            double x,
            double y,
            int readyTime,
            int dueTime,
            int demand,
            int serviceTime,
            boolean depot) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
        this.demand = demand;
        this.serviceTime = serviceTime;
        this.depot = depot;
    }
}
