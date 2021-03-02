package servidor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 * @author Airton 07/03/2018
 *
 *         Esta classe fará o escaneamento de um diretório (virusFolderToScan)
 *         buscando por virus que estão contidos em um direório base onde todos
 *         os arquivos são tidos como vírus válidos (virusDB).
 *
 */
public class VirusScanning {

	private static final String VIRUS_DB_PATH = "virusDB";
	private List<Future<String>> listaFutureSignatureDB = new ArrayList<>();
	private List<Future<String>> listaFutureNewVirus = new ArrayList<>();
	private List<String> listaVirus = new ArrayList<>();

	private List<String> listaClienteRequest;

	
        public VirusScanning(List<String> listaClienteRequest) {
		this.listaClienteRequest = listaClienteRequest;
	}

	public List<String> scanningSignatures() {

		List<String> listaSignatureDB = startSignaturesBD();

		List<CheckFileVirus> listaCheckVirus = new ArrayList<>();

		listaClienteRequest.forEach(nameSignature -> {
			String[] aux = nameSignature.split("-"); 
			listaCheckVirus.add(new CheckFileVirus(aux[0], aux[1], listaSignatureDB));

		});

		ExecutorService executorCheckVirus = Executors.newFixedThreadPool(listaCheckVirus.size());

		try {
			listaFutureNewVirus = executorCheckVirus.invokeAll(listaCheckVirus);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(
				"Nr signatures: " + listaSignatureDB.size() + "\nNr files to scan: " + listaFutureNewVirus.size());

		listaFutureNewVirus.parallelStream().forEach(nameVirus -> {
			try {
				if (nameVirus.get() != null)
					listaVirus.add(nameVirus.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});

		executorCheckVirus.shutdown();

		return listaVirus;
	}

	private List<String> startSignaturesBD() {
		Stream<Path> pathVirusDB;
		List<String> listaSignatureDB = new ArrayList<>();
		try {
			pathVirusDB = Files.walk(Paths.get(VIRUS_DB_PATH));
			List<InitSignatureDB> listaInitSignatureDB = new ArrayList<>();

			pathVirusDB.parallel().filter(path -> (Files.isRegularFile(path))).forEach(path -> {
				listaInitSignatureDB.add(new InitSignatureDB(path.toString()));

			});

			pathVirusDB.close();

			ExecutorService exexutorInitSignDB = Executors.newFixedThreadPool(listaInitSignatureDB.size());

			try {
				listaFutureSignatureDB = exexutorInitSignDB.invokeAll(listaInitSignatureDB);
			} catch (InterruptedException ex) {

			}

			listaFutureSignatureDB.parallelStream().forEach(futureSignature -> {
				try {
					listaSignatureDB.add(futureSignature.get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			});

			System.out.println("Finished signatureBD initialization");

			exexutorInitSignDB.shutdown();

		} catch (IOException e) {
			e.printStackTrace();

		}

		return listaSignatureDB;
	}

}