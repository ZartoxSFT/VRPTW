package vrptw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Configuration VRPTW ===\n");

        System.out.print("Fichier d'instance [data/data102.vrp]: ");
        String instancePath = readLineOrDefault(scanner, "data/data102.vrp");

        System.out.print("Algorithme (sa/tabu/both) [both]: ");
        String algo = readLineOrDefault(scanner, "both").toLowerCase();

        System.out.print("Nombre d'itérations [30000]: ");
        int iterations = readIntOrDefault(scanner, 30000);

        System.out.print("Graine aléatoire (seed) [42]: ");
        long seed = readLongOrDefault(scanner, 42);

        System.out.print("Répertoire de sortie [results]: ");
        String outDirStr = readLineOrDefault(scanner, "results");
        Path outDir = Path.of(outDirStr);
        Files.createDirectories(outDir);

        System.out.println("\n=== Démarrage ===\n");

        VrpParser parser = new VrpParser();
        VrpInstance instance = parser.parse(Path.of(instancePath));
        Evaluator evaluator = new Evaluator(instance, 1000.0);

        Solution initial = HeuristicUtils.buildInitialGreedy(instance, evaluator);
        Evaluator.Eval initEval = evaluator.evaluate(initial);
        System.out.printf("Initial: obj=%.2f dist=%.2f timeV=%.2f capV=%.2f routes=%d%n",
                initEval.objective, initEval.distance, initEval.timeViolation, initEval.capacityViolation,
                initial.routes.size());

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
            throw new IllegalArgumentException("L'algorithme doit être sa, tabu ou both");
        }

        for (SearchResult r : results) {
            String stem = fileStem(instancePath) + "_" + r.algorithm;
            Path historyCsv = outDir.resolve(stem + "_history.csv");
            Path historyPng = outDir.resolve(stem + "_history.png");
            Path routesPng = outDir.resolve(stem + "_routes.png");

            Exporter.exportHistoryCsv(r.bestObjectiveHistory, historyCsv);
            Exporter.exportHistoryPng(r.bestObjectiveHistory, historyPng,
                    "Historique - " + r.algorithm + " - " + instance.name);
            Exporter.exportRoutesPng(instance, r.bestSolution, routesPng,
                    "Tournées - " + r.algorithm + " - " + instance.name);

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

    private static String readLineOrDefault(Scanner scanner, String defaultValue) {
        String line = scanner.nextLine().trim();
        return line.isEmpty() ? defaultValue : line;
    }

    private static int readIntOrDefault(Scanner scanner, int defaultValue) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty())
            return defaultValue;
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Valeur invalide, utilisation de la valeur par défaut: " + defaultValue);
            return defaultValue;
        }
    }

    private static long readLongOrDefault(Scanner scanner, long defaultValue) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty())
            return defaultValue;
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            System.out.println("Valeur invalide, utilisation de la valeur par défaut: " + defaultValue);
            return defaultValue;
        }
    }
}
