/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.utils;

/**
 *
 * @author brian
 */
public class Utilities {
    public String getUptime(java.util.Date start, java.util.Date end)
    {
         java.util.Date now = new java.util.Date();
        long diff = (end.getTime() - start.getTime());
        long seconds = (diff / 1000);

        int days = (int) (seconds / 86400);	// antal hele dage
        int days_r = (int) (seconds % 86400);	// sekunder til rest

        int hours = days_r / 3600;			// heltals division	giver timer 
        int r_secs = days_r % 3600;			// sekunder til rest

        int mins = r_secs / 60;				// heltals division giver minutter af sekunder til rest
        int secs = r_secs % 60;				// sekunder til rest 	

        String mydays = days + "";
        String myhours = hours + "";
        String mymins = mins + "";
        String mysecs = secs + "";

        /*if(secs < 10)
    		mysecs = "0" + secs;	    	
    	if(mins < 10)	
    		mymins = "0" + mins;	    		
         */
        return "Uptime: " + mydays + " days " + myhours + " hrs " + mymins + " mins " + mysecs + " secs ";
    }      
}
