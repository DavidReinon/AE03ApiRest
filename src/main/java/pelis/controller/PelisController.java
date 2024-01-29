package pelis.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/APIpelis")
public class PelisController {

	@GetMapping("/t")
	@ResponseBody
	String mostrarPelis(@RequestParam(name = "id", defaultValue = "") String id, HttpServletResponse response) {
		String resultat = "";
		if (id.equals("all")) {
			resultat = obtenerInfoTodasLasPelis().toString(2);
			if (resultat != null) {
				response.setStatus(202);
				return resultat;
			}
			response.setStatus(404);
			return "Error 404: No ni han pelicules que mostrar";
		}
		resultat = obtenerInfoPeli(id).toString(2);
		if (resultat != null) {
			response.setStatus(202);
			return resultat;
		}
		response.setStatus(404);
		return "Error 404: No se ha trobat la pelicula";
	}

	@PostMapping("/novaPeli")
	ResponseEntity<String> novaPeli(@RequestBody String jsonNovaPeli) {
		return insertarPeli(jsonNovaPeli);

	}

	@PostMapping("/novaRessenya")
	ResponseEntity<String> novaRessenya(@RequestBody String jsonNovaPeli) {
		return insertarResenya(jsonNovaPeli);

	}

	@PostMapping("/nouUsuari")
	ResponseEntity<String> nouUsuari(@RequestBody String jsonNouUsuari) {
		return registrarUsuari(jsonNouUsuari);
	}

	/**
	 * Procesa el número de respuesta y devuelve una ResponseEntity con un mensaje
	 * correspondiente.
	 *
	 * @param numeroResposta Número de respuesta a procesar.
	 * @return ResponseEntity con un mensaje y el código de estado HTTP
	 *         correspondiente.
	 */
	private ResponseEntity<String> procesarResposta(int numeroResposta) {
		switch (numeroResposta) {
		case 401:
			return new ResponseEntity<>("Error 401: No autorizado", HttpStatus.UNAUTHORIZED);
		case 400:
			return new ResponseEntity<>("Error 400: Solicitud mal formada", HttpStatus.BAD_REQUEST);
		case 404:
			return new ResponseEntity<>("Error 404: Recurso no encontrado", HttpStatus.NOT_FOUND);
		case 202:
			return new ResponseEntity<>("Operación exitosa", HttpStatus.ACCEPTED);
		case 500:
			return new ResponseEntity<>("Operación exitosa", HttpStatus.INTERNAL_SERVER_ERROR);
		default:
			return new ResponseEntity<>("Operación exitosa", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Verifica si un usuario está autorizado.
	 *
	 * @param usuari Nombre de usuario a verificar.
	 * @return true si el usuario está autorizado, false si no lo está.
	 */
	private boolean usuarioAutorizado(String usuari) {
		try {
			File autoritzats = new File("autoritzats.txt");

			if (!autoritzats.exists()) {
				return false;
			}

			BufferedReader br = new BufferedReader(new FileReader(autoritzats));
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().equals(usuari)) {
					br.close();
					return true;
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Registra un nuevo usuario.
	 *
	 * @param jsonNouUsuari JSON con la información del nuevo usuario.
	 * @return ResponseEntity con un mensaje y el código de estado HTTP
	 *         correspondiente.
	 */
	private ResponseEntity<String> registrarUsuari(String jsonNouUsuari) {
		try {
			JSONObject obj = new JSONObject(jsonNouUsuari);

			if (!obj.has("usuari")) {
				return procesarResposta(400);
			}

			String nouUsuari = obj.getString("usuari");

			File autoritzats = new File("autoritzats.txt");

			if (!autoritzats.exists()) {
				autoritzats.createNewFile();
			}

			try (FileWriter fw = new FileWriter(autoritzats, true)) {
				fw.write(nouUsuari + "\n");
			}

			return procesarResposta(202);
		} catch (Exception e) {
			e.printStackTrace();
			return procesarResposta(500);
		}
	}

	/**
	 * Obtiene la información de una película específica.
	 *
	 * @param id ID de la película.
	 * @return JSONObject con la información de la película o null si no se
	 *         encuentra.
	 */
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

	/**
	 * Obtiene la información de todas las películas.
	 *
	 * @return JSONObject con un array de objetos JSON que representan las
	 *         películas.
	 */
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

	/**
	 * Inserta una nueva reseña para una película existente.
	 *
	 * @param jsonNovaRessenya JSON con la información de la nueva reseña.
	 * @return ResponseEntity con un mensaje y el código de estado HTTP
	 *         correspondiente.
	 */
	private ResponseEntity<String> insertarResenya(String jsonNovaRessenya) {
		try {
			JSONObject obj = new JSONObject(jsonNovaRessenya);

			// Validación del JSON
			if (!obj.has("usuari") || !obj.has("id") || !obj.has("ressenya")) {
				// Manejar caso en que los campos necesarios no estén presentes
				return procesarResposta(400);

			}

			String usuari = obj.getString("usuari");
			String id = obj.getString("id");
			String ressenya = obj.getString("ressenya");

			if (!usuarioAutorizado(usuari)) {
				return procesarResposta(401);
			}

			File directori = new File("pelis");
			if (!directori.exists()) {
				return procesarResposta(404);
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
				return procesarResposta(404);
			}

			File estaPelicula = new File(directori, id + ".txt");

			// Utilizar try-with-resources para garantizar el cierre adecuado del recurso
			try (FileWriter fw = new FileWriter(estaPelicula, true)) {
				fw.write(usuari + ": " + ressenya + "\n");
			}

		} catch (Exception e) {
			return procesarResposta(500);
		}
		return procesarResposta(202);

	}

	/**
	 * Inserta una nueva película.
	 *
	 * @param jsonNovaPeli JSON con la información de la nueva película.
	 * @return ResponseEntity con un mensaje y el código de estado HTTP
	 *         correspondiente.
	 */
	private ResponseEntity<String> insertarPeli(String jsonNovaPeli) {
		try {
			JSONObject obj = new JSONObject(jsonNovaPeli);

			// Validación del JSON
			if (!obj.has("usuari") || !obj.has("titol")) {
				// Manejar caso en que los campos necesarios no estén presentes
				return procesarResposta(400);

			}

			String usuari = obj.getString("usuari");
			String titol = obj.getString("titol");

			if (!usuarioAutorizado(usuari)) {
				return procesarResposta(401);
			}

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
			return procesarResposta(500);
		}
		return procesarResposta(202);
	}
}
