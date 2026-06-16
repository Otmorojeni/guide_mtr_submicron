package com.guide;

import com.guide.views.MainStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Создаем объект интерфейса и запускаем его отображение
        new MainStage().show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}