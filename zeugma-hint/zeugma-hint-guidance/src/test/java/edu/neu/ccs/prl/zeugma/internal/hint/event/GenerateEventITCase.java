package edu.neu.ccs.prl.zeugma.internal.hint.event;

import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.parametric.AttemptUnawareGenerationStatus;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.parametric.ProviderBackedRandomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GenerateEventITCase {
    private static final DataProviderFactory FACTORY = BasicRecordingDataProvider.createFactory(
        null,
        Integer.MAX_VALUE);

    @AfterEach
    void clearSubscriber() {
        GenerateEventBroker.setSubscriber(null);
    }

    @Test
    void generate() {
        RecordingDataProvider provider = FACTORY.create(ByteArrayList.range((byte) 0, (byte) 102));
        final int[] actualStart = new int[1];
        final Object[] actualGenerated = new Object[1];
        GenerateEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public int starting() {
                return provider.getNumberConsumed();
            }

            @Override
            public void finished(int start, Object generated) {
                actualStart[0] = start;
                actualGenerated[0] = generated;
            }
        });
        // Advance the provider
        provider.nextLong();
        int expectedStart = provider.getNumberConsumed();
        SourceOfRandomness source = new ProviderBackedRandomness(provider, Integer.MAX_VALUE);
        Object generated = new StringGenerator()
            .generate(source, new AttemptUnawareGenerationStatus(source, 10));
        clearSubscriber();
        Assertions.assertSame(expectedStart, actualStart[0]);
        Assertions.assertSame(generated, actualGenerated[0]);
    }
}
