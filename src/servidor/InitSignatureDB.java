package servidor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class InitSignatureDB implements Callable<String> {

    private static final int SIGNATURE_SIZE = 1348;

    private String path;

    public InitSignatureDB(String path) {
        this.path = path;
    }

    public String call() {
        return geraInitSignatureDB(this.path);
    }

    private String geraInitSignatureDB(String path) {
        String signature = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            char[] buffer = new char[SIGNATURE_SIZE];

            File virus  = new File(path);

            FileReader signatureVirus = new FileReader(virus);
            int totalRead = 0;
            signatureVirus.read(buffer, totalRead, buffer.length - totalRead);
            signature = byteArrayToHexString(md.digest(new String(buffer).getBytes()));
            
            signatureVirus.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }

        return signature;
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}