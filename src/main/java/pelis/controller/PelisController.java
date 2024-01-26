package pelis.controller;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import clases.FiltreExtensio;

@RestController
@RequestMapping("/APIpelis")
public class PelisController {
	@GetMapping("/hola")
	String home() {
		return "Hola món!";
	}


	@PostMapping("/novaPeli")
	ResponseEntity<Void> novaPeli(@RequestBody String jsonNovaPeli) {
		if (insertarPeli(jsonNovaPeli)) {
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	private boolean insertarPeli(String jsonNovaPeli) {
		boolean resposta = true;
		try {
			JSONObject obj = new JSONObject(jsonNovaPeli);

			// Validación del JSON
			if (!obj.has("usuari") || !obj.has("titol")) {
				// Manejar caso en que los campos necesarios no estén presentes
				resposta = false;
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
