package de.fayedev.andurilwebdocker.service;

import de.fayedev.andurilwebdocker.model.AndurilFeatureFlag;
import de.fayedev.andurilwebdocker.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FeatureFlagService {

    private final FileHelper fileHelper;

    public FeatureFlagService(FileHelper fileHelper) {
        this.fileHelper = fileHelper;
    }

    public List<AndurilFeatureFlag> getFeatureFlags(String fileName) {
        List<String> file = readFileLines(fileName);
        List<AndurilFeatureFlag> flags = new ArrayList<>();

        for (int i = 0; i < file.size(); i++) {
            AndurilFeatureFlag flag = readFlag(fileName, file.get(i), i); // Index 0 for saving feature flag

            if (flag != null) {
                flags.add(flag);
            }
        }

        return flags;
    }

    public void saveFeatureFlags(String fileName, List<AndurilFeatureFlag> flags) {
        List<String> file = readFileLines(fileName);

        for (AndurilFeatureFlag flag : flags) {
            file.set(flag.getLine(), buildFlagString(flag));
        }

        try {
            Files.write(fileHelper.getFilePath(fileName), file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("IO Error: " + e);
        }
    }

    private List<String> readFileLines(String fileName) {
        try {
            return Files.readAllLines(fileHelper.getFilePath(fileName));
        } catch (IOException e) {
            log.error("IO Error: " + e);
            return new ArrayList<>();
        }
    }

    private String buildFlagString(AndurilFeatureFlag flag) {
        StringBuilder sb = new StringBuilder();

        if (flag.isDefined()) {
            sb.append("#define");
        } else {
            sb.append("#undef");
        }

        sb.append(" ");
        sb.append(flag.getName());

        if (flag.isDefined() && flag.getValue() != null && !flag.getValue().isBlank()) {
            sb.append(" ");
            sb.append(flag.getValue());
        }

        return sb.toString();
    }

    private AndurilFeatureFlag readFlag(String fileName, String line, int lineCount) {
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
            andurilFeatureFlag.setLine(lineCount);

            if (seperated.size() > 2) {
                andurilFeatureFlag.setValue(seperated.get(2));
            }

            return andurilFeatureFlag;
        }

        if (line.startsWith("#include")) {
            // TODO: Handle
        }

        return null;
    }
}
