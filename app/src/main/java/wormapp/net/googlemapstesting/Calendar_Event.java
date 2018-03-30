package wormapp.net.googlemapstesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ASUS on 09/08/2015.
 */
public class Calendar_Event {
    int Duree;
    int FK_IdElectrovanne;
    int IdCalendrier;
    String Datedebut;

    Calendar_Event(JSONObject jsonObject){
        try {
            this.Duree = jsonObject.getInt("Duree");
            this.FK_IdElectrovanne = jsonObject.getInt("FK_IdElectrovanne");
            this.Datedebut = jsonObject.getString("Datedebut");
            this.IdCalendrier = jsonObject.getInt("IdCalendrier");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String toString()
    {
        return ""+IdCalendrier;
    }

    public String getReadableDateString()
    {
        String result=null;
        result = (((Datedebut.replaceAll("[\\-\\+\\/\\.\\^:,]", "")).replaceAll("Date", "")).replaceAll("\\p{P}", "")).replaceAll("0700", "");
        Date date = new Date(Long.parseLong(result));
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
        return format.format(date).toString();
    }

    public Date getDate()
    {
        String result=null;
        result = (((Datedebut.replaceAll("[\\-\\+\\/\\.\\^:,]", "")).replaceAll("Date", "")).replaceAll("\\p{P}", "")).replaceAll("0700", "");
        Date date = new Date(Long.parseLong(result));
        return date;
    }

    public int[] debut_date()
    {

        String h = this.getReadableDateString();
        int[] date= new int[5];
        String[] s =h.split("\\.");
        date[0]=Integer.parseInt(s[0]);
        date[1]=Integer.parseInt(s[1].replaceAll("0",""));
        date[2]=Integer.parseInt(s[2]);
        String[] s1 = s[3].split("\\:");
        if(Datedebut.charAt(Datedebut.length()-3) == '7') {date[3] = (Integer.parseInt((s1[0]))-8+24)%24;}
        else {date[3] = (Integer.parseInt((s1[0]))-9+24)%24;}
        date[4]=Integer.parseInt((s1[1]));

        return date;
    }

    public String Working_date()
    {
        int a = this.debut_date()[3];
        int b = this.debut_date()[4]+this.Duree;
        while (b > 60) {
            a++;
            b = b-60;
        }

        if (b == 60){
            String c = "00";
            a++;
            return "Electrovanne en marche de " + this.debut_date()[3] + ":" + this.debut_date()[4] + " à  " + a + ":" + c;
        }
        else {
            return "Electrovanne en marche de " + this.debut_date()[3] + ":" + this.debut_date()[4] + " à  " + a + ":" + b;
        }

    }

    public boolean isTheDay(int year, int month, int day)
    {
        if ((debut_date()[0] == year) && (debut_date()[1] == month) && (debut_date()[2] == day)){
            return true;
        }
        else {
            return false;
        }
    }
}
