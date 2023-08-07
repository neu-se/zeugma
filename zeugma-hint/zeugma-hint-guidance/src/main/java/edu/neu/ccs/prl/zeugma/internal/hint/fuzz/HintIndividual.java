package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.ObjectIntMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class HintIndividual extends Individual {
    private SimpleList<StringHint> localHints = null;

    public HintIndividual(ByteList input) {
        super(input);
    }

    public SimpleList<StringHint> getLocalHints() {
        if (localHints == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " instance has not been initialized.");
        }
        return localHints;
    }

    @Override
    public void initialize(FuzzTarget target) {
        this.localHints = toList(new HintDeriver(target).derive(getInput()));
    }

    private static <T> SimpleList<T> toList(SimpleSet<? extends T> set) {
        SimpleList<T> result = new SimpleList<>();
        Iterator<? extends ObjectIntMap.Entry<? extends T>> itr = set.getBackingMap().entryIterator();
        while (itr.hasNext()) {
            result.add(itr.next().getKey());
        }
        return result;
    }
}
