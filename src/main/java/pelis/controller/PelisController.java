package pelis.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import clases.FiltreExtensio;

@RestController
@RequestMapping("/APIpelis")
public class PelisController {

	@GetMapping("/t")
	@ResponseBody
	String mostrarPelis(@RequestParam(name = "id", defaultValue = "") String id) {
		String resultat = "";
		if (id.equals("all")) {
			resultat = obtenerInfoTodasLasPelis().toString(2);
			if (resultat != null)
				return resultat;
			return "Error: No ni han pelicules que mostrar";
		}
		resultat = obtenerInfoPeli(id).toString(2);
		if (resultat != null)
			return resultat;
		return "Error: No se ha trobat la pelicula";
	}

	@PostMapping("/novaPeli")
	ResponseEntity<Void> novaPeli(@RequestBody String jsonNovaPeli) {
		if (insertarPeli(jsonNovaPeli)) {
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping("/novaRessenya")
	ResponseEntity<Void> novaRessenya(@RequestBody String jsonNovaPeli) {
		if (insertarResenya(jsonNovaPeli)) {
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	private JSONObject obtenerInfoPeli(String id) {
		try {
			File fitxer = new File("pelis", id + ".txt");

			if (!fitxer.exists()) {
				return null; // No se encontró la película
			}

			// Crear un objeto JSON para almacenar la información de la película
			JSONObject infoPeliJson = new JSONObject();

			// Configurar el ID y el título en el objeto JSON
			infoPeliJson.put("id", id);

			// Leer el archivo línea por línea
			String linea;
			try (BufferedReader br = new BufferedReader(new FileReader(fitxer))) {
				linea = br.readLine();
				String titol = linea.split(":")[1].trim();

				// Configurar el título en el objeto JSON
				infoPeliJson.put("titol", titol);

				// Crear un array JSON para almacenar las resenyes
				JSONArray ressenyesArray = new JSONArray();

				// Leer el resto de las líneas para obtener las resenyes
				while ((linea = br.readLine()) != null) {
					ressenyesArray.put(linea);
				}

				// Configurar el array de resenyes en el objeto JSON
				infoPeliJson.put("ressenyes", ressenyesArray);
			}

			return infoPeliJson;

		} catch (Exception e) {
			return null;
		}
	}

	private JSONObject obtenerInfoTodasLasPelis() {
		// Crear un array JSON para almacenar los objetos JSON de cada archivo
		JSONArray jsonArray = new JSONArray();
		try {

			File directori = new File("pelis");
			if (!directori.exists()) {
				return null;
			}

			// Obtener la lista de archivos en el directorio
			File[] llistaFitxers = directori.listFiles(new FiltreExtensio(".txt"));

			for (File fitxer : llistaFitxers) {
				// Obtener el id del nombre del archivo
				String id = fitxer.getName().replaceFirst("[.][^.]+$", ""); // Quitar la extensión

				// Crear un objeto JSON para cada archivo
				JSONObject peliculaJson = new JSONObject();
				peliculaJson.put("id", id);

				// Leer el archivo línea por línea
				try (BufferedReader br = new BufferedReader(new FileReader(fitxer))) {
					String linea = br.readLine();
					String titol = linea.split(":")[1].trim();

					// Agregar el título al objeto JSON de la película
					peliculaJson.put("titol", titol);
				}

				// Agregar el objeto JSON de la película al array
				jsonArray.put(peliculaJson);
			}

		} catch (Exception e) {
			return null;
		}
		// Crear un objeto JSON final con el array de películas
		JSONObject jsonObjectFinal = new JSONObject();
		jsonObjectFinal.put("titols", jsonArray);

		return jsonObjectFinal;
	}

	private boolean insertarResenya(String jsonNovaPeli) {
		try {
			JSONObject obj = new JSONObject(jsonNovaPeli);

			// Validación del JSON
			if (!obj.has("usuari") || !obj.has("id") || !obj.has("ressenya")) {
				// Manejar caso en que los campos necesarios no estén presentes
				return false;

			}

			String usuari = obj.getString("usuari");
			String id = obj.getString("id");
			String ressenya = obj.getString("ressenya");

			File directori = new File("pelis");
			if (!directori.exists()) {
				return false;
			}

			// Obtener la lista de archivos en el directorio
			String[] llistaFitxers = directori.list(new FiltreExtensio(".txt"));

			// Verificar si el archivo ya existe en la lista
			boolean archivoExistente = false;
			for (String nombreArchivo : llistaFitxers) {
				if (nombreArchivo.equals(id + ".txt")) {
					archivoExistente = true;
					break;
				}
			}

			if (!archivoExistente) {
				return false;
			}

			File estaPelicula = new File(directori, id + ".txt");

			// Utilizar try-with-resources para garantizar el cierre adecuado del recurso
			try (FileWriter fw = new FileWriter(estaPelicula, true)) {
				fw.write(usuari + ": " + ressenya + "\n");
			}

		} catch (Exception e) {
			return false;
		}
		return true;

	}

	private boolean insertarPeli(String jsonNovaPeli) {
		boolean resposta = true;
		try {
			JSONObject obj = new JSONObject(jsonNovaPeli);

			// Validación del JSON
			if (!obj.has("usuari") || !obj.has("titol")) {
				// Manejar caso en que los campos necesarios no estén presentes
				return resposta = false;

			}

			String usuari = obj.getString("usuari");
			String titol = obj.getString("titol");

			File directori = new File("pelis");
			if (!directori.exists()) {
				directori.mkdirs(); // Crea el directorio si no existe
			}

			// Obtener la lista de archivos en el directorio
			String[] llistaFitxers = directori.list(new FiltreExtensio(".txt"));

			// Identificador único para la película
			int identificadorPelicula = llistaFitxers.length + 1;

			// Crear el archivo para la película
			File estaPelicula = new File(directori, identificadorPelicula + ".txt");

			// Utilizar try-with-resources para garantizar el cierre adecuado del recurso
			try (FileWriter fw = new FileWriter(estaPelicula, true)) {
				fw.write("Titol: " + titol + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resposta = false;

		}
		return resposta;
	}
}
