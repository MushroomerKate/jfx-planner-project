package planner;

import java.util.ArrayList;
import java.time.LocalDate;

public class Notebook {
    private ArrayList<Item> allItems;
    private LocalDate currentDate;
    
    public Notebook(){
        allItems = new ArrayList<>();
        currentDate = LocalDate.now();
    }
    
    public void setCurrentDate(LocalDate date){
        this.currentDate = date;
        try {
            DBTools.saveNotebook(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public LocalDate getCurrentDate(){
        return currentDate;
    }
    
    public Item addItem(Item item){    
        allItems.add(item);
        try {
            if (item instanceof Note) {
                DBTools.saveNote((Note) item, 1);
            } else if (item instanceof Task) {
                DBTools.saveTask((Task) item, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }
    
    // Method to add item without saving to DB (used during loading)
    public Item addItemDirect(Item item){    
        if (!allItems.contains(item)) {
            allItems.add(item);
        }
        return item;
    }
      
    public ArrayList<Item> getItems(){
        return(allItems);
    }    
    
    public ArrayList<Item> getTodayItems(){
        System.out.println("getTodayItems activated");
        ArrayList<Item> items = new ArrayList<Item>();
        for (Item item: allItems){
            if (item instanceof Task){
                if (!(((Task)item).getDeadline() == null)
                    &&(((Task)item).getDeadline().equals(currentDate))){
                    items.add((Task)item);
                    System.out.println(item.getName() + " is today");                    
                }
            } else {
                items.add(item);
                System.out.println("Added " + item.getName());
            }
        }
        return(items);
    }
    
    public void loadFromDatabase() throws Exception {
        allItems.clear();
        DBTools.loadAllItems(this);
    }
}