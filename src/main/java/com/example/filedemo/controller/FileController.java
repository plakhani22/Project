package com.example.filedemo.controller;

import com.example.filedemo.auth.Authenticate;
import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;
import com.example.filedemo.property.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;

@RestController
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

	@GetMapping("/executeCommand/{command:.+}")
	public ResponseEntity<String> executeCommand(@PathVariable String command,HttpServletRequest request) {

		
		logger.debug("IN GenCerts function");
		logger.info("Executing commnad: " + new String(command) + "from node: " + hostname + "/" + ipaddress);
		Process p = null;
		BufferedReader r = null;
		try {
			Random randon = new Random();
			int uud = (int) (randon.nextDouble() * 10000);
			String name = new String(environment + service + "_" + uud + ".sh");
			ProcessBuilder builder = new ProcessBuilder("bash", "-c",
					"cd home/ubuntu &&"+command);
			builder.redirectErrorStream(true);
			p = builder.start();
			p.waitFor();
			logger.info("command execution done");
		} catch (IOException e) {
			logger.error("Error in process builder");
		} catch (InterruptedException e) {
			logger.error("Error in process builder");
		}
		
		String line;
		try {
			while ((line = r.readLine()) != null) {
				response += line + "\n";
			}
		} catch (IOException e) {
			logger.error("Error parsing response");
		}
		logger.debug("Exiting GenCerts function");
		return ResponseEntity.ok().body(response);

	}
	
}
