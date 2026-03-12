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

    /**
     * Calcule une borne inférieure du nombre de véhicules nécessaires
     * basée uniquement sur la capacité (sans considérer les fenêtres temporelles).
     */
    public int minVehiclesCapacityBound() {
        int totalDemand = 0;
        for (Node client : clients) {
            totalDemand += client.demand;
        }
        return (int) Math.ceil((double) totalDemand / capacity);
    }

    /**
     * Affiche des statistiques sur l'instance pour aider à comprendre
     * les contraintes (utile pour documenter les instances dans le rapport).
     */
    public String getStatistics() {
        int totalDemand = 0;
        int minReady = Integer.MAX_VALUE;
        int maxDue = Integer.MIN_VALUE;
        for (Node client : clients) {
            totalDemand += client.demand;
            minReady = Math.min(minReady, client.readyTime);
            maxDue = Math.max(maxDue, client.dueTime);
        }

        int minVehicles = minVehiclesCapacityBound();

        return String.format(
                "Instance: %s\n" +
                        "  Clients: %d\n" +
                        "  Capacité véhicule: %d\n" +
                        "  Demande totale: %d\n" +
                        "  Min véhicules (capacité): %d\n" +
                        "  Fenêtre dépôt: [%d, %d]\n" +
                        "  Fenêtres clients: [%d, %d]",
                name, clients.size(), capacity, totalDemand, minVehicles,
                depot.readyTime, depot.dueTime, minReady, maxDue);
    }
}
