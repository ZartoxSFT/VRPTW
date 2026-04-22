package vrptw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HeuristicUtils {
    public static class Neighbor {
        public final Solution solution;
        public final String moveKey;

        public Neighbor(Solution solution, String moveKey) {
            this.solution = solution;
            this.moveKey = moveKey;
        }
    }

    public static Solution buildInitialGreedy(VrpInstance instance, Evaluator evaluator) {
        Set<Integer> unserved = new HashSet<>();
        for (Node c : instance.clients) {
            unserved.add(c.id);
        }

        List<List<Integer>> routes = new ArrayList<>();

        while (!unserved.isEmpty()) {
            List<Integer> route = new ArrayList<>();
            int current = -1;
            double currentTime = Math.max(0.0, instance.depot.readyTime);
            int currentLoad = 0;

            while (true) {
                int bestClient = -1;
                double bestScore = Double.POSITIVE_INFINITY;

                for (int candidateId : unserved) {
                    Node c = instance.getClient(candidateId);
                    if (currentLoad + c.demand > instance.capacity) {
                        continue;
                    }

                    double travel = current == -1
                            ? instance.distDepotToClient(candidateId)
                            : instance.distClientToClient(current, candidateId);

                    double arrival = currentTime + travel;
                    double startService = c.earliestFeasibleServiceStart(arrival);
                    if (startService < 0.0) {
                        continue;
                    }

                    double backToDepot = instance.distClientToDepot(candidateId);
                    if (startService + c.serviceTime + backToDepot > instance.depot.dueTime) {
                        continue;
                    }

                    double score = travel + 0.05 * Math.max(0, c.readyTime - arrival) + 0.01 * c.dueTime;
                    if (score < bestScore) {
                        bestScore = score;
                        bestClient = candidateId;
                    }
                }

                if (bestClient == -1) {
                    break;
                }

                Node selected = instance.getClient(bestClient);
                double travel = current == -1
                        ? instance.distDepotToClient(bestClient)
                        : instance.distClientToClient(current, bestClient);
                double arrival = currentTime + travel;
                double startService = selected.earliestFeasibleServiceStart(arrival);
                if (startService < 0.0) {
                    break;
                }

                currentTime = startService + selected.serviceTime;
                currentLoad += selected.demand;
                current = bestClient;

                route.add(bestClient);
                unserved.remove(bestClient);
            }

            if (route.isEmpty()) {
                int fallback = earliestDue(unserved, instance);
                route.add(fallback);
                unserved.remove(fallback);
            }

            routes.add(route);
        }

        Solution s = new Solution(routes);
        if (!evaluator.evaluate(s).feasible()) {
            return repairBySplitting(s, evaluator);
        }
        return s;
    }

    public static Solution buildInitialRandom(VrpInstance instance, Evaluator evaluator, long seed) {
        return buildInitialRandom(instance, evaluator, new Random(seed));
    }

    public static Solution buildInitialRandom(VrpInstance instance, Evaluator evaluator, Random random) {
        List<Integer> shuffledClients = new ArrayList<>();
        for (Node c : instance.clients) {
            shuffledClients.add(c.id);
        }
        Collections.shuffle(shuffledClients, random);

        List<List<Integer>> routes = new ArrayList<>();

        for (int clientId : shuffledClients) {
            Node client = instance.getClient(clientId);
            List<Integer> candidateRoutes = new ArrayList<>();

            for (int r = 0; r < routes.size(); r++) {
                int load = routeLoad(routes.get(r), instance);
                if (load + client.demand <= instance.capacity) {
                    candidateRoutes.add(r);
                }
            }

            if (candidateRoutes.isEmpty()) {
                List<Integer> route = new ArrayList<>();
                route.add(clientId);
                routes.add(route);
            } else {
                int selectedRoute = candidateRoutes.get(random.nextInt(candidateRoutes.size()));
                List<Integer> route = routes.get(selectedRoute);
                int pos = random.nextInt(route.size() + 1);
                route.add(pos, clientId);
            }
        }

        Solution randomSolution = new Solution(routes);
        if (!evaluator.evaluate(randomSolution).feasible()) {
            return repairBySplitting(randomSolution, evaluator);
        }
        return randomSolution;
    }

    private static Solution repairBySplitting(Solution s, Evaluator evaluator) {
        List<List<Integer>> repaired = new ArrayList<>();
        for (List<Integer> route : s.routes) {
            List<Integer> current = new ArrayList<>();
            for (int client : route) {
                current.add(client);
                if (!evaluator.routeFeasible(current)) {
                    current.remove(current.size() - 1);
                    if (!current.isEmpty()) {
                        repaired.add(current);
                    }
                    current = new ArrayList<>();
                    current.add(client);
                }
            }
            if (!current.isEmpty()) {
                repaired.add(current);
            }
        }
        return new Solution(repaired);
    }

    private static int earliestDue(Set<Integer> unserved, VrpInstance instance) {
        int best = -1;
        int due = Integer.MAX_VALUE;
        for (int id : unserved) {
            int d = instance.getClient(id).dueTime;
            if (d < due) {
                due = d;
                best = id;
            }
        }
        return best;
    }

    private static int routeLoad(List<Integer> route, VrpInstance instance) {
        int load = 0;
        for (int clientId : route) {
            load += instance.getClient(clientId).demand;
        }
        return load;
    }

    public static Neighbor randomNeighbor(Solution base, Random random) {
        return randomNeighbor(base, random, "relocate", "2opt");
    }

    public static Neighbor randomNeighbor(Solution base, Random random, String neighborhoodType) {
        return randomNeighbor(base, random, neighborhoodType, neighborhoodType);
    }

    public static Neighbor randomNeighbor(
            Solution base,
            Random random,
            String interNeighborhoodType,
            String intraNeighborhoodType) {
        if (base.routes.isEmpty()) {
            return new Neighbor(base.deepCopy(), "noop");
        }

        Solution copy = base.deepCopy();
        List<String> allowedModes = collectAllowedNeighborModes(interNeighborhoodType, intraNeighborhoodType);
        if (allowedModes.isEmpty()) {
            return new Neighbor(copy, "noop");
        }

        String mode = allowedModes.get(random.nextInt(allowedModes.size()));
        if ("relocate".equals(mode)) {
            return randomRelocate(copy, random);
        }
        if ("exchange".equals(mode)) {
            return randomSwap(copy, random);
        }
        return randomTwoOpt(copy, random);
    }

    private static String normalizeNeighborhoodTypeIfPresent(String neighborhoodType) {
        if (neighborhoodType == null || neighborhoodType.trim().isEmpty()) {
            return null;
        }
        return normalizeNeighborhoodType(neighborhoodType);
    }

    private static List<String> collectAllowedNeighborModes(String interNeighborhoodType,
            String intraNeighborhoodType) {
        List<String> allowedModes = new ArrayList<>();

        String interMode = normalizeNeighborhoodTypeIfPresent(interNeighborhoodType);
        if (interMode != null) {
            if ("inter".equals(interMode) || "relocate".equals(interMode)) {
                allowedModes.add("relocate");
            }
            if ("inter".equals(interMode) || "exchange".equals(interMode)) {
                allowedModes.add("exchange");
            }
        }

        String intraMode = normalizeNeighborhoodTypeIfPresent(intraNeighborhoodType);
        if (intraMode != null) {
            if ("intra".equals(intraMode) || "2opt".equals(intraMode)) {
                allowedModes.add("2opt");
            }
        }

        return allowedModes;
    }

    public static String normalizeNeighborhoodType(String neighborhoodType) {
        if (neighborhoodType == null) {
            return "relocate";
        }
        String t = neighborhoodType.trim().toLowerCase();
        if ("2-opt".equals(t) || "two-opt".equals(t)) {
            return "2opt";
        }
        if ("mixed".equals(t)) {
            return "relocate";
        }
        if ("relocate".equals(t) || "exchange".equals(t) || "2opt".equals(t) ||
                "intra".equals(t) || "inter".equals(t)) {
            return t;
        }
        return "relocate";
    }

    private static Neighbor randomRelocate(Solution s, Random random) {
        List<List<Integer>> routes = s.routes;
        int fromRoute = pickNonEmptyRoute(routes, random);
        if (fromRoute < 0) {
            return new Neighbor(s, "noop");
        }

        List<Integer> src = routes.get(fromRoute);
        int fromPos = random.nextInt(src.size());
        int client = src.remove(fromPos);

        int toRoute = random.nextInt(routes.size());
        List<Integer> dst = routes.get(toRoute);
        int toPos = random.nextInt(dst.size() + 1);
        dst.add(toPos, client);

        routes.removeIf(List::isEmpty);
        String move = "R:" + client + ":" + fromRoute + ":" + toRoute;
        return new Neighbor(s, move);
    }

    private static Neighbor randomSwap(Solution s, Random random) {
        List<List<Integer>> routes = s.routes;
        int r1 = pickNonEmptyRoute(routes, random);
        int r2 = pickNonEmptyRoute(routes, random);
        if (r1 < 0 || r2 < 0) {
            return new Neighbor(s, "noop");
        }

        List<Integer> a = routes.get(r1);
        List<Integer> b = routes.get(r2);
        int p1 = random.nextInt(a.size());
        int p2 = random.nextInt(b.size());

        int c1 = a.get(p1);
        int c2 = b.get(p2);
        a.set(p1, c2);
        b.set(p2, c1);

        String move = "S:" + c1 + ":" + c2;
        return new Neighbor(s, move);
    }

    private static Neighbor randomTwoOpt(Solution s, Random random) {
        List<List<Integer>> routes = s.routes;
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).size() >= 4) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            return new Neighbor(s, "noop");
        }

        int routeIndex = candidates.get(random.nextInt(candidates.size()));
        List<Integer> route = routes.get(routeIndex);

        int i = random.nextInt(route.size() - 2);
        int j = i + 1 + random.nextInt(route.size() - i - 1);

        while (i < j) {
            int tmp = route.get(i);
            route.set(i, route.get(j));
            route.set(j, tmp);
            i++;
            j--;
        }

        return new Neighbor(s, "O:" + routeIndex);
    }

    private static int pickNonEmptyRoute(List<List<Integer>> routes, Random random) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (!routes.get(i).isEmpty()) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            return -1;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * Generate ALL possible neighbors for a given solution.
     * Returns a list of all relocate, swap, and 2-opt moves.
     */
    public static List<Neighbor> getAllNeighbors(Solution base, String neighborhoodType) {
        return getAllNeighbors(base, neighborhoodType, neighborhoodType);
    }

    public static List<Neighbor> getAllNeighbors(
            Solution base,
            String interNeighborhoodType,
            String intraNeighborhoodType) {
        List<Neighbor> neighbors = new ArrayList<>();
        if (base.routes.isEmpty()) {
            return neighbors;
        }

        String interMode = normalizeNeighborhoodTypeIfPresent(interNeighborhoodType);
        String intraMode = normalizeNeighborhoodTypeIfPresent(intraNeighborhoodType);

        boolean allowRelocate = interMode != null
                && ("inter".equals(interMode)
                        || "relocate".equals(interMode));
        boolean allowSwap = interMode != null
                && ("inter".equals(interMode)
                        || "exchange".equals(interMode));
        boolean allowTwoOpt = intraMode != null
                && ("intra".equals(intraMode)
                        || "2opt".equals(intraMode));

        if (allowRelocate) {
            generateAllRelocate(base, neighbors);
        }
        if (allowSwap) {
            generateAllSwap(base, neighbors);
        }
        if (allowTwoOpt) {
            generateAll2Opt(base, neighbors);
        }

        if (neighbors.isEmpty()) {
            generateAllRelocate(base, neighbors);
            generateAllSwap(base, neighbors);
            generateAll2Opt(base, neighbors);
        }

        return neighbors;
    }

    private static void generateAllRelocate(Solution base, List<Neighbor> neighbors) {
        List<List<Integer>> routes = base.routes;

        for (int fromRoute = 0; fromRoute < routes.size(); fromRoute++) {
            List<Integer> src = routes.get(fromRoute);
            for (int fromPos = 0; fromPos < src.size(); fromPos++) {
                int client = src.get(fromPos);

                for (int toRoute = 0; toRoute < routes.size(); toRoute++) {
                    if (fromRoute == toRoute)
                        continue; // Skip same route

                    List<Integer> dst = routes.get(toRoute);
                    for (int toPos = 0; toPos <= dst.size(); toPos++) {
                        Solution copy = base.deepCopy();
                        List<Integer> srcCopy = copy.routes.get(fromRoute);
                        List<Integer> dstCopy = copy.routes.get(toRoute);

                        int relocClient = srcCopy.remove(fromPos);
                        dstCopy.add(toPos, relocClient);
                        copy.routes.removeIf(List::isEmpty);

                        String move = "R:" + client + ":" + fromRoute + ":" + toRoute;
                        neighbors.add(new Neighbor(copy, move));
                    }
                }
            }
        }
    }

    private static void generateAllSwap(Solution base, List<Neighbor> neighbors) {
        List<List<Integer>> routes = base.routes;

        for (int r1 = 0; r1 < routes.size(); r1++) {
            for (int r2 = r1 + 1; r2 < routes.size(); r2++) {
                List<Integer> a = routes.get(r1);
                List<Integer> b = routes.get(r2);

                for (int p1 = 0; p1 < a.size(); p1++) {
                    for (int p2 = 0; p2 < b.size(); p2++) {
                        Solution copy = base.deepCopy();
                        List<Integer> aCopy = copy.routes.get(r1);
                        List<Integer> bCopy = copy.routes.get(r2);

                        int c1 = aCopy.get(p1);
                        int c2 = bCopy.get(p2);
                        aCopy.set(p1, c2);
                        bCopy.set(p2, c1);

                        String move = "S:" + c1 + ":" + c2;
                        neighbors.add(new Neighbor(copy, move));
                    }
                }
            }
        }
    }

    private static void generateAll2Opt(Solution base, List<Neighbor> neighbors) {
        List<List<Integer>> routes = base.routes;

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            List<Integer> route = routes.get(routeIndex);
            if (route.size() < 4)
                continue; // Skip routes with less than 4 clients

            for (int i = 0; i < route.size() - 2; i++) {
                for (int j = i + 2; j < route.size(); j++) {
                    Solution copy = base.deepCopy();
                    List<Integer> routeCopy = copy.routes.get(routeIndex);

                    // Reverse the segment [i, j]
                    int ii = i;
                    int jj = j;
                    while (ii < jj) {
                        int tmp = routeCopy.get(ii);
                        routeCopy.set(ii, routeCopy.get(jj));
                        routeCopy.set(jj, tmp);
                        ii++;
                        jj--;
                    }

                    String move = "O:" + routeIndex + ":" + i + ":" + j;
                    neighbors.add(new Neighbor(copy, move));
                }
            }
        }
    }
}
