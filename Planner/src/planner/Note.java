package planner;

public class Note extends Item{
    private String text;
    
    public Note(String name, Notebook notebook){
        super(name, notebook);
        text = "";
    }
    
    public Note(String name, String text, Notebook notebook){
        super(name, notebook);
        this.text = text;
        // Don't add to notebook here - let the caller decide
    }
    
    public String getText(){
        return text;
    }
    
    public void setText(String text){
        this.text = text;
        try {
            DBTools.saveNote(this, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}