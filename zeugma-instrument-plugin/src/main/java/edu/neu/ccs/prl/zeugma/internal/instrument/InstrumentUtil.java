package edu.neu.ccs.prl.zeugma.internal.instrument;

import java.io.File;

public final class InstrumentUtil {
    private InstrumentUtil() {
        throw new AssertionError();
    }

    public static String getAgentOption(Class<?> agentClass) {
        return "-javaagent:" + getClassPathElement(agentClass).getAbsolutePath();
    }

    public static String getBootClassPathOption(Class<?> coreClass) {
        return "-Xbootclasspath/a:" + getClassPathElement(coreClass).getAbsolutePath();
    }

    public static File javaHomeToBin(File javaHome) {
        return new File(javaHome, "bin");
    }

    public static File javaHomeToJavaExec(File javaHome) {
        return new File(javaHomeToBin(javaHome), "java");
    }

    public static File javaHomeToJLinkExec(File javaHome) {
        return new File(javaHomeToBin(javaHome), "jlink");
    }

    public static boolean isJavaHome(File directory) {
        return javaHomeToJavaExec(directory).isFile();
    }

    public static boolean isModularJvm(File javaHome) {
        return isJavaHome(javaHome) && javaHomeToJLinkExec(javaHome).isFile();
    }

    public static File getClassPathElement(Class<?> clazz) {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
    }
}