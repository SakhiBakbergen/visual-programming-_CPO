package kz.atu.inventory;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StockDialogController {

    @FXML private Label headerLabel;
    @FXML private Label productLabel;
    @FXML private Label currentLabel;
    @FXML private TextField amountField;
    @FXML private TextField commentField;
    @FXML private Button applyButton;

    private Product product;
    private boolean receipt;
    private boolean applied;
    private int amount;

    public void setup(Product p, boolean receipt) {
        this.product = p;
        this.receipt = receipt;
        headerLabel.setText(receipt ? "Приход товара" : "Списание товара");
        applyButton.setText(receipt ? "Принять" : "Списать");
        productLabel.setText(p.getName());
        currentLabel.setText(p.getQuantity() + " шт.");
    }

    @FXML
    private void onApply() {
        try {
            amount = Integer.parseInt(amountField.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            error("Количество должно быть целым числом.");
            return;
        }
        if (amount <= 0) {
            error("Количество должно быть больше нуля.");
            return;
        }
        if (!receipt && amount > product.getQuantity()) {
            error("Нельзя списать больше, чем есть на складе (" + product.getQuantity() + " шт.).");
            return;
        }
        product.setQuantity(product.getQuantity() + (receipt ? amount : -amount));
        applied = true;
        amountField.getScene().getWindow().hide();
    }

    @FXML
    private void onCancel() {
        amountField.getScene().getWindow().hide();
    }

    private void error(String text) {
        MainController.showAlert(Alert.AlertType.ERROR, "Ошибка ввода", text);
    }

    public boolean isApplied() { return applied; }
    public int getAmount() { return amount; }
}