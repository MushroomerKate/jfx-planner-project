module Planner {
	requires javafx.controls;
	requires java.sql;
	requires java.desktop;
	
	opens planner to javafx.graphics, javafx.fxml;
}
