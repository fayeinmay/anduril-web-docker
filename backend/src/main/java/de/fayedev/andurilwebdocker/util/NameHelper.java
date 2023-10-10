package de.fayedev.andurilwebdocker.util;

public class NameHelper {

    private NameHelper() {
    }

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
}
