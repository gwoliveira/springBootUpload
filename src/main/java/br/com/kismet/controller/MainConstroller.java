package br.com.kismet.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.kismet.entity.Store;
import br.com.kismet.repository.StoreRepository;

@Controller
public class MainConstroller {
	final Path rootDir;
	final StoreRepository storeRepository;

	@Autowired
	public MainConstroller(StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
		this.rootDir = Paths.get("/tmp");
	}

	@GetMapping("/")
	public String form(Model model) {
		model.addAttribute("files", storeRepository.findAll());
		return "uploadForm";
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
			throws IOException {

		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");
		Path filePath = rootDir.resolve(file.getOriginalFilename());
		Files.copy(file.getInputStream(), filePath);
		storeRepository.save(new Store(file.getOriginalFilename(), filePath.toString()));
		return "redirect:/";
	}

	@GetMapping("/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws MalformedURLException {
		UrlResource file = new UrlResource(rootDir.resolve(filename).toUri());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
}
