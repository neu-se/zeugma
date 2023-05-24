package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class CorporaScanner {
    private final File directory;

    public CorporaScanner(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        this.directory = directory;
    }

    private static Corpus createCorpus(Path file) throws IOException {
        String content = null;
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(
                file)))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                if (entry.isFile()) {
                    if ("./summary.json".equals(entry.getName())) {
                        content = new String(IOUtils.toByteArray(in));
                        break;
                    }
                }
            }
        }
        if (content == null) {
            throw new IllegalStateException("Invalid meringue archive: " + file);
        }
        String testClassName = findValue("testClassName", content);
        String testMethodName = findValue("testMethodName", content);
        String frameworkClassName = findValue("frameworkClassName", content);
        if (testClassName == null || testMethodName == null || frameworkClassName == null) {
            throw new IllegalStateException("Invalid meringue summary file: " + file);
        }
        String[] split = testClassName.split("\\.");
        String subject = split[split.length - 1].replace("Fuzz", "");
        split = frameworkClassName.split("\\.");
        String fuzzer = split[split.length - 1].replace("Framework", "");
        if (content.contains("-Dzeugma.crossover=none")) {
            fuzzer += "-None";
        }
        return new Corpus(fuzzer, subject, testClassName, testMethodName, file.toFile());
    }

    private static String findValue(String key, String content) {
        String target = String.format("\"%s\": \"", key);
        int i = content.indexOf(target);
        if (i == -1) {
            return null;
        }
        int start = i + target.length();
        int end = content.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        return content.substring(start, end);
    }

    public List<Corpus> scan() throws IOException {
        List<Corpus> corpora = new ArrayList<>();
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if ("meringue.tgz".equals(file.getFileName().toString())) {
                    corpora.add(createCorpus(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return corpora;
    }
}