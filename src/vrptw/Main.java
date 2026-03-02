package vrptw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> p = parseArgs(args);

        String instancePath = p.getOrDefault("instance", "data/data102.vrp");
        String algo = p.getOrDefault("algo", "both").toLowerCase();
        int iterations = Integer.parseInt(p.getOrDefault("iter", "30000"));
        long seed = Long.parseLong(p.getOrDefault("seed", "42"));
        Path outDir = Path.of(p.getOrDefault("out", "results"));
        Files.createDirectories(outDir);

        VrpParser parser = new VrpParser();
        VrpInstance instance = parser.parse(Path.of(instancePath));
        Evaluator evaluator = new Evaluator(instance, 1000.0);

        Solution initial = HeuristicUtils.buildInitialGreedy(instance, evaluator);
        Evaluator.Eval initEval = evaluator.evaluate(initial);
        System.out.printf("Initial: obj=%.2f dist=%.2f timeV=%.2f capV=%.2f routes=%d%n",
                initEval.objective, initEval.distance, initEval.timeViolation, initEval.capacityViolation, initial.routes.size());

        List<SearchResult> results = new ArrayList<>();

        if ("sa".equals(algo) || "both".equals(algo)) {
            SimulatedAnnealingSolver sa = new SimulatedAnnealingSolver();
            SearchResult r = sa.solve(instance, initial, evaluator, iterations, 2500.0, 0.9995, seed);
            results.add(r);
        }

        if ("tabu".equals(algo) || "both".equals(algo)) {
            TabuSearchSolver tabu = new TabuSearchSolver();
            SearchResult r = tabu.solve(instance, initial, evaluator, iterations, 40, 25, seed + 7);
            results.add(r);
        }

        if (results.isEmpty()) {
            throw new IllegalArgumentException("--algo doit être sa, tabu ou both");
        }

        for (SearchResult r : results) {
            String stem = fileStem(instancePath) + "_" + r.algorithm;
            Path historyCsv = outDir.resolve(stem + "_history.csv");
            Path historyPng = outDir.resolve(stem + "_history.png");
            Path routesPng = outDir.resolve(stem + "_routes.png");

            Exporter.exportHistoryCsv(r.bestObjectiveHistory, historyCsv);
            Exporter.exportHistoryPng(r.bestObjectiveHistory, historyPng, "Historique - " + r.algorithm + " - " + instance.name);
            Exporter.exportRoutesPng(instance, r.bestSolution, routesPng, "Tournées - " + r.algorithm + " - " + instance.name);

            System.out.printf("[%s] obj=%.2f dist=%.2f timeV=%.2f capV=%.2f routes=%d runtimeMs=%d%n",
                    r.algorithm,
                    r.bestEval.objective,
                    r.bestEval.distance,
                    r.bestEval.timeViolation,
                    r.bestEval.capacityViolation,
                    r.bestSolution.routes.size(),
                    r.runtimeMs);
            System.out.println("  -> " + historyCsv);
            System.out.println("  -> " + historyPng);
            System.out.println("  -> " + routesPng);
        }
    }

    private static String fileStem(String path) {
        String name = Path.of(path).getFileName().toString();
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(0, idx) : name;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String key = a.substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(key, args[++i]);
                } else {
                    map.put(key, "true");
                }
            }
        }
        return map;
    }
}
