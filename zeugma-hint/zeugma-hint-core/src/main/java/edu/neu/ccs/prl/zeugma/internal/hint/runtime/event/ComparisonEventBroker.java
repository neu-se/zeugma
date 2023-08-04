package edu.neu.ccs.prl.zeugma.internal.hint.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.ThreadFieldAccessor;

/**
 * See {@link ThreadFieldAccessor} for comments on event suppressing.
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

    @Monitors(owner = "java/lang/String", name = "equals", descriptor = "(Ljava/lang/Object;)Z", isStatic = false)
    public static void equals(String a0, Object a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equals(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    @Monitors(owner = "java/lang/String", name = "contentEquals", descriptor = "(Ljava/lang/StringBuffer;)Z",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "contentEquals", descriptor = "(Ljava/lang/CharSequence;)Z",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "equalsIgnoreCase", descriptor = "(Ljava/lang/String;)Z",
              isStatic = false)
    public static void equalsIgnoreCase(String a0, String a1) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equalsIgnoreCase(a0, a1);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    @Monitors(owner = "java/lang/String", name = "compareTo", descriptor = "(Ljava/lang/String;)I", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "compareToIgnoreCase", descriptor = "(Ljava/lang/String;)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "regionMatches", descriptor = "(ILjava/lang/String;II)Z",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "regionMatches", descriptor = "(ZILjava/lang/String;II)Z",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "startsWith", descriptor = "(Ljava/lang/String;)Z", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "startsWith", descriptor = "(Ljava/lang/String;I)Z", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "endsWith", descriptor = "(Ljava/lang/String;)Z", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "indexOf", descriptor = "(Ljava/lang/String;)I", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "indexOf", descriptor = "(Ljava/lang/String;I)I", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "lastIndexOf", descriptor = "(Ljava/lang/String;)I", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "lastIndexOf", descriptor = "(Ljava/lang/String;I)I", isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "contains", descriptor = "(Ljava/lang/CharSequence;)Z",
              isStatic = false)
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

    @Monitors(owner = "java/lang/String", name = "replace",
              descriptor = "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuilder", name = "indexOf", descriptor = "(Ljava/lang/String;)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuilder", name = "indexOf", descriptor = "(Ljava/lang/String;I)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuilder", name = "lastIndexOf", descriptor = "(Ljava/lang/String;)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuilder", name = "lastIndexOf", descriptor = "(Ljava/lang/String;I)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuffer", name = "indexOf", descriptor = "(Ljava/lang/String;)I",
              isStatic = false)

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

    @Monitors(owner = "java/lang/StringBuffer", name = "indexOf", descriptor = "(Ljava/lang/String;I)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuffer", name = "lastIndexOf", descriptor = "(Ljava/lang/String;)I",
              isStatic = false)
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

    @Monitors(owner = "java/lang/StringBuffer", name = "lastIndexOf", descriptor = "(Ljava/lang/String;I)I",
              isStatic = false)
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

    @Monitors(owner = "java/util/Arrays", name = "equals", descriptor = "([B[B)Z", isStatic = true)
    public static void equal(byte[] a1, byte[] a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equals(a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    @Monitors(owner = "java/util/Arrays", name = "equals", descriptor = "([C[C)Z", isStatic = true)
    public static void equal(char[] a1, char[] a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equals(a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    @Monitors(owner = "java/util/Arrays", name = "equals", descriptor = "([I[I)Z", isStatic = true)
    public static void equal(int[] a1, int[] a2) {
        ComparisonEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.equals(a1, a2);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }
}
