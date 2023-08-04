package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;


import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ModifierUtil;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.provider.PrimitiveWriter;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public final class StringHint {
    private final Interval source;
    private final String target;

    public StringHint(Interval source, String target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        } else if (source.getStart() < 0) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.target = target;
    }

    public Interval getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return String.format("Gen<String> -> %s", target);
    }

    private static ByteList createReplacements(String target) {
        ByteList replacements = new ByteArrayList();
        // Flag to indicate that an unconstrained String should be generated
        PrimitiveWriter.write(replacements, GenerateEventBroker.FLAG);
        byte[] values = target.getBytes();
        PrimitiveWriter.write(replacements, values.length);
        replacements.addAll(values);
        return replacements;
    }

    public boolean isValid(ByteList parent) {
        return source.getEnd() <= parent.size();
    }

    public ByteList apply(ByteList parent) {
        return ModifierUtil.replaceRange(parent, source.getStart(), source.size(), createReplacements(target));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof StringHint)) {
            return false;
        }
        StringHint that = (StringHint) o;
        if (!source.equals(that.source)) {
            return false;
        }
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }
}
