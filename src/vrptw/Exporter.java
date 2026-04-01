package vrptw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class Exporter {
    private static final DateTimeFormatter RUN_TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void exportHistoryCsv(List<Double> history, Path outputCsv) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("iter,best_objective\n");
        for (int i = 0; i < history.size(); i++) {
            sb.append(i + 1).append(',').append(history.get(i)).append('\n');
        }
        Files.writeString(outputCsv, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void appendExecutionLogCsv(
            Path outputCsv,
            String instanceName,
            SearchResult result,
            double penaltyWeight,
            boolean enforceTimeWindows,
            int maxVehicles) throws IOException {
        boolean fileExists = Files.exists(outputCsv);
        StringBuilder sb = new StringBuilder();

        if (!fileExists) {
            sb.append(
                    "timestamp,instance,algorithm,best_objective,best_distance,time_violation,capacity_violation,vehicle_violation,routes,runtime_ms,solutions_evaluated,generated_relocate,generated_swap,generated_noop,penalty_weight,enforce_time_windows,max_vehicles,parameters\n");
        }

        sb.append(csv(LocalDateTime.now().format(RUN_TS_FORMAT))).append(',')
                .append(csv(instanceName)).append(',')
                .append(csv(result.algorithm)).append(',')
                .append(result.bestEval.objective).append(',')
                .append(result.bestEval.distance).append(',')
                .append(result.bestEval.timeViolation).append(',')
                .append(result.bestEval.capacityViolation).append(',')
                .append(result.bestEval.vehicleViolation).append(',')
                .append(result.bestSolution.routes.size()).append(',')
                .append(result.runtimeMs).append(',')
                .append(result.solutionsEvaluated).append(',')
                .append(result.neighborhoodGeneratedCounts.getOrDefault("relocate", 0)).append(',')
                .append(result.neighborhoodGeneratedCounts.getOrDefault("swap", 0)).append(',')
                .append(result.neighborhoodGeneratedCounts.getOrDefault("noop", 0)).append(',')
                .append(penaltyWeight).append(',')
                .append(enforceTimeWindows).append(',')
                .append(maxVehicles).append(',')
                .append(csv(formatParams(result.parameters)))
                .append('\n');

        Files.writeString(outputCsv, sb.toString(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND);
    }

    public static void exportHistoryPng(List<Double> history, Path outputPng, String title) throws IOException {
        int w = 1200;
        int h = 700;
        int left = 80;
        int right = 30;
        int top = 60;
        int bottom = 70;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, left, 30);

        int x0 = left;
        int y0 = h - bottom;
        int plotW = w - left - right;
        int plotH = h - top - bottom;

        g.setColor(Color.GRAY);
        g.drawLine(x0, y0, x0 + plotW, y0);
        g.drawLine(x0, y0, x0, top);

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : history) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (history.isEmpty()) {
            min = 0;
            max = 1;
        }
        if (Math.abs(max - min) < 1e-9) {
            max = min + 1.0;
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(Color.GRAY);
        g.drawString("itérations", x0 + plotW - 60, y0 + 30);
        g.drawString(String.format("%.2f", max), 10, top + 5);
        g.drawString(String.format("%.2f", min), 10, y0 + 5);

        g.setColor(new Color(35, 99, 235));
        g.setStroke(new BasicStroke(2f));
        for (int i = 1; i < history.size(); i++) {
            int x1 = x0 + (int) ((i - 1) * (plotW / (double) Math.max(1, history.size() - 1)));
            int x2 = x0 + (int) (i * (plotW / (double) Math.max(1, history.size() - 1)));
            int y1 = y0 - (int) ((history.get(i - 1) - min) / (max - min) * plotH);
            int y2 = y0 - (int) ((history.get(i) - min) / (max - min) * plotH);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        ImageIO.write(img, "png", outputPng.toFile());
    }

    public static void exportRoutesPng(VrpInstance instance, Solution solution, Path outputPng, String title)
            throws IOException {
        int w = 1200;
        int h = 900;
        int pad = 60;

        double minX = instance.depot.x;
        double maxX = instance.depot.x;
        double minY = instance.depot.y;
        double maxY = instance.depot.y;
        for (Node c : instance.clients) {
            minX = Math.min(minX, c.x);
            maxX = Math.max(maxX, c.x);
            minY = Math.min(minY, c.y);
            maxY = Math.max(maxY, c.y);
        }

        double spanX = Math.max(1e-9, maxX - minX);
        double spanY = Math.max(1e-9, maxY - minY);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, 30, 30);

        for (int r = 0; r < solution.routes.size(); r++) {
            List<Integer> route = solution.routes.get(r);
            Color color = Color.getHSBColor((float) r / Math.max(1, solution.routes.size()), 0.85f, 0.85f);
            g.setColor(color);
            g.setStroke(new BasicStroke(2f));

            double prevX = instance.depot.x;
            double prevY = instance.depot.y;
            for (int clientId : route) {
                Node c = instance.getClient(clientId);
                int x1 = toPixel(prevX, minX, spanX, pad, w - pad);
                int y1 = toPixelY(prevY, minY, spanY, pad, h - pad);
                int x2 = toPixel(c.x, minX, spanX, pad, w - pad);
                int y2 = toPixelY(c.y, minY, spanY, pad, h - pad);
                g.drawLine(x1, y1, x2, y2);
                prevX = c.x;
                prevY = c.y;
            }

            if (!route.isEmpty()) {
                int x1 = toPixel(prevX, minX, spanX, pad, w - pad);
                int y1 = toPixelY(prevY, minY, spanY, pad, h - pad);
                int x2 = toPixel(instance.depot.x, minX, spanX, pad, w - pad);
                int y2 = toPixelY(instance.depot.y, minY, spanY, pad, h - pad);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        g.setColor(new Color(31, 41, 55));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Node c : instance.clients) {
            int x = toPixel(c.x, minX, spanX, pad, w - pad);
            int y = toPixelY(c.y, minY, spanY, pad, h - pad);
            g.fillOval(x - 3, y - 3, 6, 6);
        }

        int dx = toPixel(instance.depot.x, minX, spanX, pad, w - pad);
        int dy = toPixelY(instance.depot.y, minY, spanY, pad, h - pad);
        g.setColor(new Color(220, 38, 38));
        g.fillRect(dx - 6, dy - 6, 12, 12);

        g.dispose();
        ImageIO.write(img, "png", outputPng.toFile());
    }

    private static int toPixel(double x, double minX, double spanX, int pxMin, int pxMax) {
        return pxMin + (int) ((x - minX) / spanX * (pxMax - pxMin));
    }

    private static int toPixelY(double y, double minY, double spanY, int pyMin, int pyMax) {
        return pyMax - (int) ((y - minY) / spanY * (pyMax - pyMin));
    }

    private static String formatParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) {
                sb.append("; ");
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
