package de.fayedev.andurilwebdocker.web;

import de.fayedev.andurilwebdocker.model.AndurilFeatureFlag;
import de.fayedev.andurilwebdocker.model.AndurilFile;
import de.fayedev.andurilwebdocker.service.DockerConnectorService;
import de.fayedev.andurilwebdocker.service.FeatureFlagService;
import de.fayedev.andurilwebdocker.service.FileProcessingService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class FileProcessingRestController {

    private final FileProcessingService fileProcessingService;
    private final DockerConnectorService dockerConnectorService;
    private FeatureFlagService featureFlagService;

    public FileProcessingRestController(FeatureFlagService featureFlagService, FileProcessingService fileProcessingService, DockerConnectorService dockerConnectorService) {
        this.featureFlagService = featureFlagService;
        this.fileProcessingService = fileProcessingService;
        this.dockerConnectorService = dockerConnectorService;
    }

    @PostMapping("/files")
    public void addFiles(@RequestParam("file") MultipartFile[] files) {
        fileProcessingService.saveFiles(files);
    }

    @DeleteMapping("/files/{fileName}")
    public void removeFile(@PathVariable String fileName) {
        fileProcessingService.removeFile(fileName);
    }

    @GetMapping("/files")
    public List<AndurilFile> getFiles() {
        return dockerConnectorService.populateLogs(fileProcessingService.getFiles());
    }

    @GetMapping("/files/{fileName}")
    public FileSystemResource downloadFile(@PathVariable String fileName) {
        return fileProcessingService.downloadFile(fileName);
    }

    @GetMapping("/files/{fileName}/flags")
    public List<AndurilFeatureFlag> getFeatureFlags(@PathVariable String fileName) {
        return featureFlagService.getFeatureFlags(fileName);
    }

    @PatchMapping("/files/reset")
    public void resetFiles() {
        fileProcessingService.resetFiles();
    }

    @PatchMapping("/files/{fileName}/build")
    public void buildFile(@PathVariable String fileName) {
        dockerConnectorService.build(fileName);
    }

}
