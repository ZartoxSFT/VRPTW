package vrptw;

import java.util.List;
import java.util.Map;

public class SearchResult {
    public final String algorithm;
    public final Solution bestSolution;
    public final Evaluator.Eval bestEval;
    public final List<Double> bestObjectiveHistory;
    public final long runtimeMs;
    public final int solutionsEvaluated;
    public final Map<String, Integer> neighborhoodGeneratedCounts;
    public final Map<String, String> parameters;

    public SearchResult(
            String algorithm,
            Solution bestSolution,
            Evaluator.Eval bestEval,
            List<Double> bestObjectiveHistory,
            long runtimeMs,
            int solutionsEvaluated,
            Map<String, Integer> neighborhoodGeneratedCounts,
            Map<String, String> parameters) {
        this.algorithm = algorithm;
        this.bestSolution = bestSolution;
        this.bestEval = bestEval;
        this.bestObjectiveHistory = bestObjectiveHistory;
        this.runtimeMs = runtimeMs;
        this.solutionsEvaluated = solutionsEvaluated;
        this.neighborhoodGeneratedCounts = neighborhoodGeneratedCounts;
        this.parameters = parameters;
    }
}
