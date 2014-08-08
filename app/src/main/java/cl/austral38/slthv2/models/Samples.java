package cl.austral38.slthv2.models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eDelgado on 12-05-14.
 */
public class Samples {

    public double temperature;
    public double light;
    public double humidity;
    public String time;

    /**Constructor
     * */
    public  Samples(float temperature,float light, float humidity){
        this.humidity = humidity;
        this.light = light;
        this.temperature = temperature;
        time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }
    /**
    * Constructor*/
    public Samples(){
        time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
