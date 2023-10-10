package de.fayedev.andurilwebdocker.service;

import de.fayedev.andurilwebdocker.model.AndurilFile;
import de.fayedev.andurilwebdocker.util.NameHelper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FileProcessingService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${docker.volume}")
    private String dockerVolume;

    @Value("${github.baserepo}")
    private String githubBaseRepo;

    private Path hwdefFolder;
    private Path cfgFolder;

    @PostConstruct
    public void prepareInitialFiles() {
        if (isEmpty(Path.of(dockerVolume))) {
            downloadInitialFiles();
        }

        hwdefFolder = Paths.get(dockerVolume, "anduril2-main");
        cfgFolder = Paths.get(dockerVolume, "anduril2-main", "spaghetti-monster", "anduril");
    }

    public List<AndurilFile> getFiles() {
        List<Path> paths;

        try (Stream<Path> walk = Files.walk(Paths.get(dockerVolume))) {
            paths = walk.toList();
        } catch (IOException e) {
            log.error("IO Error: " + e);
            throw new RuntimeException(e);
        }

        List<String> fileNames = paths.stream().filter(c -> Files.isRegularFile(c) && NameHelper.isAndurilFile(c.getFileName().toString()))
                .map(c -> c.getFileName().toString()).sorted().toList();

        List<String> buildFileNames = paths.stream().filter(c -> Files.isRegularFile(c) && NameHelper.isHex(c.getFileName().toString())).
                map(c -> c.getFileName().toString()).sorted().toList();

        return fileNames.stream().map(c -> AndurilFile.builder()
                .name(c)
                .buildName(buildFileNames.stream().filter(x -> x.equals(NameHelper.convertNameToBuildName(c))).findFirst().orElse(null))
                .build()).toList();
    }

    public void saveFiles(MultipartFile[] files) {
        try {
            for (MultipartFile file : files) {
                log.info("Processing file " + file.getOriginalFilename());
                file.transferTo(new File(dockerVolume, file.getOriginalFilename()));
            }
        } catch (IOException e) {
            log.error("IO Error: " + e);
        }
    }

    public void removeFile(String fileName) {
        Path path = NameHelper.isCfg(fileName) ? cfgFolder : hwdefFolder;

        try {
            log.info("Removing file " + fileName);
            Files.delete(Paths.get(path.toString(), fileName));
        } catch (IOException e) {
            log.error("IO Error: " + e);
        }
    }

    public FileSystemResource downloadFile(String fileName) {
        log.info("Serving file " + fileName + " to download");
        return new FileSystemResource(Paths.get(cfgFolder.toString(), fileName).toFile());
    }

    public void resetFiles() {
        try {
            log.info("Resetting file storage...");
            FileUtils.cleanDirectory(new File(dockerVolume));
            downloadInitialFiles();
        } catch (IOException e) {
            log.error("IO Error: " + e);
        }
    }

    private void downloadInitialFiles() {
        log.info("Downloading initial files...");
        File file = restTemplate.execute(githubBaseRepo, HttpMethod.GET, null, clientHttpResponse -> {
            File ret = File.createTempFile("download", "tmp");
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });

        if (file != null) {
            unzipContent(file);
        }
    }

    private void unzipContent(File file) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                File newFile = newFile(new File(dockerVolume), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            log.error("IO Error: " + e);
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("IO Error, entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private boolean isEmpty(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.list(path)) {
                    return entries.findFirst().isEmpty();
                }
            }

            return false;
        } catch (IOException e) {
            log.error("IO Error: " + e);
            return false;
        }
    }

}
