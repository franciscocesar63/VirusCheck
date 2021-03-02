package cliente;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InitSignature implements Callable<String> {

    private static final int SIGNATURE_SIZE = 1348;
    private String path;

    public InitSignature(String path) {
        this.path = path;
    }

    public String call() {
        return inicializar(this.path);
    }

    private String inicializar(String path) {
        String retorno = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            File newVirus = new File(path);
            retorno = newVirus.getName() + "-";
           
            char[] buffer = new char[SIGNATURE_SIZE];
            FileReader readerVirus = new FileReader(newVirus);
           
            int totalRead = 0;
            readerVirus.read(buffer, totalRead, buffer.length - totalRead);
            String signature = byteArrayToHexString(md.digest(new String(buffer).getBytes()));
           
            retorno = retorno + signature;    
            
            readerVirus.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}