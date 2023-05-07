package edu.neu.ccs.prl.zeugma.internal.instrument;

import java.io.File;

public interface Transformer {
    File getAgentJar();

    File getCoreJar();

    byte[] transform(byte[] classFileBuffer);
}
