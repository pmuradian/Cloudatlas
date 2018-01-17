package pl.edu.mimuw.cloudatlas.modules.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.util.HashMap;

public class Signer implements HttpHandler {
    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static int NUM_KEY_BITS = 1024;

    public void handle(HttpExchange t) throws IOException {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM);

            keyGenerator.initialize(NUM_KEY_BITS);
            KeyPair keyPair = keyGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            InputStream is = t.getRequestBody();
            String body = Helpers.convertStreamToString(is);
            is.close();
            HashMap<String, Object> response = new HashMap<>();

            // Sign query
            Cipher signCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = signCipher.doFinal(SHA1.calculate(body.getBytes()));

            // response body
            response.put("signature", Helpers.byteArrayToList(encryptedBytes));
            response.put("key", Helpers.publicKeyToString(publicKey));

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String resp = gson.toJson(response);
            byte[] bytes = resp.getBytes();

            t.sendResponseHeaders(200, resp.length());
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
