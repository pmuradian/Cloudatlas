package pl.edu.mimuw.cloudatlas.modules.communication;

import java.util.ArrayList;
import java.util.HashMap;

public class ComMessage {
    public ArrayList<Double> timeStamps;
    public String id;
    public ArrayList<Double> data;

    protected ComMessage() {

    }

    public ComMessage(HashMap<String, Object> json) {
        this.timeStamps = (ArrayList<Double>) json.get("ts");
        this.id = (String) json.get("id");
        this.data = (ArrayList<Double>) json.get("data");
    }

    public Long calculateRoundTrip() {
        if (timeStamps.size() < 4) {
            return (timeStamps.get(3).longValue() - timeStamps.get(0).longValue())- (timeStamps.get(2).longValue() - timeStamps.get(1).longValue());
        }
        return 0l;
    }
}
