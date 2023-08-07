package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestReport;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.parametric.AttemptUnawareGenerationStatus;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.parametric.ProviderBackedRandomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HintDeriverITCase {
    private static final DataProviderFactory FACTORY = BasicRecordingDataProvider.createFactory(
        null,
        Integer.MAX_VALUE);

    @AfterEach
    void clearSubscriber() {
        ComparisonEventBroker.setSubscriber(null);
        GenerateEventBroker.setSubscriber(null);
    }

    @Test
    void hintDerivedForComparisonsAgainstGenerated() {
        String[] targets = new String[] {"tomato", "cheese", "apple"};
        Consumer<Supplier<String>> runner = (s) -> {
            for (String target : targets) {
                //noinspection ResultOfMethodCallIgnored
                s.get().equals(target);
            }
        };
        ByteList values = ByteArrayList.range(Byte.MIN_VALUE, Byte.MAX_VALUE);
        SimpleSet<StringHint> hints = new HintDeriver(new MockFuzzTarget(runner)).derive(values);
        Assertions.assertEquals(targets.length, hints.size());
    }

    @Test
    void hintSatisfiesComparison() {
        String target = "tomato";
        Consumer<Supplier<String>> runner = (s) -> {
            //noinspection ResultOfMethodCallIgnored
            s.get().equals(target);
        };
        ByteList values = ByteArrayList.range(Byte.MIN_VALUE, Byte.MAX_VALUE);
        MockFuzzTarget fuzzTarget = new MockFuzzTarget(runner);
        SimpleSet<StringHint> hints = new HintDeriver(fuzzTarget).derive(values);
        Assertions.assertEquals(1, hints.size());
        StringHint hint = hints.getBackingMap().entryIterator().next().getKey();
        ByteList recording = fuzzTarget.report.getRecording();
        AtomicBoolean match = new AtomicBoolean(false);
        fuzzTarget = new MockFuzzTarget(s -> {
            match.set(target.equals(s.get()));
        });
        fuzzTarget.run(FACTORY.create(hint.apply(recording)));
        Assertions.assertTrue(match.get());
    }

    @Test
    void hintNotDerivedForComparisonsAgainstNotGenerated() {
        String[] targets = new String[] {"tomato", "cheese", "apple"};
        Consumer<Supplier<String>> runner = (s) -> {
            for (String target : targets) {
                s.get();
                //noinspection ResultOfMethodCallIgnored
                "target".equals(target);
            }
        };
        ByteList values = ByteArrayList.range(Byte.MIN_VALUE, Byte.MAX_VALUE);
        SimpleSet<StringHint> hints = new HintDeriver(new MockFuzzTarget(runner)).derive(values);
        Assertions.assertEquals(0, hints.size());
    }

    static final class MockFuzzTarget implements FuzzTarget {
        private final Consumer<Supplier<String>> consumer;
        private TestReport report;

        MockFuzzTarget(Consumer<Supplier<String>> consumer) {
            this.consumer = consumer;
        }

        @Override
        public int getMaxTraceSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxInputSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String getDescriptor() {
            return "Mock";
        }

        @Override
        public TestReport run(RecordingDataProvider provider) {
            SourceOfRandomness source = new ProviderBackedRandomness(provider, Integer.MAX_VALUE);
            GenerationStatus status = new AttemptUnawareGenerationStatus(source, 10);
            StringGenerator generator = new StringGenerator();
            consumer.accept(() -> generator.generate(source, status));
            return report = new TestReport(null, provider.getRecording(), this);
        }
    }
}