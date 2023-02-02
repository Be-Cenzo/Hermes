package be.cenzo.hermes.ui.rooms;

import com.mapbox.geojson.Feature;

public class Rooms {
    private String type;
    private Feature[] features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }
}
