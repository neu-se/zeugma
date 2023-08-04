package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.ObjectIntMap;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

class ComparisonRecorder implements ComparisonEventSubscriber {
    private final ByteList input;
    private final ObjectIntMap<StringComparison> comparisons = new ObjectIntMap<>();

    ComparisonRecorder(ByteList input) {
        this.input = UnmodifiableByteList.of(input);
    }

    @Override
    public void equals(int[] operand1, int[] operand2) {
        if (!input.isEmpty() && operand1 != null && operand2 != null) {
            forceEqual(new String(operand1, 0, operand1.length), new String(operand2, 0, operand2.length));
        }
    }

    @Override
    public void equals(char[] operand1, char[] operand2) {
        if (!input.isEmpty() && operand1 != null && operand2 != null) {
            forceEqual(new String(operand1), new String(operand2));
        }
    }

    @Override
    public void equals(byte[] operand1, byte[] operand2) {
        if (!input.isEmpty() && operand1 != null && operand2 != null) {
            forceEqual(new String(operand1), new String(operand2));
        }
    }

    @Override
    public void equals(String receiver, Object other) {
        if (other instanceof String) {
            forceEqual(receiver, (String) other);
        }
    }

    @Override
    public void contentEquals(String receiver, StringBuffer other) {
        if (other != null) {
            forceEqual(receiver, other.toString());
        }
    }

    @Override
    public void contentEquals(String receiver, CharSequence other) {
        if (other != null) {
            forceEqual(receiver, other.toString());
        }
    }

    @Override
    public void equalsIgnoreCase(String receiver, String other) {
        if (other != null) {
            forceEqual(receiver, other);
        }
    }

    @Override
    public void compareTo(String receiver, String other) {
        if (other != null) {
            forceEqual(receiver, other);
        }
    }

    @Override
    public void compareToIgnoreCase(String receiver, String other) {
        if (other != null) {
            forceEqual(receiver, other);
        }
    }

    @Override
    public void regionMatches(String receiver, int offset, String other, int otherOffset, int len) {
        regionMatchesInternal(receiver, offset, other, otherOffset, len);
    }

    @Override
    public void regionMatches(String receiver, boolean ignoreCase, int offset, String other, int otherOffset, int len) {
        regionMatchesInternal(receiver, offset, other, otherOffset, len);
    }

    @Override
    public void startsWith(String receiver, String prefix) {
        if (prefix != null) {
            String s = receiver;
            if (prefix.length() < receiver.length()) {
                s = receiver.substring(0, prefix.length());
            }
            forceEqual(s, prefix);
        }
    }

    @Override
    public void startsWith(String receiver, String prefix, int offset) {
        if (prefix != null && offset >= 0 && offset <= receiver.length()) {
            String s;
            if (offset + prefix.length() < receiver.length()) {
                s = receiver.substring(offset, offset + prefix.length());
            } else {
                s = receiver.substring(offset);
            }
            forceEqual(s, prefix);
        }
    }

    @Override
    public void endsWith(String receiver, String suffix) {
        if (suffix != null) {
            String s = receiver;
            if (suffix.length() < receiver.length()) {
                s = receiver.substring(receiver.length() - suffix.length());
            }
            forceEqual(s, suffix);
        }
    }

    @Override
    public void contains(String receiver, CharSequence target) {
        if (target != null) {
            forceContains(receiver, target.toString());
        }
    }

    @Override
    public void replace(String receiver, CharSequence target, CharSequence replacement) {
        if (target != null) {
            forceContains(receiver, target.toString());
        }
    }

    @Override
    public void indexOf(String receiver, String target) {
        indexOfInternal(receiver, target);
    }

    @Override
    public void indexOf(String receiver, String target, int offset) {
        indexOfInternal(receiver, target, offset);
    }

    @Override
    public void lastIndexOf(String receiver, String target) {
        lastIndexOfInternal(receiver, target);
    }

    @Override
    public void lastIndexOf(String receiver, String target, int offset) {
        lastIndexOfInternal(receiver, target, offset);
    }

    @Override
    public void indexOf(StringBuilder receiver, String target) {
        indexOfInternal(receiver.toString(), target);
    }

    @Override
    public void indexOf(StringBuilder receiver, String target, int offset) {
        indexOfInternal(receiver.toString(), target, offset);
    }

    @Override
    public void lastIndexOf(StringBuilder receiver, String target) {
        lastIndexOfInternal(receiver.toString(), target);
    }

    @Override
    public void lastIndexOf(StringBuilder receiver, String target, int offset) {
        lastIndexOfInternal(receiver.toString(), target, offset);
    }

    @Override
    public void indexOf(StringBuffer receiver, String target) {
        indexOfInternal(receiver.toString(), target);
    }

    @Override
    public void indexOf(StringBuffer receiver, String target, int offset) {
        indexOfInternal(receiver.toString(), target, offset);
    }

    @Override
    public void lastIndexOf(StringBuffer receiver, String target) {
        lastIndexOfInternal(receiver.toString(), target);
    }

    @Override
    public void lastIndexOf(StringBuffer receiver, String target, int offset) {
        lastIndexOfInternal(receiver.toString(), target, offset);
    }

    private void regionMatchesInternal(String receiver, int offset, String other, int otherOffset, int len) {
        if (other != null && len > 0 && offset >= 0 && otherOffset >= 0 && offset <= receiver.length()
            && otherOffset <= other.length()) {
            int end1 = Math.min(offset + len, receiver.length());
            int end2 = Math.min(otherOffset + len, other.length());
            forceEqual(receiver.substring(offset, end1), other.substring(otherOffset, end2));
        }
    }

    private void indexOfInternal(String receiver, String target) {
        if (target != null) {
            forceContains(receiver, target);
        }
    }

    private void indexOfInternal(String receiver, String target, int offset) {
        if (target != null && offset >= 0 && offset <= receiver.length()) {
            forceContains(receiver.substring(offset), target);
        }
    }

    private void lastIndexOfInternal(String receiver, String target) {
        if (target != null) {
            forceContains(receiver, target);
        }
    }

    private void lastIndexOfInternal(String receiver, String target, int offset) {
        if (target != null && offset >= 0) {
            String s = receiver;
            if (offset + 1 < receiver.length()) {
                s = receiver.substring(0, offset + 1);
            }
            forceContains(s, target);
        }
    }

    private void forceContains(String s, String target) {
        forceEqual(s, target);
    }

    private synchronized void forceEqual(String s1, String s2) {
        if (!input.isEmpty() && s1 != null && s2 != null && !s2.equals(s1)) {
            comparisons.put(new StringComparison(s1, s2), 0);
        }
    }

    public synchronized void accept(HintDeriver creator) {
        Iterator<ObjectIntMap.Entry<StringComparison>> itr = comparisons.entryIterator();
        while (itr.hasNext()) {
            creator.visit(itr.next().getKey());
        }
    }
}

