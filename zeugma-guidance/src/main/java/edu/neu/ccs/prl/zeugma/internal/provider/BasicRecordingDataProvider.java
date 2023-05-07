package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.Math;

import java.util.Random;

public class BasicRecordingDataProvider extends ReaderBackedDataProvider {
    private final PrimitiveReader reader;
    private final int size;

    public BasicRecordingDataProvider(ByteProducer producer, int size) {
        this.reader = new PrimitiveReader(producer);
        this.size = size;
    }

    @Override
    public int remainingBytes() {
        return Math.max(0, size - getNumberConsumed());
    }

    @Override
    protected PrimitiveReader getReader() {
        return reader;
    }

    @Override
    public boolean close() {
        return false;
    }

    public static DataProviderFactory createFactory(Random random, int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException();
        }
        return values -> {
            if (values.size() > maxSize) {
                values = values.trim(maxSize);
            }
            return new BasicRecordingDataProvider(new ExpandingByteProducer(random, maxSize, values), values.size());
        };
    }
}
