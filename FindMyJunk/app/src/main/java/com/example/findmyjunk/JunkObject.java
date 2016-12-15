package com.example.findmyjunk;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by brake.matthew on 12/1/2016.
 */

public class JunkObject implements Serializable{

    private ArrayList<Double> polyArrayLat;
    private ArrayList<Double> polyArrayLon;
    private double markerLat;
    private double markerLon;
    private String markerString;
    private String photoPath;

    public JunkObject(){

    }

    public double getMarkerLat(){
        return markerLat;
    }

    public void setMarkerLat(double lat){
        markerLat = lat;
    }

    public double getMarkerLon(){
        return markerLon;
    }

    public void setMarkerLon(double lon){
        markerLon = lon;
    }

    public void setMarkerString(String str){
        markerString = str;
    }

    public String getMarkerString(){
        return markerString;
    }

    public void setPolyArrayLat(ArrayList polyLat){
        polyArrayLat = polyLat;
    }

    public ArrayList getPolyArrayLat(){
        return polyArrayLat;
    }

    public void setPolyArrayLon(ArrayList polyLon){
        polyArrayLon = polyLon;
    }

    public ArrayList getPolyArrayLon() {
        return polyArrayLon;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
