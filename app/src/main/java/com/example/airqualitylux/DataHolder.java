package com.example.airqualitylux;

import org.json.JSONObject;
import java.util.List;

public class DataHolder {
    private static DataHolder instance = null;
    private List<JSONObject> sensorDataList;

    private DataHolder() {}

    public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setSensorDataList(List<JSONObject> sensorDataList) {
        this.sensorDataList = sensorDataList;
    }

    public List<JSONObject> getSensorDataList() {
        return sensorDataList;
    }
}
