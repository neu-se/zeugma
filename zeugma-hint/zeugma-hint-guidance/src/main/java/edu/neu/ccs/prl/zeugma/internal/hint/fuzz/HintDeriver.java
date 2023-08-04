package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;


import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestObserver;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestRunner;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class HintDeriver implements TestObserver {
    private final TestRunner runner;
    private ComparisonRecorder recorder;
    private GenerateCollector collector;
    private SimpleSet<StringHint> hints = new SimpleSet<>();

    public HintDeriver(FuzzTarget target) {
        DataProviderFactory factory = BasicRecordingDataProvider.createFactory(null, target.getMaxInputSize());
        this.runner = new TestRunner(target, factory, this);
    }

    @Override
    public synchronized void starting(ByteList input) {
        recorder = new ComparisonRecorder(input);
        collector = new GenerateCollector(input);
        ComparisonEventBroker.setSubscriber(recorder);
        GenerateEventBroker.setSubscriber(collector);
    }

    @Override
    public synchronized void finished() {
        ComparisonEventBroker.setSubscriber(null);
        GenerateEventBroker.setSubscriber(null);
    }

    private synchronized void createHints(Object oldValue, String newValue) {
        // Operand matches entire generated object
        // TODO partial matches possibly with minimum length
        SimpleList<Interval> sources = collector.getSources(oldValue);
        if (sources != null) {
            for (int i = 0; i < sources.size(); i++) {
                hints.add(new StringHint(sources.get(i), newValue));
            }
        }
    }

    void visit(StringComparison comparison) {
        String o1 = comparison.getOperand1();
        String o2 = comparison.getOperand2();
        createHints(o1, o2);
        createHints(o2, o1);
    }

    public synchronized SimpleSet<StringHint> derive(ByteList values) {
        try {
            runner.run(values);
            if (recorder != null && collector != null) {
                recorder.accept(this);
            }
            return hints;
        } finally {
            recorder = null;
            collector = null;
            hints = new SimpleSet<>();
        }
    }
}
