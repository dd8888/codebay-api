package codebayapi.demo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Returns the current date used on the body of the user with the European format.
 */
public class TodaysDate {
    public String getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
}
