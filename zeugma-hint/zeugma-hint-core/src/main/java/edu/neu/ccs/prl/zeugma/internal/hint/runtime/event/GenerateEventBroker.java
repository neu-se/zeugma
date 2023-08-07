package edu.neu.ccs.prl.zeugma.internal.hint.runtime.event;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.ThreadFieldAccessor;

public final class GenerateEventBroker {
    public static final int FLAG = 0xf722;
    private static final int MAX_STRING_LENGTH = 1024;
    private static volatile GenerateEventSubscriber subscriber = null;

    private GenerateEventBroker() {
        throw new AssertionError();
    }

    public static void setSubscriber(GenerateEventSubscriber subscriber) {
        // Ensure that ThreadFieldAccessor is loaded
        if (ThreadFieldAccessor.reserve()) {
            ThreadFieldAccessor.free();
        }
        GenerateEventBroker.subscriber = subscriber;
    }

    public static int starting() {
        GenerateEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                return s.starting();
            } finally {
                ThreadFieldAccessor.free();
            }
        }
        return -1;
    }

    public static void finished(Object generated, int start) {
        GenerateEventSubscriber s = subscriber;
        if (s != null && start != -1 && ThreadFieldAccessor.reserve()) {
            try {
                s.finished(start, generated);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static boolean checkFlag(Random random) {
        return random.nextInt() == FLAG;
    }

    public static String produceString(Random random) {
        byte[] values = new byte[random.nextInt(MAX_STRING_LENGTH)];
        random.nextBytes(values);
        return new String(values);
    }
}
