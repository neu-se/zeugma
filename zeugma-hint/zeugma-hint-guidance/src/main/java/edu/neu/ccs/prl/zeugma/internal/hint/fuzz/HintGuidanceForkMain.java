package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.io.File;

import edu.neu.ccs.prl.zeugma.internal.guidance.GuidanceBuilder;
import edu.neu.ccs.prl.zeugma.internal.parametric.JqfRunner;

public final class HintGuidanceForkMain {
    private HintGuidanceForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        try {
            GuidanceBuilder builder = new HintGuidanceBuilder(Long.parseLong(args[2]), new File(args[3]));
            ClassLoader classLoader = HintGuidanceForkMain.class.getClassLoader();
            JqfRunner.createInstance(args[0], args[1], classLoader, builder)
                     .run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
}