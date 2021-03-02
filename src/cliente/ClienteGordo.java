package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class ClienteGordo implements Serializable {


    private static final long serialVersionUID = 1L;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    List<Future<String>> listaFutures = new ArrayList<>();

    public static void main(String[] args) {
        ClienteGordo clienteGordo = new ClienteGordo();
        clienteGordo.conectar();
        clienteGordo.enviarArquivos("virusFolderToScan");
        clienteGordo.receberResposta();
        clienteGordo.sair();
    }

    public void conectar() {

        try {
            socket = new Socket("localhost", 1234);
            ou = socket.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarArquivos(String pathFolder) {

        try {
            Stream<Path> pathArquivos = Files.walk(Paths.get(pathFolder));

            List<InitSignature> listaInitSignatures = new ArrayList<>();

            List<String> listaNamesSignatures = new ArrayList<>();

            pathArquivos.parallel().filter(path -> (Files.isRegularFile(path))).forEach(path -> {
                listaInitSignatures.add(new InitSignature(path.toString()));

            });

            pathArquivos.close();

            ExecutorService executorInitSignature = Executors.newFixedThreadPool(listaInitSignatures.size());

            try {
                listaFutures = executorInitSignature.invokeAll(listaInitSignatures);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            listaFutures.forEach(future -> {
                try {
                    listaNamesSignatures.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            listaNamesSignatures.forEach(nomeSignature -> {
                try {
                    bfw.write(nomeSignature + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bfw.write("FIM");

            bfw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n Mensagem enviada");

    }

    public void receberResposta() {

        InputStream in;
        try {
            in = socket.getInputStream();

            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader bfr = new BufferedReader(inr);
            String msg = "";

            while (msg != null) {

                if (bfr.ready()) {

                    msg = bfr.readLine();

                    System.out.println(msg);

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sair() {

        try {
            bfw.close();
            ouw.close();
            ou.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
