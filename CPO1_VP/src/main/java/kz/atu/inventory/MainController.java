package kz.atu.inventory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainController {

    private static final Path DATA_FILE = Paths.get("inventory.csv");
    private static final String ALL = "Все";

    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, Number> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Number> colPrice;
    @FXML private TableColumn<Product, Number> colQty;
    @FXML private TableColumn<Product, Number> colMin;
    @FXML private TableColumn<Product, String> colSupplier;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private CheckBox lowStockCheck;
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    @FXML private Label totalLabel;
    @FXML private Label valueLabel;
    @FXML private Label lowLabel;

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private FilteredList<Product> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colPrice.setCellValueFactory(c -> c.getValue().priceProperty());
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colMin.setCellValueFactory(c -> c.getValue().minQuantityProperty());
        colSupplier.setCellValueFactory(c -> c.getValue().supplierProperty());

        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.2f ₸", v.doubleValue()));
            }
        });

        // Подсветка строк с низким остатком + двойной клик = редактирование
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>() {
                @Override
                protected void updateItem(Product p, boolean empty) {
                    super.updateItem(p, empty);
                    getStyleClass().remove("low-stock");
                    if (!empty && p != null && p.isLowStock()) {
                        getStyleClass().add("low-stock");
                    }
                }
            };
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) onEdit();
            });
            return row;
        });

        products.setAll(loadData());
        filtered = new FilteredList<>(products, p -> true);
        SortedList<Product> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        refreshCategories();
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        categoryBox.valueProperty().addListener((o, a, b) -> applyFilter());
        lowStockCheck.selectedProperty().addListener((o, a, b) -> applyFilter());
        applyFilter();
        updateStats();
        setStatus("Готово");
    }

    // ---------- данные ----------

    private List<Product> loadData() {
        if (Files.exists(DATA_FILE)) {
            try {
                List<Product> list = CsvStorage.load(DATA_FILE);
                if (!list.isEmpty()) return list;
            } catch (IOException e) {
                showAlert(Alert.AlertType.WARNING, "Файл данных",
                        "Не удалось прочитать inventory.csv. Загружены демо-данные.");
            }
        }
        return demoData();
    }

    private List<Product> demoData() {
        return new ArrayList<>(List.of(
                new Product(1, "Ноутбук Lenovo IdeaPad", "Электроника", 285000, 12, 5, "Lenovo KZ"),
                new Product(2, "Смартфон Samsung A54", "Электроника", 169000, 25, 10, "Samsung"),
                new Product(3, "Мышь Logitech M185", "Периферия", 6500, 3, 10, "Logitech"),
                new Product(4, "Клавиатура механическая", "Периферия", 18000, 18, 8, "Redragon"),
                new Product(5, "Монитор 24\" Dell", "Электроника", 82000, 7, 5, "Dell"),
                new Product(6, "Бумага А4 (500 л.)", "Канцелярия", 2300, 120, 50, "OfficeMax"),
                new Product(7, "Ручка шариковая", "Канцелярия", 150, 400, 100, "OfficeMax"),
                new Product(8, "Степлер", "Канцелярия", 1800, 4, 5, "Kangaro"),
                new Product(9, "Кресло офисное", "Мебель", 45000, 9, 3, "Мебель-Сити"),
                new Product(10, "Стол письменный", "Мебель", 62000, 2, 3, "Мебель-Сити"),
                new Product(11, "Флешка 64 ГБ", "Аксессуары", 3500, 60, 20, "Kingston"),
                new Product(12, "Наушники JBL", "Аксессуары", 24000, 15, 6, "JBL")));
    }

    private int nextId() {
        return products.stream().mapToInt(Product::getId).max().orElse(0) + 1;
    }

    private void refreshCategories() {
        String selected = categoryBox.getValue();
        Set<String> cats = new TreeSet<>();
        for (Product p : products) cats.add(p.getCategory());
        List<String> items = new ArrayList<>();
        items.add(ALL);
        items.addAll(cats);
        categoryBox.getItems().setAll(items);
        categoryBox.setValue(items.contains(selected) ? selected : ALL);
    }

    private List<String> formCategories() {
        List<String> list = new ArrayList<>(categoryBox.getItems());
        list.remove(ALL);
        return list;
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = categoryBox.getValue();
        boolean onlyLow = lowStockCheck.isSelected();
        filtered.setPredicate(p ->
                (p.getName().toLowerCase().contains(text)
                        || p.getSupplier().toLowerCase().contains(text))
                        && (cat == null || ALL.equals(cat) || cat.equals(p.getCategory()))
                        && (!onlyLow || p.isLowStock()));
        countLabel.setText("Показано: " + filtered.size() + " из " + products.size());
    }

    private void updateStats() {
        double total = 0;
        int low = 0;
        for (Product p : products) {
            total += p.getPrice() * p.getQuantity();
            if (p.isLowStock()) low++;
        }
        totalLabel.setText(String.valueOf(products.size()));
        valueLabel.setText(String.format("%,.0f ₸", total));
        lowLabel.setText(String.valueOf(low));
    }

    private void afterChange() {
        updateStats();
        refreshCategories();
        applyFilter();
        table.refresh();
    }

    // ---------- действия ----------

    @FXML
    private void onAdd() {
        Product p = openForm(null);
        if (p != null) {
            p.setId(nextId());
            products.add(p);
            afterChange();
            setStatus("Добавлен товар: " + p.getName());
        }
    }

    @FXML
    private void onEdit() {
        Product sel = selected();
        if (sel == null) return;
        if (openForm(sel) != null) {
            afterChange();
            setStatus("Изменён товар: " + sel.getName());
        }
    }

    @FXML
    private void onDelete() {
        Product sel = selected();
        if (sel == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить товар «" + sel.getName() + "»?", ButtonType.YES, ButtonType.NO);
        a.setTitle("Подтверждение");
        a.setHeaderText(null);
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            products.remove(sel);
            afterChange();
            setStatus("Удалён товар: " + sel.getName());
        }
    }

    @FXML private void onReceipt() { showStock(true); }
    @FXML private void onWriteOff() { showStock(false); }

    @FXML
    private void onResetFilter() {
        searchField.clear();
        categoryBox.setValue(ALL);
        lowStockCheck.setSelected(false);
        setStatus("Фильтры сброшены");
    }

    @FXML
    private void onSave() {
        try {
            CsvStorage.save(DATA_FILE, products);
            setStatus("Сохранено в " + DATA_FILE.toAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка сохранения",
                    "Не удалось сохранить файл: " + e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        saveQuietly();
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        showAlert(Alert.AlertType.INFORMATION, "О программе",
                "Учёт склада (Inventory Management)\nСРО №1, Визуальное программирование\nJavaFX 21");
    }

    /** Сохранение без окон-сообщений (при закрытии программы). */
    public void saveQuietly() {
        try {
            CsvStorage.save(DATA_FILE, products);
        } catch (IOException ignored) {
        }
    }

    // ---------- окна ----------

    private Product openForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/kz/atu/inventory/product-form.fxml"));
            Parent root = loader.load();
            ProductFormController ctrl = loader.getController();
            ctrl.setup(product, formCategories());
            showModal(root, product == null ? "Новый товар" : "Редактирование товара");
            return ctrl.isSaved() ? ctrl.getProduct() : null;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть форму: " + e.getMessage());
            return null;
        }
    }

    private void showStock(boolean receipt) {
        Product sel = selected();
        if (sel == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/kz/atu/inventory/stock-dialog.fxml"));
            Parent root = loader.load();
            StockDialogController ctrl = loader.getController();
            ctrl.setup(sel, receipt);
            showModal(root, receipt ? "Приход товара" : "Списание товара");
            if (ctrl.isApplied()) {
                afterChange();
                setStatus((receipt ? "Приход: " : "Списание: ") + ctrl.getAmount()
                        + " шт., " + sel.getName() + ". Остаток: " + sel.getQuantity());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно: " + e.getMessage());
        }
    }

    private void showModal(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(table.getScene().getStylesheets());
        stage.setScene(scene);
        stage.initOwner(table.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }

    // ---------- вспомогательные ----------

    private Product selected() {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Сначала выберите товар в таблице.");
        }
        return sel;
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    static void showAlert(Alert.AlertType type, String title, String text) {
        Alert a = new Alert(type, text);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}