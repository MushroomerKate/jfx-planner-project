package planner;

import java.time.DateTimeException;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import static planner.Planner.menuContainer;
import static planner.Planner.myNotebook;
import static planner.Planner.root;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import static planner.Planner.displayToday;
import static planner.Planner.today;

public final class UI {

    public class EditWindow {
        private Stage stage;        
        private Scene scene;
        private VBox root;
        private static final int WIDTH = 300;
        private static final String ENTER_DEADLINE = 
                "Enter Deadline YYYY-MM-DD/DD.MM.YYYY";
            
        public Stage getStage(ItemTypes option){    //���� ���������� ������
            stage = new Stage();
            root = new VBox();
            TextField name = new TextField();  
            int sizeX = 0, sizeY = 0;
            Button okButton = new Button();
            okButton.setText("Add " + option);
            if (option == ItemTypes.NOTE){      // Notes
                TextField text = new TextField();        
                name.setText("Enter Note Title");
                text.setText("Enter Text");
                
                sizeX = WIDTH;
                sizeY = 100;    
                okButton.setOnAction((ActionEvent event) -> {
                    Note note = new Note(name.getText(), text.getText(), myNotebook);
                    myNotebook.addItem(note);  // This adds to notebook AND saves to DB
                    display(false);
                    stage.close();
                });            
                root.getChildren().addAll(name, text); 
            }
            if (option == ItemTypes.TASK){      // Tasks   
                TextField deadline = new TextField();      
                name.setText("Enter Task Name");
                deadline.setText(ENTER_DEADLINE);

                sizeX = WIDTH;
                sizeY = 100;    
                okButton.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        Task newTask;
                        if (deadline.getText().equals(ENTER_DEADLINE))
                            newTask = new Task(name.getText(), myNotebook, null);
                        else                            
                            try{
                                newTask = new Task(name.getText(), 
                                        Tools.toLocalDate(deadline.getText()), 
                                        myNotebook, null);
                            } catch (DateTimeParseException e) {
                                JOptionPane.showMessageDialog(null, 
                                    "Sorry, wrong date format! :( \n" +
                                    "Please try again using format:\n" +
                                            ENTER_DEADLINE,
                                    "Format Error", 
                                    JOptionPane.ERROR_MESSAGE);
                                return;
                            } catch (DateTimeException e){
                                JOptionPane.showMessageDialog(null, 
                                    "Sorry, can't set the deadline to this "
                                            + "date! :( \n" + e.getMessage(),
                                    "Date Error", 
                                    JOptionPane.ERROR_MESSAGE);  
                                return;
                            }
                        myNotebook.addItem(newTask);  // This adds to notebook AND saves to DB
                        display(false);
                        stage.close();
                    }
                });  
                root.getChildren().addAll(name, deadline);
            }
            root.getChildren().add(okButton);

            scene = new Scene(root, sizeX, sizeY);
            stage.setTitle(option + " addition");
            stage.setScene(scene);   
            return stage;          
        }  
        
        public Stage getStage(final Note item, final ItemUI iUI){  
            stage = new Stage();
            root = new VBox();
            TextField name = new TextField();  
            int sizeX = 0, sizeY = 0;
            Button okButton = new Button("Done");
            name.setText(item.getName());
            if (item instanceof Note){
                TextField text = new TextField(); 
                text.setText(((Note)item).getText());
                
                sizeX = 300;
                sizeY = 100;    
                okButton.setOnAction((ActionEvent event) -> {
                    ((Note)item).setName(name.getText());
                    ((Note)item).setText(text.getText());
                    iUI.setLabel(item.getName());
                    stage.close();
                }); 
                root.getChildren().addAll(name, text);  
            }
            
            root.getChildren().add(okButton);

            scene = new Scene(root, sizeX, sizeY);
            stage.setTitle("Edit");
            stage.setScene(scene);   
            return stage;          
        }    
         
        public Stage getStage(final Task task, final TaskUI taskUI){  
            stage = new Stage();
            root = new VBox();
            TextField name = new TextField();  
            int sizeX = 0, sizeY = 0;
            Button okButton = new Button("Done");
            name.setText(task.getName()); 
            TextField deadline = new TextField();
            if (task.getDeadline() != null)
                deadline.setText(task.getDeadline().toString());
            else
                deadline.setText(ENTER_DEADLINE);
            Button subtask = new Button("Add subtask");

            sizeX = 300;
            sizeY = 100;   
            okButton.setOnAction((ActionEvent event) -> {
                (task).setName(name.getText());
                if (!deadline.getText().equals(ENTER_DEADLINE))
                    try{
                        (task).setDeadline(Tools.toLocalDate(deadline.getText()));
                    } catch (DateTimeParseException e) {
                        JOptionPane.showMessageDialog(null, 
                            "Sorry, wrong date format! :( \n" +
                            "Please try again using format:\n" +
                                    ENTER_DEADLINE,
                            "Format Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return; // Don't close window on error
                    } catch (DateTimeException e){
                        JOptionPane.showMessageDialog(null, 
                            "Sorry, can't set the deadline to this "
                                    + "date! :( \n" + e.getMessage(),
                            "Date Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return; // Don't close window on error
                    }            
                System.out.println("deadline set to: " + task.getDeadline());
                taskUI.setLabel(task.getName());
                display(false);
                stage.close();
            });    
            subtask.setOnAction((ActionEvent event) -> {
                new EditWindow().getStage(task).show();
                stage.close();
            });  
            root.getChildren().addAll(name, deadline, subtask);
            root.getChildren().add(okButton);

            scene = new Scene(root, sizeX, sizeY);
            stage.setTitle("Edit");
            stage.setScene(scene);   
            return stage;          
        }
        
        public Stage getStage(final Task task){
            stage = new Stage();
            root = new VBox();
            stage.setTitle("Subtask");
            TextField name = new TextField();  
            int sizeX = 0, sizeY = 0;
            Button okButton = new Button("Done");           
            TextField deadline = new TextField();      
            name.setText("Enter Task Name");
            deadline.setText(ENTER_DEADLINE);

            sizeX = WIDTH;
            sizeY = 100;    
            okButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Task subtask = null;
                        if (deadline.getText().equals(ENTER_DEADLINE)) {
                            subtask = task.addSubtask(name.getText());
                        } else {
                            try {
                                LocalDate deadlineDate = Tools.toLocalDate(deadline.getText());
                                subtask = task.addSubtask(name.getText(), deadlineDate);
                            } catch (DateTimeParseException e) {
                                JOptionPane.showMessageDialog(null, 
                                    "Sorry, wrong date format! :( \n" +
                                    "Please try again using format:\n" +
                                            ENTER_DEADLINE,
                                    "Format Error", 
                                    JOptionPane.ERROR_MESSAGE);
                                return; // Don't close the window, let user try again
                            }
                        }
                        
                        // Only proceed if subtask was created successfully
                        if (subtask != null) {
                            display(false);
                            stage.close();
                        }
                        
                    } catch (DateTimeException e) {
                        // Show error message and keep the window open
                        JOptionPane.showMessageDialog(null, 
                            "Sorry, can't create subtask! :( \n" + e.getMessage(),
                            "Subtask Creation Error", 
                            JOptionPane.ERROR_MESSAGE);
                        // Don't close the stage - let user try again with different values
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, 
                            "An unexpected error occurred: \n" + e.getMessage(),
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            });  
            root.getChildren().addAll(name, deadline, okButton);

            scene = new Scene(root, sizeX, sizeY);
            stage.setScene(scene);   
            return stage;            
        }
    }
    
    public void coreComponents(){
        root.getChildren().addAll(menuContainer, today, displayToday);        
    }
    
    public void display(boolean today) {
        final int X_OFFSET = 10;
        Planner.root.getChildren().clear();
        coreComponents();
        ItemUI itemUI = null;
        int yOffset = 20;
        ArrayList<Item> items = new ArrayList<>();
        if (!today)
            items = Planner.myNotebook.getItems();
        else{
            items = Planner.myNotebook.getTodayItems();
            System.out.println("getTodayItems called");
        }
        for (final Item item: items){
            if (item instanceof Note){
                itemUI = new NoteUI(item.getName()); 
                itemUI.all.setLayoutX(X_OFFSET);
                itemUI.all.setLayoutY(yOffset);
                yOffset += 25;
                final NoteUI noteUI = (NoteUI)itemUI;
                final Note note = (Note)item;
                itemUI.getUI().setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                            new EditWindow().getStage(note, noteUI).show();
                    }
                });
            Planner.root.getChildren().add(((NoteUI)itemUI).getUI());
            }
            if (item instanceof Task){
                yOffset = displayTask((Task)item, X_OFFSET, yOffset);
            }
        }
    }
    
    private int displayTask(Task task, int xOffset, int yOffset){
        TaskUI taskUI = new TaskUI(task);
        taskUI.all.setLayoutX(xOffset);
        taskUI.all.setLayoutY(yOffset);
        xOffset += 25;
        yOffset += 25;
        taskUI.getUI().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                    new EditWindow().getStage(task, taskUI).show();
            }
        });
        Planner.root.getChildren().add(taskUI.getUI());
        if (!task.getSubtasks().isEmpty()){
            for (Task subtask: task.getSubtasks()){
                yOffset = displayTask(subtask, xOffset, yOffset);
            }
        }
        return yOffset;
    }
    
    private static abstract class ItemUI{
        private Text label;     
        protected HBox all;
        
        public ItemUI(String label){
            all = new HBox();
            all.setSpacing(10);
            this.label = new Text(label);
            all.getChildren().addAll(this.label);
        }
        
        public HBox getUI(){
            all.getChildren().clear();
            all.getChildren().addAll(this.label);
            return all;
        }
        
        public void setLabel(String label){
            this.label.setText(label);
        }
        
        public Text getLabel(){
            return label;
        }
    }
    
    private static class NoteUI extends ItemUI{ 
        private Note note;       
        
        public NoteUI(String label){
            super(label);
        }
    }
    
    private  class TaskUI extends ItemUI{
        private Task task;
        private Text deadline;
        private CheckBox isComplete;
        
        public TaskUI(Task task){
            super(task.getName());
            this.task = task;
            this.deadline = new Text();
            if (task.getDeadline() != null)
                this.deadline.setText((task.getDeadline()).toString());
            this.isComplete = new CheckBox();
            this.isComplete.setSelected(task.getIsComplete());
            all.getChildren().addAll(this.deadline, this.isComplete); 
            this.isComplete.setOnAction((ActionEvent event) -> {
                this.task.setIsComplete(isComplete.isSelected());
                System.out.println("Ticked box to: " + isComplete.isSelected());
                display(false);
            });
        }
        
        private void refresh(){
            if (task.getDeadline() != null)
                this.deadline.setText((task.getDeadline()).toString());
            this.isComplete.setSelected(task.getIsComplete());  
        }
        
        public void setDeadline(String deadline){
            this.deadline.setText(deadline);
        }
        
        public HBox getUI(){
            super.getUI();
            refresh();
            all.getChildren().addAll(deadline, isComplete);
            return all;
        }
        
        public Text getDeadline(){
            return deadline;
        }
        
        public CheckBox getIsComplete(){
            return isComplete;
        }
    }
    
}