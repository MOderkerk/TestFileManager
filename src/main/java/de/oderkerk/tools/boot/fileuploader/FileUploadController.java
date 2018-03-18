package de.oderkerk.tools.boot.fileuploader;

import java.io.IOException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oderkerk.tools.boot.fileuploader.storage.StorageFileNotFoundException;
import de.oderkerk.tools.boot.fileuploader.storage.StorageService;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
@Controller
public class FileUploadController {
	Logger logger = LoggerFactory.getLogger(FileUploadController.class);
	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("listUploadedFiles startet");
		/*
		 * model.addAttribute("files", storageService.loadAll() .map(path ->
		 * MvcUriComponentsBuilder .fromMethodName(FileUploadController.class,
		 * "serveFile", path.getFileName().toString()) .build().toString())
		 * .collect(Collectors.toList()));
		 */
		return "uploadForm";
	}

	@GetMapping("/filter")
	public String listUploadedFilesFiltered(Model model, @RequestParam("stage") String stage,
			@RequestParam("mandant") String mandant) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("listUploadedFiles startet");
		model.addAttribute("files",
				storageService.loadAll(stage, mandant)
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
								.build().toString())
						.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		if (logger.isDebugEnabled())
			logger.debug("serveFile startet");
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("stage") String stage,
			@RequestParam("mandant") String mandant, RedirectAttributes redirectAttributes) {
		if (logger.isDebugEnabled())
			logger.debug("handleFileUpload startet with file {}", file.getName());
		storageService.store(file, stage, mandant);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@PostMapping("/delete")
	public String handleFileDelete(@RequestParam("fileToDelete") String file, RedirectAttributes redirectAttributes) {
		if (logger.isDebugEnabled())
			logger.debug("Delete{} startet", file);
		String[] parts = file.split("/");

		storageService.deleteFile(parts[parts.length - 1]);
		redirectAttributes.addFlashAttribute("message", "You successfully deleted " + file + "!");

		return "redirect:/";
	}

	@PostMapping("/deflate")
	public String handleFileDeflate(@RequestParam("fileToDeflate") String file, RedirectAttributes redirectAttributes) {
		if (logger.isDebugEnabled())
			logger.debug("Deflate of {} startet", file);
		String[] parts = file.split("/");
		try {
			storageService.deflateFile(parts[parts.length - 1]);
			redirectAttributes.addFlashAttribute("message", "You successfully deflated " + file + "!");
		} catch (Exception ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage() + "!");
		}
		return "redirect:/";

	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
