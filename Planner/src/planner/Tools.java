package planner;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Tools {
    public static LocalDate toLocalDate(String date) throws DateTimeParseException {
        date = date.trim(); //убрать пробелы в начале и в конце        
        if (date.matches("^20\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")){
        } else if (date.matches("^(0[1-9]|[12]\\d|3[01])\\.(0[1-9]|1[0-2])\\.(19|20)\\d{2}")){
            date = date.replaceAll("(\\d{2})\\.(\\d{2})\\.(\\d{4})", "$3-$2-$1");            
        }
        return LocalDate.parse(date);   
    }
}
