package servidor;

import java.util.List;
import java.util.concurrent.Callable;

public class CheckFileVirus implements Callable<String> {

    private List<String> listaSignatureDB;
    private String signature;
    private String nome;

    public CheckFileVirus(String nome, String signature, List<String> listaSignatureDB) {
        this.signature = signature;
        this.listaSignatureDB = listaSignatureDB;
        this.nome = nome;
    }

    public String call() {
        return isInVirusDB(this.signature) ? this.nome : null;
    }

    private boolean isInVirusDB(String signature) {
        boolean flag = false;
        for (String signatureDB : listaSignatureDB) {
            if (signature.equals(signatureDB)) {
                flag = true;
                break;
            }
        }

        return flag;
    }

}