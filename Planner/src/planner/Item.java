package planner;

public abstract class Item {
    private String name;
    private final Notebook notebook;
    private int id = -1; // -1 indicates not yet persisted
    
    public Item(String name, Notebook notebook){
        this.name = name;
        this.notebook = notebook;
    }
    
    public Notebook getNotebook(){
        return(notebook);
    }
    
    public void setName(String name){
        this.name = name;
        try {
            DBTools.saveItem(this, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}