package vrptw;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TabuSearchSolver {
    public SearchResult solve(
            VrpInstance instance,
            Solution initial,
            Evaluator evaluator,
            int iterations,
            int neighborhoodSize,
            int tabuTenure,
            long seed) {
        long t0 = System.currentTimeMillis();
        Random random = new Random(seed);

        Solution current = initial.deepCopy();
        Evaluator.Eval currentEval = evaluator.evaluate(current);

        Solution best = current.deepCopy();
        Evaluator.Eval bestEval = currentEval;

        Deque<String> tabuQueue = new ArrayDeque<>();
        Set<String> tabuSet = new HashSet<>();
        List<Double> history = new ArrayList<>(iterations);
        int solutionsEvaluated = 1; // Initial solution

        for (int i = 0; i < iterations; i++) {
            Solution bestCandidate = null;
            Evaluator.Eval bestCandidateEval = null;
            String bestMove = null;

            for (int k = 0; k < neighborhoodSize; k++) {
                HeuristicUtils.Neighbor n = HeuristicUtils.randomNeighbor(current, random);
                Evaluator.Eval ev = evaluator.evaluate(n.solution);
                solutionsEvaluated++;
                boolean isTabu = tabuSet.contains(n.moveKey);
                boolean aspiration = ev.objective < bestEval.objective;
                if (isTabu && !aspiration) {
                    continue;
                }

                if (bestCandidateEval == null || ev.objective < bestCandidateEval.objective) {
                    bestCandidate = n.solution;
                    bestCandidateEval = ev;
                    bestMove = n.moveKey;
                }
            }

            if (bestCandidate == null) {
                HeuristicUtils.Neighbor fallback = HeuristicUtils.randomNeighbor(current, random);
                bestCandidate = fallback.solution;
                bestCandidateEval = evaluator.evaluate(bestCandidate);
                solutionsEvaluated++;
                bestMove = fallback.moveKey;
            }

            current = bestCandidate;
            currentEval = bestCandidateEval;

            if (currentEval.objective < bestEval.objective) {
                best = current.deepCopy();
                bestEval = currentEval;
            }

            if (bestMove != null && !"noop".equals(bestMove)) {
                tabuQueue.addLast(bestMove);
                tabuSet.add(bestMove);
                while (tabuQueue.size() > tabuTenure) {
                    String old = tabuQueue.removeFirst();
                    tabuSet.remove(old);
                }
            }

            history.add(bestEval.objective);
        }

        long dt = System.currentTimeMillis() - t0;
        return new SearchResult("tabu", best, bestEval, history, dt, solutionsEvaluated);
    }
}
