package kz.atu.inventory;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ProductFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;
    @FXML private TextField minField;
    @FXML private TextField supplierField;

    private Product product;
    private boolean saved;

    public void setup(Product p, List<String> categories) {
        this.product = p;
        categoryBox.getItems().setAll(categories);
        if (p == null) {
            titleLabel.setText("Новый товар");
            qtyField.setText("0");
            minField.setText("5");
        } else {
            titleLabel.setText("Редактирование товара");
            nameField.setText(p.getName());
            categoryBox.getEditor().setText(p.getCategory());
            priceField.setText(String.valueOf(p.getPrice()));
            qtyField.setText(String.valueOf(p.getQuantity()));
            minField.setText(String.valueOf(p.getMinQuantity()));
            supplierField.setText(p.getSupplier());
        }
    }

    @FXML
    private void onSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String category = categoryBox.getEditor().getText() == null
                ? "" : categoryBox.getEditor().getText().trim();
        String supplier = supplierField.getText() == null ? "" : supplierField.getText().trim();

        if (name.isEmpty()) { error("Введите название товара."); nameField.requestFocus(); return; }
        if (category.isEmpty()) { error("Укажите категорию."); return; }

        double price;
        int qty, min;
        try {
            price = Double.parseDouble(priceField.getText().trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            error("Цена должна быть числом, например 1250.50");
            priceField.requestFocus();
            return;
        }
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            error("Остаток должен быть целым числом.");
            qtyField.requestFocus();
            return;
        }
        try {
            min = Integer.parseInt(minField.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            error("Минимальный остаток должен быть целым числом.");
            minField.requestFocus();
            return;
        }
        if (price < 0 || qty < 0 || min < 0) {
            error("Цена и количества не могут быть отрицательными.");
            return;
        }

        if (product == null) {
            product = new Product(0, name, category, price, qty, min, supplier);
        } else {
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setQuantity(qty);
            product.setMinQuantity(min);
            product.setSupplier(supplier);
        }
        saved = true;
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        nameField.getScene().getWindow().hide();
    }

    private void error(String text) {
        MainController.showAlert(Alert.AlertType.ERROR, "Ошибка ввода", text);
    }

    public boolean isSaved() { return saved; }
    public Product getProduct() { return product; }
}