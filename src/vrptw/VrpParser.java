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
                        true);
                continue;
            }

            if (inClients) {
                String[] t = line.split("\\s+");
                if (t.length < 7) {
                    continue;
                }
                int id = parseClientId(t[0]);
                Node c = new Node(
                        id,
                        t[0],
                        Double.parseDouble(t[1]),
                        Double.parseDouble(t[2]),
                        Integer.parseInt(t[3]),
                        Integer.parseInt(t[4]),
                        Integer.parseInt(t[5]),
                        Integer.parseInt(t[6]),
                        false);
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
}
