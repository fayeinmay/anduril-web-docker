package de.fayedev.andurilwebdocker.service;

import de.fayedev.andurilwebdocker.model.AndurilFeatureFlag;
import de.fayedev.andurilwebdocker.util.NameHelper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FeatureFlagService {

    @Value("${docker.volume}")
    private String dockerVolume;

    // TODO: Resolve code duplication
    private Path hwdefFolder;
    private Path cfgFolder;

    @PostConstruct
    public void prepareInitialFiles() {
        hwdefFolder = Paths.get(dockerVolume, "anduril2-main");
        cfgFolder = Paths.get(dockerVolume, "anduril2-main", "spaghetti-monster", "anduril");
    }

    public List<AndurilFeatureFlag> getFeatureFlags(String fileName) {
        String folder = NameHelper.isCfg(fileName) ? cfgFolder.toString() : hwdefFolder.toString();
        try {
            List<String> file = Files.readAllLines(Path.of(folder, fileName));
            List<AndurilFeatureFlag> flags = new ArrayList<>();

            for (String line : file) {
                AndurilFeatureFlag flag = readFlag(fileName, line);

                if (flag != null) {
                    flags.add(flag);
                }
            }

            return flags;
        } catch (IOException e) {
            log.error("IO Error: " + e);
            return new ArrayList<>();
        }
    }

    private AndurilFeatureFlag readFlag(String fileName, String line) {
        if (line.startsWith("#define") || line.startsWith("#undef")) {
            AndurilFeatureFlag andurilFeatureFlag = new AndurilFeatureFlag();

            // Examples:
            // #define TEST "test"
            // #define TEST       "test"
            // #define TEST
            // #undef TEST
            // #define TEST //thisiscool
            List<String> seperated = Arrays.stream(line.split(" ")).filter(c -> !c.isBlank()).toList();

            // This should always work...
            andurilFeatureFlag.setDefined(seperated.get(0).equals("#define"));
            andurilFeatureFlag.setName(seperated.get(1));

            andurilFeatureFlag.setFileName(fileName);

            if (seperated.size() > 2) {
                andurilFeatureFlag.setValue(seperated.get(2));
            }

            return andurilFeatureFlag;
        }

        if (line.startsWith("#include")) {
            // Handle
        }

        return null;
    }
}
