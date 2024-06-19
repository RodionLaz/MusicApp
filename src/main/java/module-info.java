module com.example.musicapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires mp3agic;
    requires java.logging;

    opens com.example.musicapp to javafx.fxml;
    exports com.example.musicapp;
}