package de.fayedev.andurilwebdocker.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileHelper {

    @Value("${docker.volume}")
    private String dockerVolume;
    private Path hwdefFolder;
    private Path cfgFolder;

    public static boolean isAndurilFile(String name) {
        return isCfg(name) || isHwdef(name);
    }

    public static boolean isCfg(String name) {
        return name.startsWith("cfg");
    }

    public static boolean isHwdef(String name) {
        return name.startsWith("hwdef");
    }

    public static boolean isHex(String name) {
        return name.endsWith(".hex");
    }

    public static String convertNameToBuildName(String name) {
        return name.replace("cfg-", "anduril.").replace(".h", ".hex");
    }

    public static String convertNameToDockerArgument(String name) {
        return name.replace("cfg-", "").replace(".h", "");
    }

    @PostConstruct
    public void prepareInitialFiles() {
        hwdefFolder = Paths.get(dockerVolume, "anduril2-main");
        cfgFolder = Paths.get(dockerVolume, "anduril2-main", "spaghetti-monster", "anduril");
    }

    public Path getFilePath(String fileName) {
        return Path.of(FileHelper.isHwdef(fileName) ? hwdefFolder.toString() : cfgFolder.toString(), fileName);
    }
}
