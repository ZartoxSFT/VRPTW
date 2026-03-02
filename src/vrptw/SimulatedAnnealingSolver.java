package vrptw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealingSolver {
    public SearchResult solve(
            VrpInstance instance,
            Solution initial,
            Evaluator evaluator,
            int iterations,
            double initialTemp,
            double coolingRate,
            long seed) {
        long t0 = System.currentTimeMillis();
        Random random = new Random(seed);

        Solution current = initial.deepCopy();
        Evaluator.Eval currentEval = evaluator.evaluate(current);

        Solution best = current.deepCopy();
        Evaluator.Eval bestEval = currentEval;

        double temp = initialTemp;
        List<Double> history = new ArrayList<>(iterations);

        for (int i = 0; i < iterations; i++) {
            HeuristicUtils.Neighbor neighbor = HeuristicUtils.randomNeighbor(current, random);
            Solution candidate = neighbor.solution;
            Evaluator.Eval candidateEval = evaluator.evaluate(candidate);

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
        return new SearchResult("sa", best, bestEval, history, dt);
    }
}
