package planner;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class DBTools {    
    
    private static class DBSettings {
        String driver;
        String url;
        String user;
        String password;
        
        DBSettings(String driver, String url, String user, String password) {
            this.driver = driver;
            this.url = url;
            this.user = user;
            this.password = password;
        }
    }
    
    private static DBSettings loadSettingsFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        if (lines.size() < 4) {
            throw new IOException("Settings file must contain at least 4 lines: driver, url, user, password");
        }
        
        String driver = lines.get(0).trim();
        String url = lines.get(1).trim();
        String user = lines.get(2).trim();
        String password = lines.get(3).trim();
        
        return new DBSettings(driver, url, user, password);
    }
    
    private static Connection getConnection() throws SQLException, IOException, ClassNotFoundException {
        DBSettings settings = loadSettingsFromFile("src/db_settings.txt");
        Class.forName(settings.driver);
        return DriverManager.getConnection(settings.url, settings.user, settings.password);
    }
    
    public static void initializeDB() {
        initializeDB("db_settings.txt");
    }
    
    public static void initializeDB(String settingsFilePath) {
        try {
            System.out.println("ESTABLISHING DATABASE CONNECTION");
            Connection connection = getConnection();
            
            // Initialize Item_types table with required values
            initializeItemTypes(connection);
            
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, "planner", "%", new String[]{"TABLE"});
            
            java.util.ArrayList<String> tableList = new java.util.ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            tables.close();
            System.out.println("DATABASE CONNECTION ESTABLISHED");
            
            connection.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static void initializeItemTypes(Connection conn) throws SQLException {
        // First check if Item_types table exists and has data
        try {
            String checkSql = "SELECT COUNT(*) as count FROM Item_types";
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Item_types already initialized");
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();
        } catch (SQLException e) {
            // Table might not exist yet, that's fine
            System.out.println("Item_types table not found, will create if needed");
        }
        
        // Insert the required item types
        String insertSql = "INSERT IGNORE INTO Item_types (type_id, type_description) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, "NOTE");
            pstmt.setString(2, "A note for storing text information");
            pstmt.executeUpdate();
            
            pstmt.setString(1, "TASK");
            pstmt.setString(2, "A task with optional deadline and subtasks");
            pstmt.executeUpdate();
            
            System.out.println("Item_types initialized with NOTE and TASK");
        }
    }
    
    // Notebook operations
    public static void saveNotebook(Notebook notebook) throws Exception {
        String sql = "INSERT INTO Notebooks (id, `current_date`) VALUES (1, ?) " +
                     "ON DUPLICATE KEY UPDATE `current_date` = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(notebook.getCurrentDate()));
            pstmt.setDate(2, Date.valueOf(notebook.getCurrentDate()));
            pstmt.executeUpdate();
        }
    }
    
    public static void ensureNotebookExists() throws Exception {
        String sql = "INSERT IGNORE INTO Notebooks (id, `current_date`) VALUES (1, CURDATE())";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    public static Notebook loadNotebook() throws Exception {
        // First ensure notebook exists
        ensureNotebookExists();
        
        Notebook notebook = new Notebook();
        String sql = "SELECT `current_date` FROM Notebooks WHERE id = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                notebook.setCurrentDate(rs.getDate("current_date").toLocalDate());
            }
        }
        return notebook;
    }
    
    // Item operations
    public static int saveItem(Item item, int notebookId) throws Exception {
        // Ensure notebook exists before saving item
        ensureNotebookExists();
        
        String sql = "INSERT INTO Items (id, notebook_id, name, item_type) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = ?, item_type = ?";
        
        int itemId = item.getId();
        if (itemId == -1) {
            // Get next available ID
            itemId = getNextItemId();
            item.setId(itemId);
        }
        
        String itemType = item instanceof Note ? "NOTE" : "TASK";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, notebookId);
            pstmt.setString(3, item.getName());
            pstmt.setString(4, itemType);
            pstmt.setString(5, item.getName());
            pstmt.setString(6, itemType);
            pstmt.executeUpdate();
        }
        return itemId;
    }
    
    public static void deleteItem(int itemId) throws Exception {
        String sql = "DELETE FROM Items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.executeUpdate();
        }
    }
    
    // Note operations
    public static void saveNote(Note note, int notebookId) throws Exception {
        int itemId = saveItem(note, notebookId);
        String sql = "INSERT INTO Notes (item_id, text) VALUES (?, ?) ON DUPLICATE KEY UPDATE text = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setString(2, note.getText());
            pstmt.setString(3, note.getText());
            pstmt.executeUpdate();
        }
    }
    
    public static Note loadNote(int itemId, Notebook notebook) throws Exception {
        String sql = "SELECT i.name, n.text FROM Items i JOIN Notes n ON i.id = n.item_id WHERE i.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Note note = new Note(rs.getString("name"), rs.getString("text"), notebook);
                    note.setId(itemId);
                    return note;
                }
            }
        }
        return null;
    }
    
    // Task operations
    public static void saveTask(Task task, int notebookId) throws Exception {
        int itemId = saveItem(task, notebookId);
        String sql = "INSERT INTO Tasks (item_id, parent_task_id, deadline, is_complete) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE parent_task_id = ?, deadline = ?, is_complete = ?";
        
        Integer parentId = task.getParent() != null ? task.getParent().getId() : null;
        Date deadline = task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null;
        int isComplete = task.getIsComplete() ? 1 : 0;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            if (parentId != null && parentId != -1) {
                pstmt.setInt(2, parentId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setDate(3, deadline);
            pstmt.setInt(4, isComplete);
            
            if (parentId != null && parentId != -1) {
                pstmt.setInt(5, parentId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setDate(6, deadline);
            pstmt.setInt(7, isComplete);
            pstmt.executeUpdate();
        }
        
        // Save subtasks
        for (Task subtask : task.getSubtasks()) {
            saveTask(subtask, notebookId);
        }
    }
    
    public static Task loadTask(int itemId, Notebook notebook, Map<Integer, Task> taskMap) throws Exception {
        String sql = "SELECT i.name, t.parent_task_id, t.deadline, t.is_complete " +
                     "FROM Items i JOIN Tasks t ON i.id = t.item_id WHERE i.id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    Date deadlineDate = rs.getDate("deadline");
                    LocalDate deadline = deadlineDate != null ? deadlineDate.toLocalDate() : null;
                    boolean isComplete = rs.getInt("is_complete") == 1;
                    int parentId = rs.getInt("parent_task_id");
                    
                    Task parent = null;
                    if (!rs.wasNull() && parentId != -1 && taskMap.containsKey(parentId)) {
                        parent = taskMap.get(parentId);
                    }
                    
                    Task task = new Task(name, notebook, parent);
                    task.setId(itemId);
                    if (deadline != null) {
                        try {
                            task.setDeadline(deadline);
                        } catch (Exception e) {
                            // Handle deadline validation for loaded tasks
                            System.err.println("Warning: Could not set deadline for task " + name + ": " + e.getMessage());
                        }
                    }
                    task.setIsComplete(isComplete);
                    
                    if (parent != null && !parent.getSubtasks().contains(task)) {
                        parent.addSubtask(task);
                    }
                    
                    return task;
                }
            }
        }
        return null;
    }
    
    public static void loadAllItems(Notebook notebook) throws Exception {
        // First check if tables exist and have data
        String checkSql = "SELECT COUNT(*) as count FROM Items WHERE notebook_id = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("No items found in database");
                return;
            }
        }
        
        Map<Integer, Task> taskMap = new HashMap<>();
        
        try (Connection conn = getConnection()) {
            // Load tasks first
            String tasksSql = "SELECT item_id FROM Tasks";
            try (Statement tasksStmt = conn.createStatement();
                 ResultSet tasksRs = tasksStmt.executeQuery(tasksSql)) {
                while (tasksRs.next()) {
                    int taskId = tasksRs.getInt("item_id");
                    Task task = loadTask(taskId, notebook, taskMap);
                    if (task != null) {
                        taskMap.put(taskId, task);
                        if (task.getParent() == null) {
                            // Use addItemDirect to avoid double-saving
                            notebook.addItemDirect(task);
                        }
                    }
                }
            }
            
            // Load notes
            String notesSql = "SELECT item_id FROM Notes";
            try (Statement notesStmt = conn.createStatement();
                 ResultSet notesRs = notesStmt.executeQuery(notesSql)) {
                while (notesRs.next()) {
                    int noteId = notesRs.getInt("item_id");
                    Note note = loadNote(noteId, notebook);
                    if (note != null) {
                        // Use addItemDirect to avoid double-saving
                        notebook.addItemDirect(note);
                    }
                }
            }
        }
    }
    
    private static int getNextItemId() throws Exception {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 as next_id FROM Items";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }
    
    public static void updateTaskCompletion(int itemId, boolean isComplete) throws Exception {
        String sql = "UPDATE Tasks SET is_complete = ? WHERE item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isComplete ? 1 : 0);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        }
    }
}