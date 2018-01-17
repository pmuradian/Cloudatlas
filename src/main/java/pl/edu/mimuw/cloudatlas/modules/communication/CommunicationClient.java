package pl.edu.mimuw.cloudatlas.modules.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;

public class CommunicationClient {

    private boolean packetReceived = false;
    private Long roundTripDelay = 0l;
    private Long tolerableDelay = 10l;
    DatagramSocket clientSocket;
    InetAddress IPAddress;


    public boolean send(String message, String receiverName,  Integer portNumber) {
//        DatagramSocket clientSocket = null;
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName(receiverName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            String sentence = message;
            Gson gson = new Gson();
            HashMap<String, Object> request = new HashMap<>();
            request.put("id", "0");

            request.put("data", Helpers.byteArrayToList(sentence.getBytes()));

            ArrayList<Double> timeStamps = new ArrayList<>();
            timeStamps.add(Helpers.generateTimestamp());
            request.put("ts", timeStamps);

            sendData = gson.toJson(request).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
            try {
                socket.send(sendPacket);
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
                                socket.receive(receivePacket);

                                Double receivedTime = Helpers.generateTimestamp();
                                Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
                                Gson gson = new Gson();
                                String response = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                                HashMap<String, Object> map = gson.fromJson(response, type);
                                ComMessage message = new ComMessage(map);
                                message.timeStamps.add(receivedTime);
                                System.out.println("FROM SERVER:" + response);
                                roundTripDelay = message.calculateRoundTrip();
                                synchronized(wait) {
                                    packetReceived = true;
                                    socket.close();
                                    wait.notify();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
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
            if (!socket.isClosed()) {
                socket.close();
            }
            return packetReceived;

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendPacket(byte[] data,  Integer portNumber) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
            clientSocket.send(sendPacket);
            Thread t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                                clientSocket.receive(receivePacket);
//                                Double receivedTime = Helpers.generateTimestamp();
//                                Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
//                                Gson gson = new Gson();
//                                String response = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
//                                HashMap<String, Object> map = gson.fromJson(response, type);
//                                ComMessage message = new ComMessage(map);
//                                message.timeStamps.add(receivedTime);
//
//                                // Drop packets with high round trip times
//                                if (roundTripDelay + tolerableDelay < message.calculateRoundTrip()) {
//
//                                }
                                String modifiedSentence = new String(receivePacket.getData());
                                System.out.println("FROM SERVER:" + modifiedSentence);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            t.start();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean connectTo(String receiverName) {
        Integer tryCount = 0;
        while (tryCount < 3) {
            if (this.send("Hi", receiverName, 9876)) {
                try {
                    clientSocket = new DatagramSocket();
                    IPAddress = InetAddress.getByName(receiverName);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                return true;
            }
            tryCount++;
        }
        System.out.println("Unable to connect to " + receiverName);
        if (clientSocket != null) {
            clientSocket.disconnect();
        }
        return false;
    }

    public void disconnect() {
        clientSocket.close();
        IPAddress = null;
    }

    // Sends zmi information to the receiver, default port for sending data is 9786
    public void sendZMI(ZMI zmi) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Integer chunkSize = 160;
        byte[] sendData = new byte[chunkSize];
        byte[] jsonBytes = Helpers.zmiToByteArray(zmi);
        Integer totalNumber = jsonBytes.length / chunkSize;
        if (totalNumber == 0) {
            totalNumber = 1;
        }
        Integer packetNumber = 1;
        Integer startIndex = 0;
        String id = UUID.randomUUID().toString();;
        HashMap<String, Object> request = new HashMap();
        byte[] responseBytes = jsonBytes;
        while (startIndex < jsonBytes.length) {
            request.put("pn", packetNumber);
            request.put("tn", totalNumber);
            request.put("id", id);
            if (packetNumber == totalNumber) {
                chunkSize = jsonBytes.length - chunkSize * (totalNumber - 1);
            }

            byte[] bytes = Arrays.copyOfRange(responseBytes, startIndex, startIndex + chunkSize);
            request.put("data", Helpers.byteArrayToList(bytes));

            ArrayList<Double> timeStamps = new ArrayList<>();
            timeStamps.add(Helpers.generateTimestamp());
            request.put("ts", timeStamps);
            this.sendPacket(gson.toJson(request).getBytes(), 9876);
            startIndex += chunkSize;
            packetNumber++;
        }
    }
}
