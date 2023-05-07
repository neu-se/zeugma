package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.util.Random;

public final class TwoPointSplicer implements Splicer {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * True if the first parent should be chosen at random.
     */
    private final boolean randomFirstParent;

    public TwoPointSplicer(Random random) {
        this(random, true);
    }

    public TwoPointSplicer(Random random, boolean randomFirstParent) {
        if (random == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.randomFirstParent = randomFirstParent;
    }

    @Override
    public ByteList splice(ByteList parent1, ByteList parent2) {
        if (randomFirstParent && random.nextBoolean()) {
            // Randomly choose the first parent
            ByteList temp = parent1;
            parent1 = parent2;
            parent2 = temp;
        }
        int start1 = parent1.size() == 0 ? 0 : random.nextInt(parent1.size());
        int end1 = parent1.size() == 0 ? 0 : random.nextInt(parent1.size());
        int start2 = parent2.size() == 0 ? 0 : random.nextInt(parent2.size());
        int end2 = parent2.size() == 0 ? 0 : random.nextInt(parent2.size());
        if (start1 > end1) {
            int temp = start1;
            start1 = end1;
            end1 = temp;
        }
        if (start2 > end2) {
            int temp = start2;
            start2 = end2;
            end2 = temp;
        }
        return recombine(parent1, start1, end1, parent2, start2, end2);
    }

    ByteList recombine(ByteList parent1, int start1, int end1, ByteList parent2, int start2, int end2) {
        ByteList child = new ByteArrayList();
        for (int i = 0; i < start1 && i < parent1.size(); i++) {
            child.add(parent1.get(i));
        }
        for (int i = start2; i < parent2.size() && i < end2 + 1; i++) {
            child.add(parent2.get(i));
        }
        for (int i = end1 + 1; i < parent1.size(); i++) {
            child.add(parent1.get(i));
        }
        return child;
    }

    @Override
    public String toString() {
        return "TwoPointSplicer";
    }
}

