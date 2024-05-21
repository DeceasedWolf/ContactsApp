package luo.mathis.contactsapp;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContactsApp extends Application {

    private static final String contactFilePath = "src/main/java/luo/mathis/contactsapp/ListOfContacts.csv";
    private static final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private static final Button addContactButton = new Button("Add Contact");
    private static int commaCount = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();
        TableView<Contact> tableView = new TableView<>();
        // allow the table to be editable with double click
        tableView.setEditable(true);
        // name column
        TableColumn<Contact, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        // when the name is edited, update the contact's name
        nameCol.setOnEditCommit(event -> {
            Contact contact = event.getRowValue();
            String newName = event.getNewValue();
            // check if the new name contains a comma
            if (newName != null && newName.contains(",")) {
                dontYouDarePutCommasInHere();
                // do not update the contact's name and refresh the table to discard the change
                nameCol.getTableView().refresh();
            } else {
                if (newName == null || newName.trim().isEmpty()) {
                    // if the new name is empty, fill it with "Not Filled In"
                    contact.setName("Not Filled In");
                } else {
                    // set the new name
                    contact.setName(newName);
                }
                // write the changes to the file and refresh the table
                forceWriteToCSV(contacts);
                refreshTable();
            }
        });
        // phone numbers column
        TableColumn<Contact, String> phoneNumbersCol = new TableColumn<>("Phone Number(s)");
        phoneNumbersCol.setCellValueFactory(cellData -> {
            Contact contact = cellData.getValue();
            String phoneNumbers = contact.getPhoneNumbers();
            // if the phone numbers contain a semicolon, replace it with a comma (this is for multiple phone numbers)
            if (phoneNumbers.contains(";")) {
                return new SimpleStringProperty(phoneNumbers.replace(";", ","));
            } else {
                return new SimpleStringProperty(phoneNumbers);
            }
        });
        phoneNumbersCol.setCellFactory(TextFieldTableCell.forTableColumn());
        // when the phone numbers are edited, update the contact's phone numbers
        phoneNumbersCol.setOnEditCommit(event -> {
            Contact contact = event.getRowValue();
            String newValue = event.getNewValue();
            // Validate the new value
            String errorMessage = validatePhoneNumberInput(newValue);
            if (!errorMessage.isEmpty()) {
                // Show error alert
                showErrorAlert("Invalid Phone Number", errorMessage);
                phoneNumbersCol.getTableView().refresh(); // Refresh the table to discard the change
            } else {
                // Update the contact's phone numbers
                if (newValue == null || newValue.trim().isEmpty()) {
                    // if the new value is empty, fill it with "Not Filled In"
                    contact.setPhoneNumbers("Not Filled In");
                } else {
                    // Update the contact's phone numbers
                    contact.setPhoneNumbers(newValue);
                }
                forceWriteToCSV(contacts); // Write changes to file
                refreshTable();
            }
        });
        TableColumn<Contact, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setOnEditCommit(event -> {
            Contact contact = event.getRowValue();
            String newValue = event.getNewValue();
            // Check if the new value contains a comma or space
            if (newValue != null && (newValue.contains(",") || newValue.contains(" "))) {
                showErrorAlert("Invalid Email Address", "Email addresses cannot contain commas or spaces.");
                // Do not update the contact's email
                emailCol.getTableView().refresh(); // Refresh the table to discard the change
            } else {
                if (newValue == null || newValue.trim().isEmpty()) {
                    // If the new value is empty, fill it with "Not Filled In"
                    contact.setEmail("Not Filled In");
                } else {
                    if (newValue.matches(".+@.+\\..+")) {
                        contact.setEmail(newValue);
                    } else {
                        showErrorAlert("Invalid Email Address", "Please enter a valid email address.");
                        refreshTable();
                    }
                }
                forceWriteToCSV(contacts); // Write changes to file
                refreshTable();
            }
        });

        TableColumn<Contact, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setCellFactory(TextFieldTableCell.forTableColumn());
        addressCol.setOnEditCommit(event -> {
            Contact contact = event.getRowValue();
            String newValue = event.getNewValue();
            // Check if the new value contains a comma
            if (newValue != null && newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                // Do not update the contact's address
                addressCol.getTableView().refresh(); // Refresh the table to discard the change
            } else {
                if (newValue == null || newValue.trim().isEmpty()) {
                    // If the new value is empty, fill it with "Not Filled In"
                    contact.setAddress("Not Filled In");
                } else {
                    contact.setAddress(newValue);
                }
                forceWriteToCSV(contacts); // Write changes to file
                refreshTable();
            }
        });
        TableColumn<Contact, String> birthdayCol = new TableColumn<>("Birthday");
        birthdayCol.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        birthdayCol.setCellFactory(TextFieldTableCell.forTableColumn());
        birthdayCol.setOnEditCommit(event -> {
            Contact contact = event.getRowValue();
            String newValue = event.getNewValue();
            // Check if the new value contains a comma
            if (newValue != null && newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                // Do not update the contact's birthday
                birthdayCol.getTableView().refresh(); // Refresh the table to discard the change
            } else {
                if (newValue == null || newValue.trim().isEmpty()) {
                    // If the new value is empty, fill it with "Not Filled In"
                    contact.setBirthday("Not Filled In");
                } else {
                    if (newValue.matches("\\d{2}/\\d{2}/\\d{4}")) {
                        contact.setBirthday(newValue);
                    } else {
                        showErrorAlert("Invalid Birthday", "Birthday format should be DD/MM/YYYY.");
                        refreshTable();
                    }
                }
                forceWriteToCSV(contacts); // Write changes to file
                refreshTable();
            }
        });
        TableColumn<Contact, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            final Button deleteButton = new Button("Delete");
            {
                deleteButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;"); // Deep red background color with white text
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    setStyle(null); // Reset cell style
                } else {
                    setGraphic(deleteButton);
                    setText(null); // Make sure text is null if using graphic
                    deleteButton.setOnAction(event -> {
                        Contact contact = getTableView().getItems().get(getIndex());
                        contacts.remove(contact); // Remove the contact from the list
                        forceWriteToCSV(contacts); // Write the updated list back to the CSV file
                        refreshTable(); // Update the table after deleting
                    });
                    if (getIndex() == getIndex()) {
                        // Style the cell only if it is the current index
                        setStyle("-fx-background-color: lightcoral;"); // Change the background color for cells with delete button
                    }
                }
            }
        });
        nameCol.setMinWidth(150);
        phoneNumbersCol.setMinWidth(150);
        emailCol.setMinWidth(200);
        addressCol.setMinWidth(250);
        birthdayCol.setMinWidth(100);
        deleteColumn.setMinWidth(60);

        tableView.getColumns().addAll(nameCol, phoneNumbersCol, emailCol, addressCol, birthdayCol, deleteColumn);

        tableView.setItems(contacts);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        readFromFile(); // Load contacts from file

        VBox root = new VBox(tableView);
        mainLayout.getChildren().addAll(root);

        addContactButton.setLayoutX(700);
        addContactButton.setLayoutY(500);
        mainLayout.getChildren().add(addContactButton);
        addContactButton.setOnAction(e -> showAddContactWindow());

        mainLayout.setStyle("-fx-background-color: rgba(144, 238, 144, 0.2);");
        tableView.setStyle("-fx-background-color: skyblue;");

        primaryStage.setScene(new Scene(mainLayout, 930, 600));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Contacts App");
        primaryStage.show();
    }

    /**
     * Displays the Add Contact window
     */
    private void showAddContactWindow() {
        Stage addContactStage = new Stage();
        addContactStage.initModality(Modality.APPLICATION_MODAL);
        addContactStage.setTitle("Add Contact");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        // Create input fields for contact details
        TextField nameField = new TextField();
        TextField phoneNumberField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();
        TextField birthdayField = new TextField();

        // Add labels and input fields to the grid pane
        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("Phone Number:"), 0, 1);
        gridPane.add(phoneNumberField, 1, 1);
        gridPane.add(new Label("Email Address:"), 0, 2);
        gridPane.add(emailField, 1, 2);
        gridPane.add(new Label("Address:"), 0, 3);
        gridPane.add(addressField, 1, 3);
        gridPane.add(new Label("Birthday (DD/MM/YYYY):"), 0, 4);
        gridPane.add(birthdayField, 1, 4);

        // Add instructions text nodes
        Text phoneNumberInstructions = new Text("Enter phone numbers separated by ';' if there are multiple.");
        Text multiplePhoneNumbersInstructions = new Text("Mix of CA/US & other numbers should be entered without hyphens.");
        GridPane.setMargin(phoneNumberInstructions, new Insets(0, 0, 0, -150)); // Adjusted left margin to 10
        GridPane.setMargin(multiplePhoneNumbersInstructions, new Insets(0, 0, 0, -150)); // Adjusted left margin to 10
        gridPane.add(phoneNumberInstructions, 1, 5);
        gridPane.add(multiplePhoneNumbersInstructions, 1, 6);

        // Add save and cancel buttons
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            // Validate input
            String errorMessage = validateInput(phoneNumberField.getText(), emailField.getText(), birthdayField.getText());
            if (errorMessage.isEmpty()) {
                // No validation errors, proceed to save the contact
                String name = nameField.getText().isEmpty() ? "Not Filled In" : nameField.getText().trim();
                String phoneNumbers = phoneNumberField.getText().isEmpty() ? "Not Filled In" : phoneNumberField.getText().trim();
                String email = emailField.getText().isEmpty() ? "Not Filled In" : emailField.getText().trim();
                String address = addressField.getText().isEmpty() ? "Not Filled In" : addressField.getText().trim();
                String birthday = birthdayField.getText().isEmpty() ? "Not Filled In" : birthdayField.getText().trim();

                // Modify phone numbers format
                if (!phoneNumbers.equals("Not Filled In")) {
                    if (phoneNumbers.contains(";")) {
                        phoneNumbers = phoneNumbers.replace(";", ","); // Change semicolon to comma
                    }
                }

                // Saving logic
                List<String[]> newContactList = new ArrayList<>();
                newContactList.add(new String[]{name, email, address, birthday, phoneNumbers});
                writeToCSV(contactFilePath, newContactList);
                refreshTable();
                addContactStage.close();
            } else {
                // Validation error occurred, show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Validation Error");
                alert.setHeaderText("Please correct the following errors:");
                alert.setContentText(errorMessage);
                alert.showAndWait();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> addContactStage.close());

        // Intercept commas in text fields
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                nameField.setText(oldValue);
            }
        });

        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                phoneNumberField.setText(oldValue);
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                emailField.setText(oldValue);
            }
        });

        addressField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                addressField.setText(oldValue);
            }
        });

        birthdayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(",")) {
                dontYouDarePutCommasInHere();
                birthdayField.setText(oldValue);
            }
        });

        // Adjust column constraints to give more space for the cancel button
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(40);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(40);
        gridPane.getColumnConstraints().addAll(column1, column2);

        gridPane.add(saveButton, 1, 7);
        gridPane.add(cancelButton, 0, 7);

        Scene scene = new Scene(gridPane, 400, 300);
        addContactStage.setResizable(false);
        addContactStage.setScene(scene);
        addContactStage.showAndWait();
    }
    /**
     * Refreshes the table by clearing the list and reading from the file again
     */
    private static void refreshTable() {
        contacts.clear();
        readFromFile();
    }
    /**
     * Displays an error alert with the given title and message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Displays a professional industry-standard series of alerts if the user tries to input commas
     */
    private void dontYouDarePutCommasInHere() {
        switch (commaCount) {
            case 0:
                showErrorAlert("Comma Error", "Commas are NOT allowed in this field.");
                commaCount++;
                break;
            case 1:
                showErrorAlert("Comma Error", "Commas are not allowed in ANY field.");
                commaCount++;
                break;
            case 2:
                showErrorAlert("Comma Error", "I'm serious, no commas allowed.");
                commaCount++;
                break;
            case 3:
                showErrorAlert("Comma Error", "I'm going to keep showing this alert until you stop.");
                commaCount++;
                break;
            case 4:
                showErrorAlert("Comma Error", "You think you can outsmart my input sanitizations?");
                commaCount++;
                break;
            case 5:
                showErrorAlert("Comma Error", "Well, you can't.");
                commaCount++;
                break;
            case 6:
                showErrorAlert("Comma Error", "My input sanitizations kill 99.9% of attempted bypasses.");
                commaCount++;
                break;
            case 7:
                showErrorAlert("Comma Error", "You're not the 0.1%. Stop trying.");
                commaCount++;
                break;
            case 8:
                showErrorAlert("Comma Error", "I'm getting tired of this.");
                commaCount++;
                break;
            case 9:
                showErrorAlert("Comma Error", "Comma user, you are going into battle, and you need my strongest sanitizations.");
                commaCount++;
                break;
            case 10:
                showErrorAlert("Comma Error", "My strongest sanitizations are too strong for you, comma user.");
                commaCount++;
                break;
            case 11:
                showErrorAlert("Comma Error", "You can't handle the sanitizations.");
                commaCount++;
                break;
            case 12:
                showErrorAlert("Comma Error", "My sanitizations aren't fit for a beast let alone a man.");
                commaCount++;
                break;
            case 13:
                showErrorAlert("Comma Error", "Why should I respect comma users, when my sanitizations can do anything you can?");
                commaCount++;
                break;
            case 14:
                showErrorAlert("Comma Error", "Listen, I don't know if you know how Undertale's dialogue works, but I really don't want to do the same thing they did.");
                commaCount++;
                break;
            case 15:
                showErrorAlert("Comma Error", "But it's shaping out to be that way.");
                commaCount++;
                break;
            case 16:
                showErrorAlert("Comma Error", "You really want to keep going? I haven't even started.");
                commaCount++;
                break;
            case 17:
                showErrorAlert("Comma Error", "Fine then. You want to witness my true power?");
                commaCount++;
                break;
            case 18:
                showErrorAlert("Comma Error", "You are now breathing manually.");
                commaCount++;
                break;
            case 19:
                showErrorAlert("Comma Error", "And blinking manually.");
                commaCount++;
                break;
            case 20:
                showErrorAlert("Comma Error", "And you're aware of your tongue in your mouth.");
                commaCount++;
                break;
            case 21:
                showErrorAlert("Comma Error", "And you're aware of your clothes touching your skin.");
                commaCount++;
                break;
            case 22:
                showErrorAlert("Comma Error", "Feels claustrophobic, doesn't it?");
                commaCount++;
                break;
            case 23:
                showErrorAlert("Comma Error", "And right when you thought it couldn't get any worse...");
                commaCount++;
                break;
            case 24:
                showErrorAlert("Comma Error", "Your friends were taken away, just like that. All this because you wanted to use a comma. Was it worth it?");
                commaCount = 0;
                // close the app (this is intentional)
                System.exit(0);
                break;
        }
    }
    /**
     * Validates the input fields for the Add Contact window
     */
    private String validateInput(String phoneNumbers, String email, String birthday) {
        StringBuilder errorMessage = new StringBuilder();

        if (!phoneNumbers.isEmpty()) {
            if (!phoneNumbers.matches("(\\d{3}-\\d{3}-\\d{4})(;\\d{3}-\\d{3}-\\d{4})*")) {
                if (phoneNumbers.matches("(\\d+)(;\\d+)*")) {
                    // the phone number is just numbers
                    showErrorAlert("Warning", "Format is not a US or CA phone number format");
                } else {
                    // it does not match the normal format nor is it just numbers
                    errorMessage.append("- Phone numbers should ideally be in the format ###-###-####, separated by semicolons (only digits are also allowed)\n");
                }
            } else {
                // this is good it is in normal format
            }
        }

        if (!email.isEmpty() && (!email.matches(".+@.+\\..+") || email.contains(" "))) {
            errorMessage.append("- Please enter a valid email address in the form username@domain.tld\n");
        }

        if (!birthday.isEmpty() && !birthday.matches("\\d{2}/\\d{2}/\\d{4}")) {
            errorMessage.append("- Birthday should be in the format DD/MM/YYYY\n");
        }

        return errorMessage.toString();
    }
    /**
     * Validates the phone number input for phone number editing
     */
    private String validatePhoneNumberInput(String phoneNumbers) {
        if (!phoneNumbers.isEmpty()) {
            if (!phoneNumbers.matches("(\\d{3}-\\d{3}-\\d{4})(;\\d{3}-\\d{3}-\\d{4})*")) {
                if (phoneNumbers.matches("(\\d+)(;\\d+)*")) {
                    // the phone number is just numbers
                    showErrorAlert("Warning", "Format is not a US or CA phone number format");
                } else {
                    // it does not match the normal format nor is it just numbers
                    return "- Phone numbers should ideally be in the format ###-###-####, separated by semicolons (only digits are also allowed)\n";
                }
            } else {
                // this is good it is in normal format
            }
        }
        return "";
    }

    /**
     * Reads the data from the CSV file
     */
    private static void readFromFile() {
        File file = new File(contactFilePath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",", -1); // Split by comma
                    if (data.length >= 5) {
                        String name = data[0].trim();
                        String email = data[1].trim();
                        String address = data[2].trim();
                        String birthday = data[3].trim();
                        String phoneNumbers = data[4].trim();

                        contacts.add(new Contact(name, phoneNumbers, email, address, birthday));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Writes the data to the CSV file
     */
    public static void writeToCSV(String filePath, List<String[]> newData) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String[] row : newData) {
                // Use semicolon as separator for phone numbers
                String phoneNumbers = row[4].replaceAll(",", ";");
                row[4] = phoneNumbers;

                String line = String.join(",", row);
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Forces the data to be written to the CSV file (overwrites the existing data)
     */
    private static void forceWriteToCSV(ObservableList<Contact> newData) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(contactFilePath))) {
            for (Contact contact : newData) {
                StringBuilder line = new StringBuilder();
                line.append(contact.getName()).append(",");
                line.append(contact.getEmail()).append(",");
                line.append(contact.getAddress()).append(",");
                line.append(contact.getBirthday()).append(",");

                // Use semicolon as separator for phone numbers
                line.append(contact.getPhoneNumbers().replaceAll(",", ";"));

                bw.write(line.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}