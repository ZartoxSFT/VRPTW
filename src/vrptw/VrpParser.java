package vrptw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VrpParser {
    public VrpInstance parse(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);

        String name = filePath.getFileName().toString();
        int capacity = -1;
        Node depot = null;
        List<Node> clients = new ArrayList<>();

        boolean inDepot = false;
        boolean inClients = false;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("NAME:")) {
                name = line.substring("NAME:".length()).trim();
                continue;
            }
            if (line.startsWith("MAX_QUANTITY:")) {
                capacity = Integer.parseInt(line.substring("MAX_QUANTITY:".length()).trim());
                continue;
            }
            if (line.startsWith("DATA_DEPOTS")) {
                inDepot = true;
                inClients = false;
                continue;
            }
            if (line.startsWith("DATA_CLIENTS")) {
                inDepot = false;
                inClients = true;
                continue;
            }

            if (inDepot) {
                String[] t = line.split("\\s+");
                if (t.length < 5) {
                    continue;
                }
                depot = new Node(
                        0,
                        t[0],
                        Double.parseDouble(t[1]),
                        Double.parseDouble(t[2]),
                        Integer.parseInt(t[3]),
                        Integer.parseInt(t[4]),
                        0,
                        0,
                        true,
                        List.of(new TimeWindow(
                                Integer.parseInt(t[3]),
                                Integer.parseInt(t[4]))));
                continue;
            }

            if (inClients) {
                String[] t = line.split("\\s+");
                if (t.length < 7) {
                    continue;
                }
                int id = parseClientId(t[0]);
                int readyTime = Integer.parseInt(t[3]);
                int dueTime = Integer.parseInt(t[4]);
                List<TimeWindow> windows = parseAvailabilityWindows(t, readyTime, dueTime);
                Node c = new Node(
                        id,
                        t[0],
                        Double.parseDouble(t[1]),
                        Double.parseDouble(t[2]),
                        readyTime,
                        dueTime,
                        Integer.parseInt(t[5]),
                        Integer.parseInt(t[6]),
                        false,
                        windows);
                clients.add(c);
            }
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("MAX_QUANTITY introuvable dans " + filePath);
        }
        if (depot == null) {
            throw new IllegalArgumentException("Depot introuvable dans " + filePath);
        }
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("Aucun client trouvé dans " + filePath);
        }

        clients.sort((a, b) -> Integer.compare(a.id, b.id));
        return new VrpInstance(name, capacity, depot, clients);
    }

    private int parseClientId(String token) {
        if (token.length() < 2) {
            throw new IllegalArgumentException("ID client invalide: " + token);
        }
        if (token.charAt(0) == 'c' || token.charAt(0) == 'C') {
            return Integer.parseInt(token.substring(1));
        }
        return Integer.parseInt(token);
    }

    private List<TimeWindow> parseAvailabilityWindows(String[] tokens, int readyTime, int dueTime) {
        List<TimeWindow> windows = new ArrayList<>();

        // Format optionnel après les 7 colonnes standard:
        // cX x y ready due demand service [w1Start w1End w2Start w2End ...]
        int extraCount = tokens.length - 7;
        if (extraCount >= 2 && extraCount % 2 == 0) {
            for (int i = 7; i < tokens.length; i += 2) {
                int start = Integer.parseInt(tokens[i]);
                int end = Integer.parseInt(tokens[i + 1]);
                windows.add(new TimeWindow(start, end));
            }
        }

        if (windows.isEmpty()) {
            windows.add(new TimeWindow(readyTime, dueTime));
        }

        return windows;
    }
}
