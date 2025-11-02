package aydin.firebasedemo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class PrimaryController {

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField ageTextField;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextArea outputTextArea;

    @FXML
    private Button readButton;

    @FXML
    private Button writeButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button switchSecondaryViewButton;

    private final ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    private boolean key;

    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    void initialize() {
        AccessDataView accessDataViewModel = new AccessDataView();
        nameTextField.textProperty().bindBidirectional(accessDataViewModel.personNameProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }

    // --- FIREBASE OPERATIONS --- //

    @FXML
    void writeButtonClicked(ActionEvent event) {
        addData();
    }

    @FXML
    void readButtonClicked(ActionEvent event) {
        readFirebase();
    }

    @FXML
    void registerButtonClicked(ActionEvent event) {
        registerUser();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        DemoApp.setRoot("secondary");
    }

    /**
     * Reads all persons from Firestore (including phone).
     */
    public boolean readFirebase() {
        key = false;
        ApiFuture<QuerySnapshot> future = DemoApp.fstore.collection("Persons").get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            outputTextArea.clear();

            if (documents.isEmpty()) {
                outputTextArea.setText("No data found in Firestore.");
                return false;
            }

            outputTextArea.appendText("=== People in Firestore ===\n");
            listOfUsers.clear();

            for (QueryDocumentSnapshot doc : documents) {
                String name = String.valueOf(doc.getData().get("Name"));
                int age = Integer.parseInt(doc.getData().get("Age").toString());
                String phone = String.valueOf(doc.getData().get("Phone"));

                outputTextArea.appendText(
                        String.format("%s, Age: %d, Phone: %s\n", name, age, phone)
                );

                person = new Person(name, age, phone);
                listOfUsers.add(person);
            }

            key = true;

        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            outputTextArea.appendText("Error reading from Firestore.\n");
        }
        return key;
    }

    /**
     * Adds a new person to Firestore (Name, Age, Phone).
     */
    public void addData() {
        try {
            String name = nameTextField.getText();
            int age = Integer.parseInt(ageTextField.getText());
            String phone = txtPhone.getText();

            if (name.isEmpty() || phone.isEmpty()) {
                showAlert("Error", "Name and Phone cannot be empty.");
                return;
            }

            DocumentReference docRef = DemoApp.fstore.collection("Persons")
                    .document(UUID.randomUUID().toString());

            Map<String, Object> data = new HashMap<>();
            data.put("Name", name);
            data.put("Age", age);
            data.put("Phone", phone);

            ApiFuture<WriteResult> result = docRef.set(data);
            result.get(); // wait for confirmation
            outputTextArea.appendText("Added person: " + name + ", Age: " + age + ", Phone: " + phone + "\n");

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for age.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error adding data to Firestore: " + e.getMessage());
        }
    }

    /**
     * Creates a new Firebase user in Authentication.
     */
    public boolean registerUser() {
        try {
            // Example: For now we’ll just use static data or text field data
            String email = nameTextField.getText().trim() + "@example.com";
            String password = "password123";
            String phone = "+1" + txtPhone.getText().trim();

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setPassword(password)
                    .setPhoneNumber(phone)
                    .setDisplayName(nameTextField.getText().trim())
                    .setDisabled(false);

            UserRecord userRecord = DemoApp.fauth.createUser(request);

            outputTextArea.appendText("Successfully created new user: " + userRecord.getUid() + "\n");
            System.out.println("Firebase UID: " + userRecord.getUid());

            return true;
        } catch (FirebaseAuthException ex) {
            System.out.println("Error creating a new user in Firebase: " + ex.getMessage());
            showAlert("Firebase Error", "Error creating new user: " + ex.getMessage());
            return false;
        }
    }

    // --- HELPER --- //
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
