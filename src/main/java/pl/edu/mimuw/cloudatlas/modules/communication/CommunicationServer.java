package pl.edu.mimuw.cloudatlas.modules.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class CommunicationServer {

    private HashMap<String, List<CommunicationMessage>> receivedPackets = new HashMap<>();
    private ArrayList<Timer> timeoutTimers = new ArrayList<>();

    public void start(Integer portNumber) {
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DatagramSocket serverSocket = null;
                        try {
                            serverSocket = new DatagramSocket(portNumber);
                            while(true)
                            {
                                byte[] receiveData = new byte[1024];
                                byte[] sendData = new byte[1024];

                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                serverSocket.receive(receivePacket);
                                Double receivedTime = Helpers.generateTimestamp();
                                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                                Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
                                Gson gson = new Gson();
                                HashMap<String, Object> map = gson.fromJson(sentence, type);
                                ComMessage message = new ComMessage(map);
                                if (!message.id.equals("0")) {
                                    CommunicationMessage msg = new CommunicationMessage(map);
                                    message = msg;
                                    receiveMessage(msg);
                                }
                                message.timeStamps.add(receivedTime);
//                                System.out.println("RECEIVED: " + sentence);
                                InetAddress IPAddress = receivePacket.getAddress();
                                int port = receivePacket.getPort();
                                message.timeStamps.add(Helpers.generateTimestamp());
                                map.put("ts", message.timeStamps);
                                sendData = gson.toJson(map).getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                try {
                                    serverSocket.send(sendPacket);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        t.start();
    }

    private void receiveMessage(CommunicationMessage message) {
        if (receivedPackets.containsKey(message.id)) {
            List packets = receivedPackets.get(message.id);
            packets.add(message);
            if (packets.size() == message.totalNumber) {
                System.out.println("Packet removed: id = " + message.id);
                assemblePackets(receivedPackets.get(message.id));
                receivedPackets.remove(message.id);
                return;
            }
        }
        else {
            List<CommunicationMessage> messages = new ArrayList<>();
            messages.add(message);
            receivedPackets.put(message.id, messages);
            Timer timer = new Timer();
            // Remove packets in case they never got assembled after 10 seconds
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (receivedPackets.containsKey(message.id)) {
                        receivedPackets.remove(message.id);
                        System.out.println("Packet dropped: id = " + message.id);
                    }
                }
            }, 10000);
        }
    }

    private void assemblePackets(List<CommunicationMessage> packets) {
        packets.sort(Comparator.comparingInt(p -> p.number));
        ArrayList<Double> dataList = new ArrayList<>();
        for (CommunicationMessage l: packets) {
            dataList.addAll(l.data);
        }
        if (dataList.size() > 0) {
            byte[] bytes = Helpers.arrayToBytes(dataList);
            ZMI zmi = Helpers.bytesToZMI(bytes);
            System.out.println("ZMI assambled: name = " + zmi.getAttributes().get("name"));
            Main.updateZMIAttributes(zmi);
        }

    }
}

