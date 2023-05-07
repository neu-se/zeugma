package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.SynchronizedByteList;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

public abstract class ReaderBackedDataProvider implements RecordingDataProvider {
    private final ByteList recording = SynchronizedByteList.of(new ByteArrayList());

    public ByteList getRecording() {
        return UnmodifiableByteList.of(recording);
    }

    @Override
    public int getNumberConsumed() {
        return recording.size();
    }

    @Override
    public float nextFloat() {
        return PrimitiveWriter.write(recording, getReader().readFloat());
    }

    @Override
    public float nextFloat(float min, float max) {
        return PrimitiveWriter.write(recording, getReader().readFloat(min, max));
    }

    @Override
    public float nextFiniteFloat() {
        return PrimitiveWriter.write(recording, getReader().readFiniteFloat());
    }

    @Override
    public float nextProbabilityFloat() {
        return PrimitiveWriter.write(recording, getReader().readProbabilityFloat());
    }

    @Override
    public double nextDouble() {
        return PrimitiveWriter.write(recording, getReader().readDouble());
    }

    @Override
    public double nextDouble(double min, double max) {
        return PrimitiveWriter.write(recording, getReader().readDouble(min, max));
    }

    @Override
    public double nextFiniteDouble() {
        return PrimitiveWriter.write(recording, getReader().readFiniteDouble());
    }

    @Override
    public double nextProbabilityDouble() {
        return PrimitiveWriter.write(recording, getReader().readProbabilityDouble());
    }

    @Override
    public long nextLong() {
        return PrimitiveWriter.write(recording, getReader().readLong());
    }

    @Override
    public long nextLong(long min, long max) {
        return PrimitiveWriter.write(recording, getReader().readLong(min, max));
    }

    @Override
    public int nextInt() {
        return PrimitiveWriter.write(recording, getReader().readInt());
    }

    @Override
    public int nextInt(int min, int max) {
        return PrimitiveWriter.write(recording, getReader().readInt(min, max));
    }

    @Override
    public int nextInt(int n) {
        return PrimitiveWriter.write(recording, getReader().readInt(n));
    }

    @Override
    public boolean nextBoolean() {
        return PrimitiveWriter.write(recording, getReader().readBoolean());
    }

    @Override
    public char nextChar() {
        return PrimitiveWriter.write(recording, getReader().readChar());
    }

    @Override
    public char nextChar(char min, char max) {
        return PrimitiveWriter.write(recording, getReader().readChar(min, max));
    }

    @Override
    public short nextShort() {
        return PrimitiveWriter.write(recording, getReader().readShort());
    }

    @Override
    public short nextShort(short min, short max) {
        return PrimitiveWriter.write(recording, getReader().readShort(min, max));
    }

    @Override
    public byte nextByte() {
        return PrimitiveWriter.write(recording, getReader().readByte());
    }

    @Override
    public byte nextByte(byte min, byte max) {
        return PrimitiveWriter.write(recording, getReader().readByte(min, max));
    }

    @Override
    public byte[] consumeRemaining() {
        ByteList result = new ByteArrayList();
        while (remainingBytes() > 0) {
            result.add(nextByte());
        }
        return result.toArray();
    }

    protected abstract PrimitiveReader getReader();
}

