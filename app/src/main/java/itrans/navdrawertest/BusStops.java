package itrans.navdrawertest;

import com.google.android.gms.maps.model.LatLng;

public class BusStops {
    LatLng latLng;
    String title;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getTitleBusStop() {
        return title;
    }

    public void setTitleBusStop(String title) {
        this.title = title;
    }
}