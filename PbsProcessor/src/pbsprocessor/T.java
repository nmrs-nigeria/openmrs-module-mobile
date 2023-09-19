package pbsprocessor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class T {
    public  static  void  main(String [] a){
        String  lastCapturedDate="2023-08-22T00:00:00";

        LocalDateTime recentCaptureDate = LocalDateTime.parse(lastCapturedDate);
        Long  recentCaptureDate2  =Date.parse( lastCapturedDate);

    }
}
