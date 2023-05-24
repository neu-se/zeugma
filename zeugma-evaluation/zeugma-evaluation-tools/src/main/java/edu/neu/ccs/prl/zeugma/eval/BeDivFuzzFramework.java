package edu.neu.ccs.prl.zeugma.eval;

import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.meringue.ZestFramework;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class BeDivFuzzFramework extends ZestFramework {
    @Override
    public Class<? extends Replayer> getReplayerClass() {
        return BeDivReplayer.class;
    }

    @Override
    public String getMainClassName() {
        return "de.hub.se.jqf.fuzz.div.BeDivDriver";
    }

    @Override
    protected String getJqfVersion() {
        return "2.0-SNAPSHOT-BeDivFuzz";
    }

    @Override
    protected File[] getInputFiles(File directory) {
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(f -> f.getName().startsWith("id_"))
                .filter(f -> !f.getName().contains("_secondary"))
                .toArray(File[]::new);
    }
}