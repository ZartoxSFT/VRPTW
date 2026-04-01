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
        return randomNeighbor(base, random, "mixed");
    }

    public static Neighbor randomNeighbor(Solution base, Random random, String neighborhoodType) {
        if (base.routes.isEmpty()) {
            return new Neighbor(base.deepCopy(), "noop");
        }

        Solution copy = base.deepCopy();
        String mode = normalizeNeighborhoodType(neighborhoodType);
        if ("relocate".equals(mode)) {
            return randomRelocate(copy, random);
        }
        if ("exchange".equals(mode)) {
            return randomSwap(copy, random);
        }
        if ("2opt".equals(mode)) {
            return randomTwoOpt(copy, random);
        }

        int pick = random.nextInt(3);
        if (pick == 0) {
            return randomRelocate(copy, random);
        }
        if (pick == 1) {
            return randomSwap(copy, random);
        }
        return randomTwoOpt(copy, random);
    }

    public static String normalizeNeighborhoodType(String neighborhoodType) {
        if (neighborhoodType == null) {
            return "mixed";
        }
        String t = neighborhoodType.trim().toLowerCase();
        if ("2-opt".equals(t) || "two-opt".equals(t)) {
            return "2opt";
        }
        if ("relocate".equals(t) || "exchange".equals(t) || "2opt".equals(t) || "mixed".equals(t)) {
            return t;
        }
        return "mixed";
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
}
