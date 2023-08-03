package edu.neu.ccs.prl.zeugma.internal.hint.runtime.event;

public interface GenerateEventSubscriber {
    int starting();

    void finished(int start, Object generated);
}