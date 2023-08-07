package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ModifierUtil;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

public class DerivedMutator {
    private final Interval source;
    private final ByteList replacements;

    public DerivedMutator(Interval source, ByteList replacements) {
        if (source.getStart() < 0) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.replacements = UnmodifiableByteList.of(new ByteArrayList(replacements));
    }

    public Interval getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", source, replacements);
    }

    public boolean isValid(ByteList parent) {
        return source.getEnd() <= parent.size();
    }

    public ByteList apply(ByteList parent) {
        return ModifierUtil.replaceRange(parent, source.getStart(), source.size(), replacements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof DerivedMutator)) {
            return false;
        }
        DerivedMutator that = (DerivedMutator) o;
        if (!source.equals(that.source)) {
            return false;
        }
        return replacements.equals(that.replacements);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + replacements.hashCode();
        return result;
    }

    public static ByteList apply(DerivedMutator[] mutators, ByteList values) {
        // Apply the mutators in reverse-order by position; mutators targeting earlier positions can shift the start of
        // mutator that target later positions
        java.util.Arrays.sort(mutators, (m1, m2) -> {
            int s1 = m1.getSource().getStart();
            int s2 = m2.getSource().getStart();
            //noinspection UseCompareMethod
            return (s1 < s2) ? 1 : ((s1 == s2) ? 0 : -1);
        });
        DerivedMutator last = null;
        for (DerivedMutator mutator : mutators) {
            // Check that mutator targets a range before the range targeted by the last mutator
            if (mutator.isValid(values)
                && (last == null || mutator.getSource().getEnd() <= last.getSource().getStart())) {
                last = mutator;
                values = mutator.apply(values);
            }
        }
        return values;
    }
}
