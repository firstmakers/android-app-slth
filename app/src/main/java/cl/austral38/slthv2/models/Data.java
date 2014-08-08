package cl.austral38.slthv2.models;

import java.util.ArrayList;

/**
 * Created by eDelgado on 14-05-14.
 */
public class Data {

    public ArrayList<Samples> data ;
    public String title;
    public Geo location;
    public ArrayList<String> sensors;

    public Data(){
        data = new ArrayList<Samples>();
        sensors = new ArrayList<String>();
        title = "";
    }

    public Data(ArrayList<Samples> data, ArrayList<String> sensors, Geo location, String title) {
        this.data = data;
        this.sensors = sensors;
        this.location = location;
        this.title = title;
    }


    public ArrayList<Samples> getData() {
        return data;
    }

    public void setData(ArrayList<Samples> data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Geo getLocation() {
        return location;
    }

    public void setLocation(Geo location) {
        this.location = location;
    }

    public ArrayList<String> getSensors() {
        return sensors;
    }

    public void setSensors(ArrayList<String> sensors) {
        this.sensors = sensors;
    }




    public int dataSize(){
        return data.size();
    }

    public int sensorSize(){
        return sensors.size();
    }
}
