package edu.neu.ccs.prl.zeugma.internal.runtime.event;

public final class ThreadFieldAccessor {
    static {
        // Ensure that used classes are loaded
        new ThreadState();
    }

    private ThreadFieldAccessor() {
        throw new AssertionError();
    }

    @SuppressWarnings("unused")
    private static Object get(Thread thread) {
        throw new AssertionError("Called un-instrumented method body");
    }

    @SuppressWarnings("unused")
    private static void set(Thread thread, Object value) {
        throw new AssertionError("Called un-instrumented method body");
    }

    private static ThreadState getCurrentState() {
        Thread current = Thread.currentThread();
        ThreadState state = (ThreadState) get(current);
        if (state == null) {
            state = new ThreadState();
            set(current, state);
        }
        return state;
    }

    static void free() {
        getCurrentState().reserved = false;
    }

    static boolean reserve() {
        ThreadState state = getCurrentState();
        if (!state.reserved) {
            // Prevent re-entry on the same Thread
            state.reserved = true;
            return true;
        } else {
            return false;
        }
    }

    public static final class ThreadState {
        private boolean reserved = false;
    }
}