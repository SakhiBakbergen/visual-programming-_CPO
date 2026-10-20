package kz.atu.inventory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Хранение товаров в CSV-файле (разделитель ';', кодировка UTF-8). */
public class CsvStorage {

    public static List<Product> load(Path file) throws IOException {
        List<Product> result = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] p = line.split(";", -1);
            if (p.length < 7) continue; // пропускаем повреждённые строки
            try {
                result.add(new Product(
                        Integer.parseInt(p[0].trim()), p[1], p[2],
                        Double.parseDouble(p[3].trim()),
                        Integer.parseInt(p[4].trim()),
                        Integer.parseInt(p[5].trim()), p[6]));
            } catch (NumberFormatException ignored) {
                // строка с неверными числами пропускается
            }
        }
        return result;
    }

    public static void save(Path file, List<Product> products) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Product p : products) {
            lines.add(p.getId() + ";" + clean(p.getName()) + ";" + clean(p.getCategory()) + ";"
                    + String.format(Locale.ROOT, "%.2f", p.getPrice()) + ";"
                    + p.getQuantity() + ";" + p.getMinQuantity() + ";" + clean(p.getSupplier()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace(";", ",").replace("\n", " ");
    }
}