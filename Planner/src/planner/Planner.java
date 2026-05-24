package planner;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Planner extends Application {
    public static Group root;
    public static Notebook myNotebook;
    public static HBox menuContainer = new HBox();
    public static Text today = new Text();
    public static Button displayToday = new Button();
    public final int WIDTH = 800;
    public final int HEIGHT = 600;
    
    @Override
    public void start(Stage primaryStage) {
        DBTools.initializeDB();
        
        // Load notebook from database
        try {
            myNotebook = DBTools.loadNotebook();
            myNotebook.loadFromDatabase();
            System.out.println("Successfully loaded data from database");
        } catch (Exception e) {
            System.err.println("Error loading from database, creating new notebook");
            e.printStackTrace();
            myNotebook = new Notebook();
            // Try to save the new notebook to database
            try {
                DBTools.saveNotebook(myNotebook);
            } catch (Exception ex) {
                System.err.println("Could not save notebook to database");
                ex.printStackTrace();
            }
        }
        
        root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        UI ui = new UI();
        today.setText("Today is: " + myNotebook.getCurrentDate().toString());
        today.setLayoutX(10);
        today.setLayoutY(10);
        displayToday.setText("Display today's tasks");
        displayToday.setLayoutX(500);
        displayToday.setLayoutY(30);
        boolean[] todayButtonFlag = {false};
        displayToday.setOnAction(e -> {
            todayButtonFlag[0] = !todayButtonFlag[0];
            System.out.println("TodayButton: " + todayButtonFlag[0]);
            ui.display(todayButtonFlag[0]);
            if (todayButtonFlag[0]){
                displayToday.setText("Display all tasks");
            } else {
                displayToday.setText("Display today's tasks");
            }
        });
        // Create menu bar
        MenuBar menuBar = new MenuBar();
        
        // Create "Add" menu
        Menu addMenu = new Menu("Add");
        MenuItem addNote = new MenuItem("Note");
        addNote.setOnAction(e -> {
            ui.new EditWindow().getStage(ItemTypes.NOTE).show();
        });
        
        MenuItem addTask = new MenuItem("Task");
        addTask.setOnAction(e -> {
            ui.new EditWindow().getStage(ItemTypes.TASK).show();
        });
        
        addMenu.getItems().addAll(addNote, addTask);
        menuBar.getMenus().add(addMenu);
        
        menuContainer.setPrefHeight(25); // Menu bar height
        
        // Add spacer to push menu to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        menuContainer.getChildren().addAll(spacer, menuBar);
        
        // Position the menu container at the top
        menuContainer.setLayoutY(0);
        menuContainer.setPrefWidth(scene.getWidth());
        
        root.getChildren().addAll(menuContainer, today, displayToday);
        
        primaryStage.setTitle("Planner");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        ui.display(false);
    }
}