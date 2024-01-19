package pelis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PelisController {
	@GetMapping("/hola")
	String home() {
		return "Hola món!";
	}

	@RequestMapping("/adeu")
	String bye() {
		return "Adéu món!";
	}

	@GetMapping("/salutacio")
	String saludo(@RequestParam(value = "nom") String strNombre) {
		return "¡Hola " + strNombre + "!";
	}
}
