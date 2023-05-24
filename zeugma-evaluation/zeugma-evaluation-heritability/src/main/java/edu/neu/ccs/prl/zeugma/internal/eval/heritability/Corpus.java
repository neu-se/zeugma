package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Math;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

final class Corpus {
    private final String fuzzer;
    private final String subject;
    private final String testClassName;
    private final String testMethodName;
    private final File archive;

    public Corpus(String fuzzer, String subject, String testClassName, String testMethodName, File archive) {
        if (fuzzer == null || subject == null || testClassName == null || testMethodName == null || archive == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
        this.subject = subject;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.archive = archive;
    }

    public String getFuzzer() {
        return fuzzer;
    }

    public String getSubject() {
        return subject;
    }

    public List<ByteList> readInputs(Duration duration) throws IOException {
        if (duration.isNegative()) {
            throw new IllegalArgumentException();
        }
        long firstTimeStamp = Long.MAX_VALUE;
        List<TimestampedInput> elements = new ArrayList<>();
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(
                archive.toPath())))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                if (entry.getName().startsWith("./campaign/corpus/") && entry.isFile()) {
                    ByteList values = new ByteArrayList(IOUtils.toByteArray(in));
                    long lastModified = entry.getLastModifiedDate().getTime();
                    elements.add(new TimestampedInput(values, lastModified));
                    firstTimeStamp = Math.min(lastModified, firstTimeStamp);
                }
            }
        }
        List<ByteList> inputs = new ArrayList<>();
        for (TimestampedInput element : elements) {
            if (element.timestamp - firstTimeStamp < duration.toMillis()) {
                inputs.add(element.input);
            }
        }
        return inputs;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    private static final class TimestampedInput {
        private final ByteList input;
        private final long timestamp;

        private TimestampedInput(ByteList input, long timestamp) {
            this.input = input;
            this.timestamp = timestamp;
        }
    }
}
