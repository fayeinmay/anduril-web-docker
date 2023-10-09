package de.fayedev.andurilwebdocker.util;

public class NameHelper {

    private NameHelper() {
    }

    public static String convertNameToBuildName(String name) {
        return name.replace("cfg-", "anduril.").replace(".h", ".hex");
    }

    public static String convertNameToDockerArgument(String name) {
        return name.replace("cfg-", "").replace(".h", "");
    }
}
