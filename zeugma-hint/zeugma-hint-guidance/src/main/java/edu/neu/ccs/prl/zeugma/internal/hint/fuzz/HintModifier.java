package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Modifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

public class HintModifier<E extends HintIndividual> implements Modifier<E> {
    /**
     * Mutates individuals.
     * <p>
     * Non-null.
     */
    private final Mutator mutator;
    /**
     * Chooses the number of stacked modifications to apply.
     * <p>
     * Non-null.
     */
    private final IntProducer countSampler;
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    private final Selector<Object> selector;

    public HintModifier(Random random, Mutator mutator, IntProducer countSampler) {
        if (random == null || mutator == null || countSampler == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.mutator = mutator;
        this.countSampler = countSampler;
        this.selector = new RandomSelector<>(random);
    }

    @Override
    public ByteList modify(E parent, SimpleList<? extends E> population) {
        int count = countSampler.get();
        SimpleList<DerivedMutator> derived = new SimpleList<>(count);
        int mutationCount = count;
        if (!parent.getLocalHints().isEmpty()) {
            for (int i = 0; i < count; i++) {
                if (random.nextBoolean()) {
                    derived.add(selector.select(parent.getLocalHints()));
                    mutationCount--;
                }
            }
        }
        ByteList child = DerivedMutator.apply(derived.toArray(new DerivedMutator[derived.size()]), parent.getInput());
        for (int i = 0; i < mutationCount && mutator.isValid(child); i++) {
            child = mutator.mutate(child);
        }
        return child;
    }
}
