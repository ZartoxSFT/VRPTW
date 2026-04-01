package vrptw;

import java.util.List;

public class Evaluator {
    public static class Eval {
        public final double distance;
        public final double timeViolation;
        public final double capacityViolation;
        public final double vehicleViolation;
        public final boolean timeWindowsEnforced;
        public final double objective;

        public Eval(double distance, double timeViolation, double capacityViolation, double vehicleViolation,
                boolean timeWindowsEnforced, double objective) {
            this.distance = distance;
            this.timeViolation = timeViolation;
            this.capacityViolation = capacityViolation;
            this.vehicleViolation = vehicleViolation;
            this.timeWindowsEnforced = timeWindowsEnforced;
            this.objective = objective;
        }

        public boolean feasible() {
            boolean timeFeasible = !timeWindowsEnforced || timeViolation < 1e-9;
            return timeFeasible && capacityViolation < 1e-9 && vehicleViolation < 1e-9;
        }
    }

    private final VrpInstance instance;
    private final double penaltyWeight;
    private final boolean enforceTimeWindows;
    private final int maxVehicles;

    public Evaluator(VrpInstance instance, double penaltyWeight) {
        this(instance, penaltyWeight, true, Integer.MAX_VALUE);
    }

    public Evaluator(VrpInstance instance, double penaltyWeight, boolean enforceTimeWindows) {
        this(instance, penaltyWeight, enforceTimeWindows, Integer.MAX_VALUE);
    }

    public Evaluator(VrpInstance instance, double penaltyWeight, boolean enforceTimeWindows, int maxVehicles) {
        this.instance = instance;
        this.penaltyWeight = penaltyWeight;
        this.enforceTimeWindows = enforceTimeWindows;
        this.maxVehicles = maxVehicles <= 0 ? Integer.MAX_VALUE : maxVehicles;
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

        double timeComponent = enforceTimeWindows ? totalTimeViolation : 0.0;
        double vehicleViolation = Math.max(0, s.routes.size() - maxVehicles);
        double objective = totalDistance + penaltyWeight * (timeComponent + totalCapacityViolation + vehicleViolation);
        return new Eval(totalDistance, totalTimeViolation, totalCapacityViolation, vehicleViolation, enforceTimeWindows,
                objective);
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
            double startService = c.earliestFeasibleServiceStart(arrival);

            if (startService < 0.0) {
                int latestEnd = c.latestAvailabilityEnd();
                if (arrival > latestEnd) {
                    timeViolation += (arrival - latestEnd);
                }
                startService = arrival;
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
        if (enforceTimeWindows) {
            return re.timeViolation < 1e-9 && re.capacityViolation < 1e-9;
        } else {
            return re.capacityViolation < 1e-9;
        }
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
