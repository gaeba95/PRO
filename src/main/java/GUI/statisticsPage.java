/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GUI;

import Statistics.components.Month;
import Statistics.parser.statParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.*;

import animalType.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import Statistics.handler.StatisticsHandler;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

/**
 *
 * @author Marion
 */
public class statisticsPage extends JFrame {
    
    private statParser parser;
    private StatisticsHandler statHandler = new StatisticsHandler();
    private String dayConfig = "1";
    private Month monthConfig = Month.values()[0];
 
    public statisticsPage() {
        // Recuperation des donnees du parser
        parser = new statParser(statHandler);
        parser.parseFile();
        statHandler.analyzeData();
        //System.out.println(statHandler.test());
        //Map <Integer, List<Integer>> test = statHandler.getAnimalTypeByHourMap(Month.FEB, 23);
        
        //System.out.println(test);
        statistics();
    }


    public void initAndShowGUI() {
        
            JFrame mainFrame = new JFrame("Crapauduc Viewer Statistics");
            
            JFXPanel fxPanel = new JFXPanel();
            
            mainFrame.setAlwaysOnTop(true);
            mainFrame.add(fxPanel);
            mainFrame.setSize(1900, 1000);
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            mainFrame.getToolkit().setDynamicLayout(true);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    initFX(fxPanel);
                }
            });
    }
    
    private Scene createScene() {
        
        Group root = new Group();
        Color backgroundColor = Color.rgb(110, 25, 222);
        Scene scene = new Scene(root, backgroundColor);
        scene.getStylesheets().add("chartsStyle.css");

        /*
        * Creation de la Pie Chart principale
        */
        Group pieChartGroup = createMainPieChart();
        root.getChildren().add(pieChartGroup);

        /*
         * Creation de la zone d'infos droite
         */
        Group sideInfosGroup = createSideInfosZone();
        root.getChildren().add(sideInfosGroup);
        
        /*
         * Creation de la Line Chart par ann�e
         */
        Group lineChartGroup = createLineChart();
        root.getChildren().add(lineChartGroup);
        
        /*
         * Creation de la Bar Chart par ann�e
         */
        Group barChartGroup = createBarChart();
        root.getChildren().add(barChartGroup);
        
        /*
        * Creation de la zone de g�n�ration dynamique
        */
        Group dynamGroup = createDynamGroup();
        root.getChildren().add(dynamGroup);

        return (scene);
    }
    
    /*
     * Cree un groupe avec la zone de generation dynamique
     */
    public Group createDynamGroup() {
        Group dynamGroup = new Group();
        
        /* BOUTON DE CHOIX MOIS */
        List<String> monthSelector = new ArrayList<String>();         
        for (Month m : Month.values()){
          monthSelector.add(m.getName());
        }
        ChoiceBox monthBox = new ChoiceBox(FXCollections.observableArrayList(monthSelector));
        
        monthBox.setLayoutX(1700);
        monthBox.setLayoutY(425);
        
        // Evenements suite aux choix
        monthBox.getSelectionModel().selectedIndexProperty().addListener(new 
            ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number value, 
                    Number new_value) {
                // Enregistrement du choix
                monthConfig = Month.values()[new_value.intValue()];
                // Repaint de la fenetre
                statistics();
            }
        });
        dynamGroup.getChildren().add(monthBox);
        
        /* LINE CHART MOIS*/
        // Definition des axes
        NumberAxis xAxisLineMonth = new NumberAxis(1, monthConfig.getNbDays(), 1);
        NumberAxis yAxisLineMonth = new NumberAxis();
        LineChart<Number, Number> monthLineChart = new LineChart<>(xAxisLineMonth, yAxisLineMonth);
        monthLineChart.setTitle("Number of animals for " + monthConfig.getName());
        monthLineChart.setPrefHeight(300);
        
        // Positionnement a gauche de la fenetre
        monthLineChart.setLayoutX(550);
        monthLineChart.setLayoutY(350);
        monthLineChart.setLegendVisible(false);
//   
        XYChart.Series monthLineSeries = new XYChart.Series();   
        
        Map<Integer, Integer> mapThisMonth = statHandler.getAnimalNbByDayMap(monthConfig); 
        for (Map.Entry<Integer, Integer> entry : mapThisMonth.entrySet()) {
           monthLineSeries.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
        }
        monthLineChart.getData().add(monthLineSeries);
        dynamGroup.getChildren().add(monthLineChart);

        
        /* BAR CHART MOIS */
        final CategoryAxis xAxisBarMonth = new CategoryAxis();
        final NumberAxis yAxisBarMonth = new NumberAxis();
        
        StackedBarChart<String, Number> sbcMonth = new StackedBarChart<>(xAxisBarMonth, yAxisBarMonth);
        sbcMonth.setPrefHeight(300);
        sbcMonth.setLayoutX(1050);
        sbcMonth.setLayoutY(375);
        sbcMonth.setLegendVisible(false);        
        
         for (AnimalType a : AnimalType.values()){               
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName(a.getName());          
            sbcMonth.getData().add(serie);
        }
          
         
        Map<Integer, List<Integer>> mapThisMonthByType = statHandler.getAnimalTypeByDayMap(monthConfig); 
         for (Map.Entry<Integer, List<Integer>> entry : mapThisMonthByType.entrySet()) {
            List<Integer> list = entry.getValue();
            for (AnimalType animal : AnimalType.values()) {
                XYChart.Series<String, Number> serie = sbcMonth.getData().get(animal.ordinal());
                serie.getData().add(new XYChart.Data<>(entry.getKey().toString(), list.get(animal.ordinal())));
            }
        }
               
        dynamGroup.getChildren().add(sbcMonth);    
        
        
        /********************************************/
        
        /* BOUTON DE CHOIX JOUR */
        List<String> daySelector = new ArrayList<String>();         
        for (int day = 1; day <= monthConfig.getNbDays(); day++){
          daySelector.add(Integer.toString(day));
        }
        ChoiceBox dayBox = new ChoiceBox(FXCollections.observableArrayList(daySelector));
        dayBox.setLayoutX(1700);
        dayBox.setLayoutY(700);               
        
        // Evenements suite aux choix
        dayBox.getSelectionModel().selectedIndexProperty().addListener(new 
            ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number value, 
                    Number new_value) {
                // Enregistrement du choix
                dayConfig = daySelector.get(new_value.intValue());  
                // Repaint de la fenetre
                statistics();
            }
        });
        dynamGroup.getChildren().add(dayBox);
        
        /* LINE CHART JOUR */   
        NumberAxis xAxisLineDay = new NumberAxis(0, 23, 1);
        NumberAxis yAxisLineDay = new NumberAxis();
        LineChart<Number, Number> dayLineChart = new LineChart<>(xAxisLineDay, yAxisLineDay);
        dayLineChart.setTitle("Number of animals for the " + dayConfig + " of " + monthConfig.getName() );
        dayLineChart.setPrefHeight(300);
        dayLineChart.setLayoutX(550);
        dayLineChart.setLayoutY(675);
        dayLineChart.setLegendVisible(false);
   
        XYChart.Series dayLineSeries = new XYChart.Series();   
        
        Map<Integer, Integer> mapThisDay = statHandler.getAnimalNbByHourMap(monthConfig, Integer.parseInt(dayConfig)); 
        for (Map.Entry<Integer, Integer> entry : mapThisDay.entrySet()) {
           dayLineSeries.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
        }
        dayLineChart.getData().add(dayLineSeries);
        dynamGroup.getChildren().add(dayLineChart);
        
          
        /* BAR CHART JOUR */        
        final CategoryAxis xAxisBarDay = new CategoryAxis();
        final NumberAxis yAxisBarDay = new NumberAxis();
        
        StackedBarChart<String, Number> sbcDay = new StackedBarChart<>(xAxisBarDay, yAxisBarDay);
        sbcDay.setPrefHeight(300);
        sbcDay.setLayoutX(1050);
        sbcDay.setLayoutY(700);
        sbcDay.setLegendVisible(false);
        
         for (AnimalType a : AnimalType.values()){               
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName(a.getName());          
            sbcDay.getData().add(serie);
        }
           
        Map<Integer, List<Integer>> mapThisDayByType = statHandler.getAnimalTypeByHourMap(monthConfig,Integer.parseInt(dayConfig)); 
         for (Map.Entry<Integer, List<Integer>> entry : mapThisDayByType.entrySet()) {
            List<Integer> list = entry.getValue();
            for (AnimalType animal : AnimalType.values()) {
                XYChart.Series<String, Number> serie = sbcDay.getData().get(animal.ordinal());
                serie.getData().add(new XYChart.Data<>(entry.getKey().toString(), list.get(animal.ordinal())));
            }
        }
        dynamGroup.getChildren().add(sbcDay);        
        
        return dynamGroup;
    }
    
    /*
     * Cree un groupe avec la pie chart principale
     */
    public Group createMainPieChart() {
        
        Group pieChartGroup = new Group();
        
        // Recuperation du nombre d'animaux
        int totalNbOfAnimals = statHandler.getTaggedAnimals();
        int nbOfTritons = statHandler.getAnimalTypeCounter().get(AnimalType.TRITON);
        int nbOfToads = statHandler.getAnimalTypeCounter().get(AnimalType.GRENOUILLE);
        int nbOfFrogs = statHandler.getAnimalTypeCounter().get(AnimalType.CRAPAUD);
        int nbOfOther = statHandler.getAnimalTypeCounter().get(AnimalType.AUTRE);
        
        // Creation d'une liste de donnees a mettre dans la pie chart
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                new PieChart.Data(AnimalType.TRITON.getName(), nbOfTritons),
                new PieChart.Data(AnimalType.GRENOUILLE.getName(), nbOfFrogs),
                new PieChart.Data(AnimalType.CRAPAUD.getName(), nbOfToads),
                new PieChart.Data(AnimalType.AUTRE.getName(), nbOfOther));
        
        PieChart chart = new PieChart(pieChartData);
        // Positionnement a gauche de la fenetre
        chart.setLayoutX(0);
        chart.setLayoutY(0);
        chart.setTitle("Nombre d'animaux\n(" + totalNbOfAnimals + " tagged)");
        chart.setLegendVisible(false);
        chart.setTitleSide(Side.BOTTOM);

        // Couleurs des tranches
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #8EFA9C;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #3F319B;");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #180F50;");
        pieChartData.get(3).getNode().setStyle("-fx-pie-color: #807AA8;");

        // Ajout de la chart au root group
        pieChartGroup.getChildren().add(chart);
        
        return pieChartGroup;
    }
    
    /*
     * Cree un groupe avec la line chart par ann�e
     */
    public Group createLineChart() {
        Group lineChartGroup = new Group();
        
        // Definition des axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                
        lineChart.setTitle("Number of animals per month");
        // Positionnement a gauche de la fenetre
        lineChart.setLayoutX(550);
        lineChart.setLayoutY(25);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(300);
        
        XYChart.Series series = new XYChart.Series();
        
        for (Month m : Month.values()){
           series.getData().add(new XYChart.Data(m.getAbbreviation(), statHandler.getAnimalNbByMonth(m)));
        }
        
        lineChart.getData().add(series);
        lineChartGroup.getChildren().add(lineChart);
        
        return lineChartGroup;
    }
    
    /*
     * Cree un groupe avec la bar chart par ann�e
     */
    public Group createBarChart() {
        Group barChartGroup = new Group();
        
        // Definition des axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        StackedBarChart<String, Number> sbc = new StackedBarChart<>(xAxis, yAxis);
        sbc.setPrefHeight(300);
        
        List<String> xAxeNames = new ArrayList<String>();
        
        for (Month m : Month.values()){
          xAxeNames.add(m.getAbbreviation());
        }
        xAxis.setCategories(FXCollections.<String>observableArrayList(xAxeNames));

        sbc.setLayoutX(1050);
        sbc.setLayoutY(50);
        
         for (AnimalType a : AnimalType.values()){               
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName(a.getName());          
            sbc.getData().add(serie);
        }
          
        for (Month m : Month.values()) {
            List<Integer> list = statHandler.getAnimalNbByMonthByType(m);
            for (AnimalType a2 : AnimalType.values()) {
                XYChart.Series<String, Number> serie = sbc.getData().get(a2.ordinal());
                serie.getData().add(new XYChart.Data<>(m.getAbbreviation(), list.get(a2.ordinal())));
            }
        }
               
        barChartGroup.getChildren().add(sbc);        
        return barChartGroup;
    }
    
    /*
     * Cree un groupe avec la zone d'infos droite
     */
    public Group createSideInfosZone() {
        
        Group sideInfosGroup = new Group();
        
        /* IMAGES */
        Text imagesLabel = new Text();
        imagesLabel.setFont(new Font(20));
        imagesLabel.setText("IMAGES");
        imagesLabel.setX(50);
        imagesLabel.setY(550);
        imagesLabel.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(imagesLabel);

        Text imagesInfos = new Text();
        imagesInfos.setFont(new Font(10));
        imagesInfos.setText("tagged : "+ statHandler.getTaggedImages() +"\n" +
                            "untagged : XXX\n" +
                            "total count : XXX");
        imagesInfos.setX(250);
        imagesInfos.setY(530);
        imagesInfos.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(imagesInfos);
        
        /* SEQUENCES */
        Text sequencesLabel = new Text();
        sequencesLabel.setFont(new Font(20));
        sequencesLabel.setText("SEQUENCES");
        sequencesLabel.setX(50);
        sequencesLabel.setY(650);
        sequencesLabel.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(sequencesLabel);

        Text sequencesInfos = new Text();
        sequencesInfos.setFont(new Font(10));
        sequencesInfos.setText("tagged : "+ statHandler.getTaggedSequenceNumber() +"\n" +
                "untagged : XXX\n" +
                "total count : XXX\n" +
                "most captures : "+ statHandler.getMostTaggedSequence() +"\n" +
                "least captures : "+ statHandler.getLeastTaggedSequence() +"\n");
        sequencesInfos.setX(250);
        sequencesInfos.setY(630);
        sequencesInfos.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(sequencesInfos);

        /* CAMERAS */
        Text camerasLabel = new Text();
        camerasLabel.setFont(new Font(20));
        camerasLabel.setText("CAMERAS");
        camerasLabel.setX(50);
        camerasLabel.setY(750);
        camerasLabel.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(camerasLabel);

        Text camerasInfos = new Text();
        camerasInfos.setFont(new Font(10));
        camerasInfos.setText("most used : "+ statHandler.getMostUsedCamera() +"\n" +
                             "least used : "+ statHandler.getLeastUsedCamera() +"\n");
        camerasInfos.setX(250);
        camerasInfos.setY(730);
        camerasInfos.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(camerasInfos);

        /* OBSERVATIONS */
        Text observationsLabel = new Text();
        observationsLabel.setFont(new Font(20));
        observationsLabel.setText("OBSERVATIONS");
        observationsLabel.setX(50);
        observationsLabel.setY(850);
        observationsLabel.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(observationsLabel);

        Text observationsInfos = new Text();
        observationsInfos.setFont(new Font(10));
        observationsInfos.setText("most : "+ statHandler.getMostFrequentDate() +"\n" +
                                 "least : "+ statHandler.getLeastFrequentDate() +"\n");
        observationsInfos.setX(250);
        observationsInfos.setY(830);
        observationsInfos.setFill(Color.WHITE);
        sideInfosGroup.getChildren().add(observationsInfos);
        
        return sideInfosGroup;
    }
    
    private void initFX(JFXPanel fxPanel) {
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
    
    /*
     * Main entry point
     */
    public void statistics() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });
    }
}
