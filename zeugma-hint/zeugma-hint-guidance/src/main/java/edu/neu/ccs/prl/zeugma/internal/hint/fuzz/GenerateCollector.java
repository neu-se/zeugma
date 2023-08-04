package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

class GenerateCollector implements GenerateEventSubscriber {
    private final ByteList input;
    private final SimpleMap<Object, SimpleList<Interval>> generatedSourceMap = new SimpleMap<>();

    GenerateCollector(ByteList input) {
        this.input = UnmodifiableByteList.of(input);
    }

    public synchronized SimpleList<Interval> getSources(Object o) {
        return generatedSourceMap.get(o);
    }

    @Override
    public int starting() {
        return input.size();
    }

    @Override
    public void finished(int start, Object generated) {
        int end = input.size();
        if (start != end) {
            synchronized (this) {
                if (!generatedSourceMap.containsKey(generated)) {
                    generatedSourceMap.put(generated, new SimpleList<>());
                }
                generatedSourceMap.get(generated).add(new Interval(start, end));
            }
        }
    }
}