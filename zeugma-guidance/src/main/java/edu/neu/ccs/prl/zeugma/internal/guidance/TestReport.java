package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

/**
 * Information about system behavior observed during the execution of a test.
 */
public final class TestReport {
    /**
     * If the test failed, the failure. Otherwise, if the test did not fail, {@code null}.
     */
    private final Throwable failure;
    /**
     * List of input values consumed during the test execution.
     * <p>
     * Non-null, unmodifiable.
     */
    private final ByteList recording;
    /**
     * The test that was run.
     * <p>
     * Non-null.
     */
    private final FuzzTarget target;

    public TestReport(Throwable failure, ByteList recording, FuzzTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        this.failure = failure;
        this.recording = UnmodifiableByteList.of(new ByteArrayList(recording));
        this.target = target;
    }

    public ByteList getRecording() {
        return recording;
    }

    public Throwable getFailure() {
        return failure;
    }

    public FuzzTarget getTarget() {
        return target;
    }
}
