package servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorClienteGordo extends Thread {

    private static ServerSocket server;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    public ServidorClienteGordo(Socket con) {
        this.con = con;
        try {
            in = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {

            server = new ServerSocket(1234);
            System.out.println("Servidor ativo ");

            while (true) {
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new ServidorClienteGordo(con);
                t.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {

            String aux;
            OutputStream out = this.con.getOutputStream();// enviar dados para o cliente (writer)
            Writer ouw = new OutputStreamWriter(out);
            BufferedWriter bfw = new BufferedWriter(ouw);

            List<String> listaArquivos = new ArrayList<>();
            System.out.println("Recebido: " + bfr.readLine());

            while (true) {
                if (!(aux = bfr.readLine()).equalsIgnoreCase("FIM")) {
                } else {
                    break;
                }
            }
            listaArquivos.add(bfr.readLine());

            VirusScanning virusScanning = new VirusScanning(listaArquivos);

            List<String> respostas = virusScanning.scanningSignatures();

            respostas.forEach(resposta -> {
                try {
                    bfw.write(resposta + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bfw.write("FIM");

            bfw.flush();

            while (this.con.isConnected()) {

            }

            bfw.close();
            ouw.close();
            out.close();
            this.con.close();

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

}
