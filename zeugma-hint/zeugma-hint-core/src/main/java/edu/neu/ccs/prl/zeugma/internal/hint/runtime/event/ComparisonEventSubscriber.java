package edu.neu.ccs.prl.zeugma.internal.hint.runtime.event;

public interface ComparisonEventSubscriber {
    void equals(String receiver, Object other);

    void contentEquals(String receiver, StringBuffer other);

    void contentEquals(String receiver, CharSequence other);

    void equalsIgnoreCase(String receiver, String other);

    void compareTo(String receiver, String other);

    void compareToIgnoreCase(String receiver, String other);

    void regionMatches(String receiver, int offset, String other, int otherOffset, int len);

    void regionMatches(String receiver, boolean ignoreCase, int offset, String other, int otherOffset, int len);

    void startsWith(String receiver, String prefix);

    void startsWith(String receiver, String prefix, int offset);

    void endsWith(String receiver, String suffix);

    void contains(String receiver, CharSequence target);

    void replace(String receiver, CharSequence target, CharSequence replacement);

    void indexOf(String receiver, String target);

    void indexOf(String receiver, String target, int offset);

    void lastIndexOf(String receiver, String target);

    void lastIndexOf(String receiver, String target, int offset);

    void indexOf(StringBuilder receiver, String target);

    void indexOf(StringBuilder receiver, String target, int offset);

    void lastIndexOf(StringBuilder receiver, String target);

    void lastIndexOf(StringBuilder receiver, String target, int offset);

    void indexOf(StringBuffer receiver, String target);

    void indexOf(StringBuffer receiver, String target, int offset);

    void lastIndexOf(StringBuffer receiver, String target);

    void lastIndexOf(StringBuffer receiver, String target, int offset);

    void equals(int[] a1, int[] a2);

    void equals(char[] a1, char[] a2);

    void equals(byte[] a1, byte[] a2);
}
