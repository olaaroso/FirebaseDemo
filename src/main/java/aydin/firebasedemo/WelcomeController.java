package aydin.firebasedemo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class WelcomeController {

    private final FirebaseAuth auth = DemoApp.fauth;

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Register");
            dialog.setHeaderText("Enter Email and Password (format: email,password)");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String[] parts = result.get().split(",");
                if (parts.length == 2) {
                    String email = parts[0].trim();
                    String password = parts[1].trim();

                    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                            .setEmail(email)
                            .setPassword(password);
                    auth.createUser(request);

                    showAlert("Success", "User registered successfully!");
                } else {
                    showAlert("Error", "Invalid input format.");
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Registration failed: " + e.getMessage());
        }
    }

    @FXML
    void handleSignIn(ActionEvent event) {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sign In");
            dialog.setHeaderText("Enter Email and Password (format: email,password)");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String[] parts = result.get().split(",");
                if (parts.length == 2) {
                    // Note: Firebase Admin SDK does not support verifying passwords directly.
                    // This simulates login by checking if user exists.
                    String email = parts[0].trim();
                    auth.getUserByEmail(email); // Throws exception if not found
                    showAlert("Success", "Sign-in successful!");

                    // Navigate to data access view
                    DemoApp.setRoot("primary");
                } else {
                    showAlert("Error", "Invalid input format.");
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Sign-in failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
