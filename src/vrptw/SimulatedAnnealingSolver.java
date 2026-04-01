package vrptw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulatedAnnealingSolver {
    public SearchResult solve(
            VrpInstance instance,
            Solution initial,
            Evaluator evaluator,
            int iterations,
            double initialTemp,
            double coolingRate,
            String neighborhoodType,
            long seed) {
        long t0 = System.currentTimeMillis();
        Random random = new Random(seed);

        Solution current = initial.deepCopy();
        Evaluator.Eval currentEval = evaluator.evaluate(current);

        Solution best = current.deepCopy();
        Evaluator.Eval bestEval = currentEval;

        double temp = initialTemp;
        List<Double> history = new ArrayList<>(iterations);
        int solutionsEvaluated = 1; // Initial solution
        Map<String, Integer> neighborhoodGeneratedCounts = new LinkedHashMap<>();
        neighborhoodGeneratedCounts.put("relocate", 0);
        neighborhoodGeneratedCounts.put("swap", 0);
        neighborhoodGeneratedCounts.put("2opt", 0);
        neighborhoodGeneratedCounts.put("noop", 0);

        for (int i = 0; i < iterations; i++) {
            HeuristicUtils.Neighbor neighbor = HeuristicUtils.randomNeighbor(current, random, neighborhoodType);
            incrementMoveCount(neighborhoodGeneratedCounts, neighbor.moveKey);
            Solution candidate = neighbor.solution;
            Evaluator.Eval candidateEval = evaluator.evaluate(candidate);
            solutionsEvaluated++;

            double delta = candidateEval.objective - currentEval.objective;
            if (delta < 0 || random.nextDouble() < Math.exp(-delta / Math.max(temp, 1e-9))) {
                current = candidate;
                currentEval = candidateEval;
            }

            if (currentEval.objective < bestEval.objective) {
                best = current.deepCopy();
                bestEval = currentEval;
            }

            history.add(bestEval.objective);
            temp *= coolingRate;
            if (temp < 1e-6) {
                temp = 1e-6;
            }
        }

        long dt = System.currentTimeMillis() - t0;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("iterations", String.valueOf(iterations));
        params.put("seed", String.valueOf(seed));
        params.put("initialTemp", String.valueOf(initialTemp));
        params.put("coolingRate", String.valueOf(coolingRate));
        params.put("neighborhoodType", HeuristicUtils.normalizeNeighborhoodType(neighborhoodType));

        return new SearchResult("sa", best, bestEval, history, dt, solutionsEvaluated,
                neighborhoodGeneratedCounts, params);
    }

    private static void incrementMoveCount(Map<String, Integer> counts, String moveKey) {
        String type = classifyMove(moveKey);
        counts.put(type, counts.getOrDefault(type, 0) + 1);
    }

    private static String classifyMove(String moveKey) {
        if (moveKey == null || "noop".equals(moveKey)) {
            return "noop";
        }
        if (moveKey.startsWith("R:")) {
            return "relocate";
        }
        if (moveKey.startsWith("S:")) {
            return "swap";
        }
        if (moveKey.startsWith("O:")) {
            return "2opt";
        }
        return "other";
    }
}
