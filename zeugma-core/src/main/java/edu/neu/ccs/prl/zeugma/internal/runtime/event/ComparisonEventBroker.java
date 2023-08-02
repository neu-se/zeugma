package edu.neu.ccs.prl.zeugma.internal.runtime.event;

/**
 * See {@link CoverageEventBroker} for comments on event suppressing.
 * Calls to these methods are added via instrumentation.
 */
@SuppressWarnings("unused")
public final class ComparisonEventBroker {
    private static volatile ComparisonEventSubscriber subscriber = null;

    private ComparisonEventBroker() {
        throw new AssertionError();
    }

    public static void setSubscriber(ComparisonEventSubscriber subscriber) {
        // Ensure that ThreadFieldAccessor is loaded
        if (ThreadFieldAccessor.reserve()) {
            ThreadFieldAccessor.free();
        }
        ComparisonEventBroker.subscriber = subscriber;
    }

    public static void equals(String a0, Object a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equal(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void contentEquals(String a0, StringBuffer a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.contentEquals(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void contentEquals(String a0, CharSequence a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.contentEquals(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void equalsIgnoreCase(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equalIgnoreCase(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void compareTo(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.compareTo(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void compareToIgnoreCase(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.compareToIgnoreCase(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void regionMatches(String a0, int a1, String a2, int a3, int a4) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.regionMatches(a0, a1, a2, a3, a4);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void regionMatches(String a0, boolean a1, int a2, String a3, int a4, int a5) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.regionMatches(a0, a1, a2, a3, a4, a5);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void startsWith(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.startsWith(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void startsWith(String a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.startsWith(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void endsWith(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.endsWith(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(String a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(String a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void contains(String a0, CharSequence a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.contains(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void replace(String a0, CharSequence a1, CharSequence a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.replace(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(StringBuilder a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(StringBuilder a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(StringBuilder a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(StringBuilder a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(StringBuffer a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void indexOf(StringBuffer a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.indexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(StringBuffer a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    public static void lastIndexOf(StringBuffer a0, String a1, int a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.lastIndexOf(a0, a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }
}
