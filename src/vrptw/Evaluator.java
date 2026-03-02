package vrptw;

import java.util.List;

public class Evaluator {
    public static class Eval {
        public final double distance;
        public final double timeViolation;
        public final double capacityViolation;
        public final double objective;

        public Eval(double distance, double timeViolation, double capacityViolation, double objective) {
            this.distance = distance;
            this.timeViolation = timeViolation;
            this.capacityViolation = capacityViolation;
            this.objective = objective;
        }

        public boolean feasible() {
            return timeViolation < 1e-9 && capacityViolation < 1e-9;
        }
    }

    private final VrpInstance instance;
    private final double penaltyWeight;

    public Evaluator(VrpInstance instance, double penaltyWeight) {
        this.instance = instance;
        this.penaltyWeight = penaltyWeight;
    }

    public Eval evaluate(Solution s) {
        double totalDistance = 0.0;
        double totalTimeViolation = 0.0;
        double totalCapacityViolation = 0.0;

        for (List<Integer> route : s.routes) {
            RouteEval re = evaluateRoute(route);
            totalDistance += re.distance;
            totalTimeViolation += re.timeViolation;
            totalCapacityViolation += re.capacityViolation;
        }

        double objective = totalDistance + penaltyWeight * (totalTimeViolation + totalCapacityViolation);
        return new Eval(totalDistance, totalTimeViolation, totalCapacityViolation, objective);
    }

    public RouteEval evaluateRoute(List<Integer> route) {
        if (route.isEmpty()) {
            return new RouteEval(0.0, 0.0, 0.0);
        }

        double distance = 0.0;
        double timeViolation = 0.0;
        int load = 0;

        int prevClient = -1;
        double currentTime = Math.max(0.0, instance.depot.readyTime);

        for (int clientId : route) {
            Node c = instance.getClient(clientId);
            double travel;
            if (prevClient == -1) {
                travel = instance.distDepotToClient(clientId);
            } else {
                travel = instance.distClientToClient(prevClient, clientId);
            }
            distance += travel;
            double arrival = currentTime + travel;
            double startService = Math.max(arrival, c.readyTime);
            if (startService > c.dueTime) {
                timeViolation += (startService - c.dueTime);
            }

            load += c.demand;
            currentTime = startService + c.serviceTime;
            prevClient = clientId;
        }

        if (prevClient != -1) {
            double back = instance.distClientToDepot(prevClient);
            distance += back;
            double depotArrival = currentTime + back;
            if (depotArrival > instance.depot.dueTime) {
                timeViolation += (depotArrival - instance.depot.dueTime);
            }
        }

        double capacityViolation = Math.max(0, load - instance.capacity);
        return new RouteEval(distance, timeViolation, capacityViolation);
    }

    public boolean routeFeasible(List<Integer> route) {
        RouteEval re = evaluateRoute(route);
        return re.timeViolation < 1e-9 && re.capacityViolation < 1e-9;
    }

    public static class RouteEval {
        public final double distance;
        public final double timeViolation;
        public final double capacityViolation;

        public RouteEval(double distance, double timeViolation, double capacityViolation) {
            this.distance = distance;
            this.timeViolation = timeViolation;
            this.capacityViolation = capacityViolation;
        }
    }
}
