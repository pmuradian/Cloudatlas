package pl.edu.mimuw.cloudatlas.helpers;

import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class Helpers {
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static Byte[] byteArrayToList(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];

        int i=0;
        for(byte b: bytes)
            byteObjects[i++] = b;

        return byteObjects;
    }

    public static byte[] arrayToBytes (ArrayList<Double> arrayList) {
        byte[] bytes = new byte[arrayList.size()];

        int i = 0;
        for (Double b: arrayList) {
            bytes[i++] = b.byteValue();
        }

        return bytes;
    }

    public static Double generateTimestamp() {
        return ((Long)(new Date()).getTime()).doubleValue();
    }

    public static byte[] zmiToByteArray(ZMI zmi) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(zmi);
            out.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        return bytes;
    }

    public static ZMI bytesToZMI(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        ZMI zmi = null;
        try {
            in = new ObjectInputStream(bis);
            zmi = (ZMI) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }
        return zmi;
    }
}
