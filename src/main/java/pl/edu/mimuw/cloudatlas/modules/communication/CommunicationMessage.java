package pl.edu.mimuw.cloudatlas.modules.communication;

import java.util.HashMap;

public class CommunicationMessage {
    public Long timeStamp;
    public String id;
    public Integer number;
    public Integer totalNumber;

    public CommunicationMessage(HashMap<String, Object> json) {
        this.timeStamp = (Long) json.get("ts");
        this.id = (String) json.get("id");
        this.number = (Integer) json.get("pn");
        this.totalNumber = (Integer) json.get("tn");
    }
}
