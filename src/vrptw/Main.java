package vrptw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final Path LAST_CONFIG_PATH = Path.of("last_execution.properties");
    private static final String DEFAULT_INSTANCE_PATH = "data/data101.vrp";
    private static final String DEFAULT_ALGO = "sa";
    private static final int DEFAULT_ITERATIONS = 30000;
    private static final long DEFAULT_SEED = 42L;
    private static final double DEFAULT_PENALTY_WEIGHT = 1000.0;
    private static final int DEFAULT_MAX_VEHICLES = Integer.MAX_VALUE;
    private static final String DEFAULT_INIT_STRATEGY = "random";
    private static final boolean DEFAULT_ESTIMATE_MIN_VEHICLES = true;
    private static final String DEFAULT_SA_NEIGHBORHOOD_TYPE = "relocate";
    private static final String DEFAULT_NEIGHBORHOOD_TYPE = "relocate";
    private static final boolean DEFAULT_ENFORCE_TIME_WINDOWS = true;
    private static final double DEFAULT_INITIAL_TEMP = 2500.0;
    private static final double DEFAULT_COOLING_RATE = 0.9995;
    private static final int DEFAULT_TABU_TENURE = 25;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        RunConfig config = loadRunConfig();

        System.out.println("=== Configuration VRPTW ===");
        System.out.println();

        System.out.print("Réinitialiser les paramètres par défaut ? (oui/non) [non]: ");
        boolean resetDefaults = readLineOrDefault(scanner, "non").toLowerCase().startsWith("o");
        if (resetDefaults) {
            config = defaultRunConfig();
            System.out.println("Paramètres réinitialisés sur les valeurs par défaut.");
            System.out.println();
        }

        System.out.print("Fichier d'instance [" + config.instancePath + "]: ");
        String instancePath = readLineOrDefault(scanner, config.instancePath);

        System.out.print("Algorithme (sa/tabu/both) [" + config.algo + "]: ");
        String algo = readLineOrDefault(scanner, config.algo).toLowerCase();

        System.out.print("Nombre d'itérations [" + config.iterations + "]: ");
        int iterations = readIntOrDefault(scanner, config.iterations);

        System.out.print("Graine aléatoire (seed) [" + config.seed + "]: ");
        long seed = readLongOrDefault(scanner, config.seed);

        System.out.print("Facteur de pénalité [" + config.penaltyWeight + "]: ");
        double penaltyWeight = readDoubleOrDefault(scanner, config.penaltyWeight);

        System.out.print("Génération solution initiale (greedy/random) [" + config.initialStrategy + "]: ");
        String initialStrategy = readLineOrDefault(scanner, config.initialStrategy).toLowerCase();
        if (!"random".equals(initialStrategy)) {
            initialStrategy = "greedy";
        }

        String estimateDefault = boolToYesNo(config.estimateMinVehicles);
        System.out
                .print("Estimer le minimum de véhicules (sans/avec fenêtres) ? (oui/non) [" + estimateDefault + "]: ");
        boolean estimateMinVehicles = readLineOrDefault(scanner, estimateDefault).toLowerCase().startsWith("o");

        String maxVehiclesDisplay = config.maxVehicles == Integer.MAX_VALUE
                ? "illimité"
                : String.valueOf(config.maxVehicles);
        System.out.print("Nombre max de véhicules [" + maxVehiclesDisplay + "]: ");
        int maxVehicles = readIntOrDefault(scanner, config.maxVehicles);
        if (maxVehicles <= 0) {
            maxVehicles = Integer.MAX_VALUE;
        }

        String enforceDefault = boolToYesNo(config.enforceTimeWindows);
        System.out.print("Appliquer les fenêtres temporelles ? (oui/non) [" + enforceDefault + "]: ");
        boolean enforceTimeWindows = readLineOrDefault(scanner, enforceDefault).toLowerCase().startsWith("o");

        // Paramètres SA
        double initialTemp = config.initialTemp;
        double coolingRate = config.coolingRate;
        String saNeighborhoodType = HeuristicUtils.normalizeNeighborhoodType(config.saNeighborhoodType);
        if ("sa".equals(algo) || "both".equals(algo)) {
            System.out.println();
            System.out.println("--- Paramètres Recuit Simulé ---");
            System.out.print("Température initiale [" + initialTemp + "]: ");
            initialTemp = readDoubleOrDefault(scanner, initialTemp);
            System.out.print("Taux de refroidissement (cooling rate) [" + coolingRate + "]: ");
            coolingRate = readDoubleOrDefault(scanner, coolingRate);
            System.out.print("Type de voisinage SA (relocate/exchange/2opt/mixed) [" + saNeighborhoodType + "]: ");
            saNeighborhoodType = HeuristicUtils
                    .normalizeNeighborhoodType(readLineOrDefault(scanner, saNeighborhoodType));
        }

        // Paramètres Tabu
        int tabuTenure = config.tabuTenure;
        String neighborhoodType = HeuristicUtils.normalizeNeighborhoodType(config.neighborhoodType);
        if ("tabu".equals(algo) || "both".equals(algo)) {
            System.out.println();
            System.out.println("--- Paramètres Recherche Tabou ---");
            System.out.print("Taille de la liste tabu (tenure) [" + tabuTenure + "]: ");
            tabuTenure = readIntOrDefault(scanner, tabuTenure);
            System.out
                    .print("Type de voisinage (relocate/exchange/2opt/intra/inter/mixed) [" + neighborhoodType + "]: ");
            neighborhoodType = HeuristicUtils
                    .normalizeNeighborhoodType(readLineOrDefault(scanner, neighborhoodType));
        }

        saveRunConfig(new RunConfig(
                instancePath,
                algo,
                iterations,
                seed,
                penaltyWeight,
                initialStrategy,
                estimateMinVehicles,
                maxVehicles,
                enforceTimeWindows,
                initialTemp,
                coolingRate,
                saNeighborhoodType,
                neighborhoodType,
                tabuTenure));

        System.out.println();
        System.out.println("=== Chargement de l'instance ===");
        System.out.println();

        VrpParser parser = new VrpParser();
        VrpInstance instance = parser.parse(Path.of(instancePath));
        System.out.println(instance.getStatistics());

        System.out.println();
        System.out.println("=== Résolution ===");
        System.out.println();

        List<RunOutcome> outcomes = new ArrayList<>();
        RunOutcome bestOverall = null;

        // Itération sur les limites de véhicules
        int[] vehicleLimits = { maxVehicles };
        if (maxVehicles != Integer.MAX_VALUE && maxVehicles > 1) {
            // Si une limite finie est spécifiée, tester aussi avec des limites progressives
            int step = Math.max(1, maxVehicles / 5);
            List<Integer> limits = new ArrayList<>();
            for (int k = step; k <= maxVehicles; k += step) {
                limits.add(k);
            }
            if (limits.isEmpty() || limits.get(limits.size() - 1) != maxVehicles) {
                limits.add(maxVehicles);
            }
            vehicleLimits = limits.stream().mapToInt(Integer::intValue).toArray();
        }

        for (int vehicleLimit : vehicleLimits) {
            System.out.println("--- Résolution avec limite de véhicules: " + vehicleLimit + " ---");
            Evaluator evaluator = new Evaluator(instance, penaltyWeight, enforceTimeWindows, vehicleLimit);

            Solution initial;
            if ("random".equals(initialStrategy)) {
                initial = HeuristicUtils.buildInitialRandom(instance, evaluator, seed);
            } else {
                initial = HeuristicUtils.buildInitialGreedy(instance, evaluator);
            }
            Evaluator.Eval initEval = evaluator.evaluate(initial);
            System.out.printf("Solution initiale: obj=%.2f dist=%.2f timeV=%.2f capV=%.2f vehV=%.2f routes=%d%n",
                    initEval.objective, initEval.distance, initEval.timeViolation, initEval.capacityViolation,
                    initEval.vehicleViolation,
                    initial.routes.size());
            System.out.println("Stratégie initiale: " + initialStrategy);
            System.out.println();

            if ("sa".equals(algo) || "both".equals(algo)) {
                SimulatedAnnealingSolver sa = new SimulatedAnnealingSolver();
                SearchResult r = sa.solve(instance, initial, evaluator, iterations, initialTemp, coolingRate,
                        saNeighborhoodType, seed + 31L * vehicleLimit);
                r.parameters.put("vehicleLimit", String.valueOf(vehicleLimit));
                RunOutcome outcome = new RunOutcome(vehicleLimit, r);
                outcomes.add(outcome);
                if (isBetterOverall(outcome, bestOverall)) {
                    bestOverall = outcome;
                }
            }

            if ("tabu".equals(algo) || "both".equals(algo)) {
                TabuSearchSolver tabu = new TabuSearchSolver();
                SearchResult r = tabu.solve(instance, initial, evaluator, iterations, tabuTenure,
                        neighborhoodType, seed + 7L * vehicleLimit);
                r.parameters.put("vehicleLimit", String.valueOf(vehicleLimit));
                RunOutcome outcome = new RunOutcome(vehicleLimit, r);
                outcomes.add(outcome);
                if (isBetterOverall(outcome, bestOverall)) {
                    bestOverall = outcome;
                }
            }
        }

        if (outcomes.isEmpty()) {
            throw new IllegalArgumentException("--algo doit être sa, tabu ou both");
        }

        System.out.println();
        System.out.println("=== Résultats ===");
        System.out.println();

        for (RunOutcome outcome : outcomes) {
            SearchResult r = outcome.result;
            String stem = fileStem(instancePath) + "_" + r.algorithm + "_v" + outcome.vehicleLimit;
            Path algoRootDir = algorithmResultRoot(r.algorithm);
            Path outDir = createNextExperimentDir(algoRootDir);
            Path historyCsv = outDir.resolve(stem + "_history.csv");
            Path historyPng = outDir.resolve(stem + "_history.png");
            Path routesPng = outDir.resolve(stem + "_routes.png");
            Path executionLogCsv = outDir.resolve("executions_log.csv");

            Exporter.exportHistoryCsv(r.bestObjectiveHistory, historyCsv);
            Exporter.exportHistoryPng(r.bestObjectiveHistory, historyPng,
                    "Historique - " + r.algorithm + " - " + instance.name);
            Exporter.exportRoutesPng(instance, r.bestSolution, routesPng,
                    "Tournées - " + r.algorithm + " - " + instance.name);
            Exporter.appendExecutionLogCsv(executionLogCsv, instance.name, r, penaltyWeight, enforceTimeWindows,
                    outcome.vehicleLimit);

            System.out.printf(
                    "[%s][k=%d] obj=%.2f dist=%.2f timeV=%.2f capV=%.2f vehV=%.2f routes=%d | faisable=%s | évaluations=%d temps=%dms%n",
                    r.algorithm,
                    outcome.vehicleLimit,
                    r.bestEval.objective,
                    r.bestEval.distance,
                    r.bestEval.timeViolation,
                    r.bestEval.capacityViolation,
                    r.bestEval.vehicleViolation,
                    r.bestSolution.routes.size(),
                    boolToYesNo(r.bestEval.feasible()),
                    r.solutionsEvaluated,
                    r.runtimeMs);
            System.out.printf("  voisinages générés: relocate=%d swap=%d 2opt=%d noop=%d%n",
                    r.neighborhoodGeneratedCounts.getOrDefault("relocate", 0),
                    r.neighborhoodGeneratedCounts.getOrDefault("swap", 0),
                    r.neighborhoodGeneratedCounts.getOrDefault("2opt", 0),
                    r.neighborhoodGeneratedCounts.getOrDefault("noop", 0));
            System.out.println("  paramètres: " + formatParams(r.parameters));
            System.out.println("  -> " + historyCsv);
            System.out.println("  -> " + historyPng);
            System.out.println("  -> " + routesPng);
            System.out.println("  -> " + executionLogCsv);
            System.out.println();
        }

        if (bestOverall != null) {
            SearchResult best = bestOverall.result;
            System.out.println("=== Meilleure solution globale ===");
            System.out.printf(
                    "Algo=%s | k=%d | obj=%.2f dist=%.2f routes=%d | faisable=%s%n",
                    best.algorithm,
                    bestOverall.vehicleLimit,
                    best.bestEval.objective,
                    best.bestEval.distance,
                    best.bestSolution.routes.size(),
                    boolToYesNo(best.bestEval.feasible()));
            System.out.println();
        }
    }

    private static boolean isBetterOverall(RunOutcome candidate, RunOutcome incumbent) {
        if (incumbent == null) {
            return true;
        }

        SearchResult cand = candidate.result;
        SearchResult best = incumbent.result;
        boolean candFeasible = cand.bestEval.feasible();
        boolean bestFeasible = best.bestEval.feasible();

        if (candFeasible != bestFeasible) {
            return candFeasible;
        }

        if (candFeasible) {
            if (Math.abs(cand.bestEval.distance - best.bestEval.distance) > 1e-9) {
                return cand.bestEval.distance < best.bestEval.distance;
            }
            return cand.bestEval.objective < best.bestEval.objective;
        }

        return cand.bestEval.objective < best.bestEval.objective;
    }

    private static Path algorithmResultRoot(String algorithm) {
        if ("sa".equalsIgnoreCase(algorithm)) {
            return Path.of("resultsSA");
        }
        if ("tabu".equalsIgnoreCase(algorithm)) {
            return Path.of("resultTABU");
        }
        return Path.of("results");
    }

    private static Path createNextExperimentDir(Path rootDir) throws IOException {
        Files.createDirectories(rootDir);
        int maxExp = 0;
        try (var entries = Files.list(rootDir)) {
            for (Path p : (Iterable<Path>) entries::iterator) {
                if (!Files.isDirectory(p)) {
                    continue;
                }
                String name = p.getFileName().toString();
                if (!name.startsWith("Exp")) {
                    continue;
                }
                try {
                    int n = Integer.parseInt(name.substring(3));
                    if (n > maxExp) {
                        maxExp = n;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore les dossiers qui ne respectent pas le format ExpN.
                }
            }
        }

        Path expDir = rootDir.resolve("Exp" + (maxExp + 1));
        Files.createDirectories(expDir);
        return expDir;
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

    private static double readDoubleOrDefault(Scanner scanner, double defaultValue) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty())
            return defaultValue;
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            System.out.println("Valeur invalide, utilisation de la valeur par défaut: " + defaultValue);
            return defaultValue;
        }
    }

    private static String formatParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "(aucun)";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    private static String boolToYesNo(boolean value) {
        return value ? "oui" : "non";
    }

    private static RunConfig defaultRunConfig() {
        return new RunConfig(
                DEFAULT_INSTANCE_PATH,
                DEFAULT_ALGO,
                DEFAULT_ITERATIONS,
                DEFAULT_SEED,
                DEFAULT_PENALTY_WEIGHT,
                DEFAULT_INIT_STRATEGY,
                DEFAULT_ESTIMATE_MIN_VEHICLES,
                DEFAULT_MAX_VEHICLES,
                DEFAULT_ENFORCE_TIME_WINDOWS,
                DEFAULT_INITIAL_TEMP,
                DEFAULT_COOLING_RATE,
                DEFAULT_SA_NEIGHBORHOOD_TYPE,
                DEFAULT_NEIGHBORHOOD_TYPE,
                DEFAULT_TABU_TENURE);
    }

    private static RunConfig loadRunConfig() {
        RunConfig defaults = defaultRunConfig();
        if (!Files.exists(LAST_CONFIG_PATH)) {
            return defaults;
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(LAST_CONFIG_PATH)) {
            p.load(in);
        } catch (IOException e) {
            System.out.println("Impossible de lire la config précédente, utilisation des valeurs par défaut.");
            return defaults;
        }

        return new RunConfig(
                p.getProperty("instancePath", defaults.instancePath),
                p.getProperty("algo", defaults.algo),
                parseIntOrDefault(p.getProperty("iterations"), defaults.iterations),
                parseLongOrDefault(p.getProperty("seed"), defaults.seed),
                parseDoubleOrDefault(p.getProperty("penaltyWeight"), defaults.penaltyWeight),
                p.getProperty("initialStrategy", defaults.initialStrategy),
                parseBooleanOrDefault(p.getProperty("estimateMinVehicles"), defaults.estimateMinVehicles),
                parseIntOrDefault(p.getProperty("maxVehicles"), defaults.maxVehicles),
                parseBooleanOrDefault(p.getProperty("enforceTimeWindows"), defaults.enforceTimeWindows),
                parseDoubleOrDefault(p.getProperty("initialTemp"), defaults.initialTemp),
                parseDoubleOrDefault(p.getProperty("coolingRate"), defaults.coolingRate),
                p.getProperty("saNeighborhoodType", defaults.saNeighborhoodType),
                p.getProperty("neighborhoodType", defaults.neighborhoodType),
                parseIntOrDefault(p.getProperty("tabuTenure"), defaults.tabuTenure));
    }

    private static void saveRunConfig(RunConfig config) {
        Properties p = new Properties();
        p.setProperty("instancePath", config.instancePath);
        p.setProperty("algo", config.algo);
        p.setProperty("iterations", String.valueOf(config.iterations));
        p.setProperty("seed", String.valueOf(config.seed));
        p.setProperty("penaltyWeight", String.valueOf(config.penaltyWeight));
        p.setProperty("initialStrategy", config.initialStrategy);
        p.setProperty("estimateMinVehicles", String.valueOf(config.estimateMinVehicles));
        p.setProperty("maxVehicles", String.valueOf(config.maxVehicles));
        p.setProperty("enforceTimeWindows", String.valueOf(config.enforceTimeWindows));
        p.setProperty("initialTemp", String.valueOf(config.initialTemp));
        p.setProperty("coolingRate", String.valueOf(config.coolingRate));
        p.setProperty("saNeighborhoodType", config.saNeighborhoodType);
        p.setProperty("neighborhoodType", config.neighborhoodType);
        p.setProperty("tabuTenure", String.valueOf(config.tabuTenure));

        try (OutputStream out = Files.newOutputStream(LAST_CONFIG_PATH)) {
            p.store(out, "Derniere configuration d'execution VRPTW");
        } catch (IOException e) {
            System.out.println("Impossible de sauvegarder la config d'exécution: " + e.getMessage());
        }
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long parseLongOrDefault(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean parseBooleanOrDefault(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static class RunConfig {
        final String instancePath;
        final String algo;
        final int iterations;
        final long seed;
        final double penaltyWeight;
        final String initialStrategy;
        final boolean estimateMinVehicles;
        final int maxVehicles;
        final boolean enforceTimeWindows;
        final double initialTemp;
        final double coolingRate;
        final String saNeighborhoodType;
        final String neighborhoodType;
        final int tabuTenure;

        RunConfig(
                String instancePath,
                String algo,
                int iterations,
                long seed,
                double penaltyWeight,
                String initialStrategy,
                boolean estimateMinVehicles,
                int maxVehicles,
                boolean enforceTimeWindows,
                double initialTemp,
                double coolingRate,
                String saNeighborhoodType,
                String neighborhoodType,
                int tabuTenure) {
            this.instancePath = instancePath;
            this.algo = algo;
            this.iterations = iterations;
            this.seed = seed;
            this.penaltyWeight = penaltyWeight;
            this.initialStrategy = initialStrategy;
            this.estimateMinVehicles = estimateMinVehicles;
            this.maxVehicles = maxVehicles;
            this.enforceTimeWindows = enforceTimeWindows;
            this.initialTemp = initialTemp;
            this.coolingRate = coolingRate;
            this.saNeighborhoodType = HeuristicUtils.normalizeNeighborhoodType(saNeighborhoodType);
            this.neighborhoodType = HeuristicUtils.normalizeNeighborhoodType(neighborhoodType);
            this.tabuTenure = tabuTenure;
        }
    }

    private static class RunOutcome {
        final int vehicleLimit;
        final SearchResult result;

        RunOutcome(int vehicleLimit, SearchResult result) {
            this.vehicleLimit = vehicleLimit;
            this.result = result;
        }
    }
}
