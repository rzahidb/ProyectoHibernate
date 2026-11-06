package org.example.proyectohibernate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.example.proyectohibernate.database.JPAutil;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("libros-view.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Registro de Libros");
        stage.setScene(scene);
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    @Override
    public void stop() {
        JPAutil.cerrar();
    }
}