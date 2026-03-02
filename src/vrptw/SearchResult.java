package vrptw;

import java.util.List;

public class SearchResult {
    public final String algorithm;
    public final Solution bestSolution;
    public final Evaluator.Eval bestEval;
    public final List<Double> bestObjectiveHistory;
    public final long runtimeMs;

    public SearchResult(
            String algorithm,
            Solution bestSolution,
            Evaluator.Eval bestEval,
            List<Double> bestObjectiveHistory,
            long runtimeMs) {
        this.algorithm = algorithm;
        this.bestSolution = bestSolution;
        this.bestEval = bestEval;
        this.bestObjectiveHistory = bestObjectiveHistory;
        this.runtimeMs = runtimeMs;
    }
}
