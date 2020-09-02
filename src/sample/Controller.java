package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Pane pane;
    @FXML
    private VBox vbox;
    @FXML
    private HBox hbox;
    @FXML
    private Button generate_btn;
    @FXML
    private Button ana_btn;
    @FXML
    private TextField text_fileName;
    @FXML
    private TextField text_freq;


    private LineChart data_chart;
    private NumberAxis x_axis;
    private NumberAxis y_axis;
    private Series<Integer, Double> xySeries;
    private boolean isFirst = true;

    private String destPath = "./resultFile/";
    String srcFile;
    String destFile;
    String computeFile;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hbox.setAlignment(Pos.CENTER);
        HBox.setHgrow(generate_btn,Priority.ALWAYS);
        HBox.setHgrow(ana_btn,Priority.ALWAYS);

        x_axis = new NumberAxis();
        x_axis.setLabel("脉冲数");

        y_axis = new NumberAxis();
        y_axis.setLabel("μx(E)");

        data_chart = new LineChart(x_axis, y_axis);
        data_chart.setTitle("QXAFS");
        data_chart.setLayoutX(10);
        data_chart.setLayoutY(120);
        data_chart.setPrefWidth(200);

        data_chart.prefWidthProperty().bind(pane.widthProperty().subtract(20));
        data_chart.prefHeightProperty().bind(pane.heightProperty().subtract(200));
        vbox.prefHeightProperty().bind(pane.heightProperty());
        vbox.prefWidthProperty().bind(pane.widthProperty());
        pane.getChildren().add(data_chart);
    }

    public void filechoose_btn_click() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("data file");
        try {
            text_fileName.setText(fileChooser.showOpenDialog(pane.getScene().getWindow()).getPath());
            text_fileName.setEditable(false);
        }catch (NullPointerException e){
            if(text_fileName.getText().equals("")){
                text_fileName.setEditable(true);
            }
        }
    }

    public void clear_btn_click(){
        data_chart.getData().clear();
    }

    public void ana_btn_click() throws IOException, InterruptedException {
        text_freq.clear();
        srcFile = text_fileName.getText();
        if(!srcFile.endsWith(".dat")){
            showDialog("请选择二进制文件！");
        }
        String fileName = srcFile.substring(srcFile.lastIndexOf('/'), srcFile.length() - 4);
        String clearDestPath = destPath + fileName +"/";
        File dir = new File(clearDestPath);
        if(!dir.exists()){
            dir.mkdir();
        }
        destFile = clearDestPath + fileName + ".txt";
        computeFile = clearDestPath + fileName;
        System.out.println("dir:" + dir.getPath());
        System.out.println("srcFile:" + srcFile);
        System.out.println("destFile:" + destFile);
        System.out.println("computeFile:" + computeFile);
        DataAnalyze.analyzeData(srcFile, destFile, computeFile, 10,0,0);
        showDialog("文件解析成功!");

    }

    public void generate_btn_click() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int freq = text_freq.getText().equals("") ? 0 : Integer.parseInt(text_freq.getText());
                    data_chart.getData().add(createSeries(freq));
                    data_chart.setCreateSymbols(false);
                }catch (IOException e){
                    e.printStackTrace();
                }catch (NumberFormatException e){
                    showDialog("抽样频率格式错误！");
                }
            }
        });

    }


    private XYChart.Series<Integer, Double> createSeries(int freq) throws IOException {
        Series series = new XYChart.Series<>();
        String fileName = text_fileName.getText();
        System.out.println(fileName);
        File dataFile = new File(fileName);
        if(!dataFile.exists()){
            showDialog("文件不存在！");
            return null;
        }
        FileInputStream fileIn = new FileInputStream(dataFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
        String line = "";
        int count = 0;
        //表头读取
        line = br.readLine();
        while ((line = br.readLine()) != null){
            if((freq == 0 ) || (count % freq == 0 && line != " ")){
                String[] num = line.split(" ");
                series.getData().add(new Data<>(Integer.parseInt(num[0]),Double.parseDouble(num[3])));
            }
            count++;
        }
        fileIn.close();
        br.close();
        return series;
    }




    private static void showDialog(String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(info);

        alert.showAndWait();
    }
}
