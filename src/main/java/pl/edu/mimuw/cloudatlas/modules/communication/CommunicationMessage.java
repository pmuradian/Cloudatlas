package pl.edu.mimuw.cloudatlas.modules.communication;

import pl.edu.mimuw.cloudatlas.helpers.Helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CommunicationMessage extends ComMessage {
    public Integer number;
    public Integer totalNumber;

    public CommunicationMessage(HashMap<String, Object> json) {
        this.timeStamps = (ArrayList<Double>) json.get("ts");
        this.id = (String) json.get("id");
        this.data = (ArrayList<Double>) json.get("data");
        this.number = ((Double) json.get("pn")).intValue();
        this.totalNumber = ((Double) json.get("tn")).intValue();
    }

    public byte[] getDataBytes() {
        return Helpers.arrayToBytes(this.data);
    }
}
