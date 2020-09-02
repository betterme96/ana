package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Main extends Application {
    private LineChart chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;




    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Controller controller = fxmlLoader.getController();
        primaryStage.setScene(new Scene(fxmlLoader.load(), 750,500));
        primaryStage.setResizable(true);
        primaryStage.show();
    }


    public static void main(String[] args) { launch(args);}
}
