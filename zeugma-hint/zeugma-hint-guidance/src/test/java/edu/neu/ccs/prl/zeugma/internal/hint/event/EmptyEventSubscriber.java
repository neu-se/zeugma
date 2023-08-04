package edu.neu.ccs.prl.zeugma.internal.hint.event;

import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventSubscriber;

public class EmptyEventSubscriber implements ComparisonEventSubscriber, GenerateEventSubscriber {
    @Override
    public void equals(String receiver, Object other) {
    }

    @Override
    public void contentEquals(String receiver, StringBuffer other) {
    }

    @Override
    public void contentEquals(String receiver, CharSequence other) {
    }

    @Override
    public void equalsIgnoreCase(String receiver, String other) {
    }

    @Override
    public void compareTo(String receiver, String other) {
    }

    @Override
    public void compareToIgnoreCase(String receiver, String other) {
    }

    @Override
    public void regionMatches(String receiver, int offset, String other, int otherOffset, int len) {
    }

    @Override
    public void regionMatches(String receiver, boolean ignoreCase, int offset, String other, int otherOffset, int len) {
    }

    @Override
    public void startsWith(String receiver, String prefix) {
    }

    @Override
    public void startsWith(String receiver, String prefix, int offset) {
    }

    @Override
    public void endsWith(String receiver, String suffix) {
    }

    @Override
    public void contains(String receiver, CharSequence target) {
    }

    @Override
    public void replace(String receiver, CharSequence target, CharSequence replacement) {
    }

    @Override
    public void indexOf(String receiver, String target) {
    }

    @Override
    public void indexOf(String receiver, String target, int offset) {
    }

    @Override
    public void lastIndexOf(String receiver, String target) {
    }

    @Override
    public void lastIndexOf(String receiver, String target, int offset) {
    }

    @Override
    public void indexOf(StringBuilder receiver, String target) {

    }

    @Override
    public void indexOf(StringBuilder receiver, String target, int offset) {
    }

    @Override
    public void lastIndexOf(StringBuilder receiver, String target) {
    }

    @Override
    public void lastIndexOf(StringBuilder receiver, String target, int offset) {
    }

    @Override
    public void indexOf(StringBuffer receiver, String target) {
    }

    @Override
    public void indexOf(StringBuffer receiver, String target, int offset) {
    }

    @Override
    public void lastIndexOf(StringBuffer receiver, String target) {
    }

    @Override
    public void lastIndexOf(StringBuffer receiver, String target, int offset) {
    }

    @Override
    public void equals(int[] a1, int[] a2) {
    }

    @Override
    public void equals(char[] a1, char[] a2) {
    }

    @Override
    public void equals(byte[] a1, byte[] a2) {
    }

    @Override
    public int starting() {
        return -1;
    }

    @Override
    public void finished(int start, Object generated) {
    }
}