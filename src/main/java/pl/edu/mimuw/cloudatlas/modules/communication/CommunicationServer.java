package pl.edu.mimuw.cloudatlas.modules.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class CommunicationServer {

    private HashMap<String, CommunicationMessage> receivedPackets = new HashMap<>();

    public void start(Integer portNumber) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(portNumber);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sentence = new String(receivePacket.getData());
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            Gson gson = new Gson();
            HashMap<String, Object> map = gson.fromJson(sentence, type);
            CommunicationMessage message = new CommunicationMessage(map);
            receivedPackets.put(message.id, message);
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                serverSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
