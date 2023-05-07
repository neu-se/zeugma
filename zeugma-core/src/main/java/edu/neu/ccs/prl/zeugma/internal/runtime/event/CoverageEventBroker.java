package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;

/**
 * Before notifying the subscriber of an event, this broker must check whether the event was triggered from
 * within a subscriber's handling of a different event. This "inner" event should be suppressed to prevent the risk
 * of triggering an infinite loop. This suppression is done using {@link ThreadFieldAccessor#reserve()}.
 */
public final class CoverageEventBroker {
    private static volatile CoverageEventSubscriber subscriber = null;

    private CoverageEventBroker() {
        throw new AssertionError();
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.PUBLISH_COVERAGE)
    public static void covered(ClassModel model, int probeIndex) {
        CoverageEventSubscriber s = subscriber;
        if (s != null && model != null && ThreadFieldAccessor.reserve()) {
            try {
                s.cover(model, probeIndex);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void setSubscriber(CoverageEventSubscriber subscriber) {
        // Ensure that ThreadFieldAccessor is loaded
        if (ThreadFieldAccessor.reserve()) {
            ThreadFieldAccessor.free();
        }
        CoverageEventBroker.subscriber = subscriber;
    }
}