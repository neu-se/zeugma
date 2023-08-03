package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import java.lang.instrument.Instrumentation;

public final class ZeugmaHintAgent {
    private ZeugmaHintAgent() {
        throw new AssertionError();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ZeugmaHintTransformer());
    }
}
