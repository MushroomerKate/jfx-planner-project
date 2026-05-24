package planner;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;

public class Task extends Item{
    private Task parent;
    private ArrayList<Task> subtasks;
    private LocalDate deadline;
    private boolean isComplete;
    
    public Task(String name, Notebook notebook, Task parent){
        super(name, notebook);
        this.subtasks = new ArrayList<>();
        this.isComplete = false;
        this.deadline = null;
        this.parent = parent;
        // Don't add to notebook here - let the caller decide
    }
    
    public Task(String name, LocalDate deadline, Notebook notebook, Task parent)
      throws DateTimeException {
        super(name, notebook);
        this.subtasks = new ArrayList<>();
        this.isComplete = false;
        if (deadline.isBefore(LocalDate.now()))
            throw new DateTimeException("Can't make a task in the past");
        if (parent != null){
            if (deadline.isAfter(parent.getDeadline())){
                throw new DateTimeException("A subtask's deadline can't be "
                        + "after the main task's");
            }
        }
        this.deadline = deadline;
        this.parent = parent;
        // Don't add to notebook here - let the caller decide
    }
    
    public Task addSubtask(String name) throws DateTimeException {
        return addSubtask(name, null);
    }
    
    public Task addSubtask(String name, LocalDate deadline) throws DateTimeException {
        // Validate deadline first before creating subtask
        if (deadline != null) {
            if (deadline.isBefore(LocalDate.now()))
                throw new DateTimeException("Can't make a task in the past");
            if (this.deadline != null && deadline.isAfter(this.deadline)) {
                throw new DateTimeException("A subtask's deadline can't be after the main task's deadline");
            }
        }
        
        // Create subtask with validation
        Task subtask;
        if (deadline != null) {
            subtask = new Task(name, deadline, this.getNotebook(), this);
        } else {
            subtask = new Task(name, this.getNotebook(), this);
        }
        
        // Only add to collections and save if validation passed
        subtasks.add(subtask);
        if (isComplete)
            setIsComplete(false);
        
        // Save to database
        try {
            DBTools.saveTask(subtask, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return subtask;
    }
    
    public void addSubtask(Task subtask) {
        subtasks.add(subtask);
    }
    
    public LocalDate getDeadline(){
        return(deadline);
    }
    
    public void setDeadline(LocalDate deadline) throws DateTimeException {
        if (deadline.isBefore(LocalDate.now()))
            throw new DateTimeException("Can't make a task in the past");
        if (parent != null){
            if (deadline.isAfter(parent.getDeadline())){
                throw new DateTimeException("A subtask's deadline can't be "
                        + "after the main task's");
            }
        }
        this.deadline = deadline;
        try {
            DBTools.saveTask(this, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean getIsComplete(){
        return isComplete;
    }
    
    public ArrayList<Task> getSubtasks(){
        return subtasks;
    }
    
    public Task getParent(){
        return parent;
    }
    
    public void setIsComplete(boolean isComplete){
        this.isComplete = isComplete;
        try {
            DBTools.updateTaskCompletion(this.getId(), isComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (isComplete){
            if (!this.subtasks.isEmpty()){
                for (Task subtask: this.getSubtasks()){
                    subtask.setIsComplete(true);
                }
            }
            System.out.println(this.getParent());
            if ((this.getParent() != null) && !this.getParent().getIsComplete()){
                boolean allSubtasksComplete = true;
                for (Task task: this.getParent().getSubtasks()){
                    if (!task.getIsComplete()){
                        allSubtasksComplete = false;
                    }
                }
                if (allSubtasksComplete){
                    this.getParent().setIsComplete(true);
                }
            }
        } else {
            if(this.getParent() != null){            
                this.getParent().setIsComplete(false);
            }
        }
    }
}