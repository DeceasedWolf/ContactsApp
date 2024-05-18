module luo.mathis.contactsapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens luo.mathis.contactsapp to javafx.fxml;
    exports luo.mathis.contactsapp;
}