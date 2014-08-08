package cl.austral38.slthv2.models;

/**
 * Created by eDelgado on 13-05-14.
 */

public class Geo {

    public Geo (){
    }

    public Geo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double latitude = 0;
    public double longitude = 0;


}
