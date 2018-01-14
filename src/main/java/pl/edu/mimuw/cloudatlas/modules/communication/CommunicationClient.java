package pl.edu.mimuw.cloudatlas.modules.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;

public class CommunicationClient {

    private boolean packetReceived = false;

    public boolean send(String message, Integer portNumber) {
//        DatagramSocket clientSocket = null;
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("localhost");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            String sentence = message;
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
            try {
                clientSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Object wait = new Object();
            Thread t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                                clientSocket.receive(receivePacket);
                                String modifiedSentence = new String(receivePacket.getData());
                                System.out.println("FROM SERVER:" + modifiedSentence);
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            synchronized(wait) {
                                packetReceived = true;
                                wait.notify();
                            }
                        }
                    }
            );
            t.start();
            try {
                synchronized(wait) {
                    wait.wait(4000);
                }
            } catch (InterruptedException e) { e.printStackTrace(); }
            return packetReceived;

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendPacket(byte[] data,  Integer portNumber) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("localhost");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
            clientSocket.send(sendPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean connectTo(String receiverName) {
        Integer tryCount = 0;
        while (tryCount < 3) {
            if (this.send("Hi", 9876)) {
                return true;
            }
            tryCount++;
        }
        System.out.println("Unable to connect to " + receiverName);
        return false;
    }

    // Sends zmi information to the receiver, default port for sending data is 9786
    public void sendZMI(ZMI zmi, String receiverName) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String response = gson.toJson(zmi);
        Integer chunkSize = 128;

        byte[] sendData = new byte[chunkSize];
        byte[] jsonBytes = response.getBytes();
        Integer totalNumber = jsonBytes.length / chunkSize;
        if (totalNumber == 0) {
            totalNumber = 1;
        }
        Integer packetNumber = 1;
        Integer startIndex = 0;
        String id = UUID.randomUUID().toString();;
        HashMap<String, Object> request = new HashMap();
        byte[] responseBytes = gson.toJson(request).getBytes();
        while (startIndex < jsonBytes.length) {
            request.put("pn", packetNumber);
            request.put("tn", totalNumber);
            request.put("id", id);
            if (packetNumber == totalNumber) {
                chunkSize = jsonBytes.length - jsonBytes.length * (totalNumber - 1);
            }
            request.put("data", Arrays.copyOfRange(responseBytes, startIndex, startIndex + chunkSize));
            request.put("ts", (new Date()).getTime());
            this.sendPacket(gson.toJson(request).getBytes(), 9876);
            startIndex += chunkSize;
            packetNumber++;
        }
    }
}
