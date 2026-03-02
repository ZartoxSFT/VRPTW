package vrptw;

import java.util.ArrayList;
import java.util.List;

public class VrpInstance {
    public final String name;
    public final int capacity;
    public final Node depot;
    public final List<Node> clients;
    private final double[][] distance;

    public VrpInstance(String name, int capacity, Node depot, List<Node> clients) {
        this.name = name;
        this.capacity = capacity;
        this.depot = depot;
        this.clients = clients;

        List<Node> all = new ArrayList<>();
        all.add(depot);
        all.addAll(clients);

        this.distance = new double[all.size()][all.size()];
        for (int i = 0; i < all.size(); i++) {
            for (int j = 0; j < all.size(); j++) {
                double dx = all.get(i).x - all.get(j).x;
                double dy = all.get(i).y - all.get(j).y;
                this.distance[i][j] = Math.hypot(dx, dy);
            }
        }
    }

    public Node getClient(int clientId) {
        return clients.get(clientId - 1);
    }

    public int clientCount() {
        return clients.size();
    }

    public double distDepotToClient(int clientId) {
        return distance[0][clientId];
    }

    public double distClientToDepot(int clientId) {
        return distance[clientId][0];
    }

    public double distClientToClient(int fromClientId, int toClientId) {
        return distance[fromClientId][toClientId];
    }
}
