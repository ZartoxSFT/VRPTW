package vrptw;

public class VehicleMinimizer {
    public static class MinVehicleSummary {
        public final int lowerBoundCapacity;
        public final int minVehiclesNoTimeWindows;
        public final int minVehiclesWithTimeWindows;

        public MinVehicleSummary(int lowerBoundCapacity, int minVehiclesNoTimeWindows, int minVehiclesWithTimeWindows) {
            this.lowerBoundCapacity = lowerBoundCapacity;
            this.minVehiclesNoTimeWindows = minVehiclesNoTimeWindows;
            this.minVehiclesWithTimeWindows = minVehiclesWithTimeWindows;
        }
    }

    public static MinVehicleSummary estimate(VrpInstance instance, double penaltyWeight, long seed) {
        int lb = instance.minVehiclesCapacityBound();
        int noTw = findFeasibleMinVehicles(instance, false, penaltyWeight, seed, lb);
        int withTw = findFeasibleMinVehicles(instance, true, penaltyWeight, seed + 10_000L, lb);
        return new MinVehicleSummary(lb, noTw, withTw);
    }

    private static int findFeasibleMinVehicles(
            VrpInstance instance,
            boolean enforceTimeWindows,
            double penaltyWeight,
            long seed,
            int lowerBound) {
        int upperBound = instance.clientCount();

        SimulatedAnnealingSolver sa = new SimulatedAnnealingSolver();
        TabuSearchSolver tabu = new TabuSearchSolver();

        for (int maxVehicles = lowerBound; maxVehicles <= upperBound; maxVehicles++) {
            Evaluator evaluator = new Evaluator(instance, penaltyWeight, enforceTimeWindows, maxVehicles);

            for (int attempt = 0; attempt < 4; attempt++) {
                long attemptSeed = seed + 997L * attempt + 37L * maxVehicles;

                Solution greedyInit = HeuristicUtils.buildInitialGreedy(instance, evaluator);
                if (evaluator.evaluate(greedyInit).feasible()) {
                    return maxVehicles;
                }

                Solution randomInit = HeuristicUtils.buildInitialRandom(instance, evaluator, attemptSeed);
                if (evaluator.evaluate(randomInit).feasible()) {
                    return maxVehicles;
                }

                SearchResult tabuResult = tabu.solve(instance, randomInit, evaluator, 2500, 35, 20, "mixed",
                        attemptSeed + 1);
                if (tabuResult.bestEval.feasible()) {
                    return maxVehicles;
                }

                SearchResult saResult = sa.solve(instance, greedyInit, evaluator, 2500, 2000.0, 0.9993, "mixed",
                        attemptSeed + 2);
                if (saResult.bestEval.feasible()) {
                    return maxVehicles;
                }
            }
        }

        return upperBound;
    }
}
