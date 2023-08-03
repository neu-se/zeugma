package edu.neu.ccs.prl.zeugma.internal.hint.event;

import java.util.Arrays;

import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventSubscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that all event types are published to the {@link ComparisonEventBroker} and sent to its
 * {@link ComparisonEventSubscriber}.
 */
class ComparisonEventITCase {
    @AfterEach
    void clearSubscriber() {
        ComparisonEventBroker.setSubscriber(null);
    }

    @Test
    void stringEquals() {
        Object expected1 = "Hello";
        Object expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void equals(String receiver, Object other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.equals(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringContentEquals() {
        String expected1 = "Hello";
        StringBuffer expected2 = new StringBuffer("World");
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void contentEquals(String receiver, StringBuffer other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.contentEquals(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringContentEquals2() {
        String expected1 = "Hello";
        CharSequence expected2 = new StringBuilder("World");
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void contentEquals(String receiver, CharSequence other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.contentEquals(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringEqualsIgnoreCase() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void equalsIgnoreCase(String receiver, String other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.equalsIgnoreCase(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringCompareTo() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void compareTo(String receiver, String other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.compareTo(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringCompareToIgnoreCase() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void compareToIgnoreCase(String receiver, String other) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.compareToIgnoreCase(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringRegionMatches() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void regionMatches(String receiver, int offset, String other, int otherOffset, int len) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.regionMatches(0, expected2, 0, 3);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringRegionMatches2() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void regionMatches(String receiver, boolean ignoreCase, int offset, String other, int otherOffset,
                                      int len) {
                actual[0] = receiver;
                actual[1] = other;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.regionMatches(true, 0, expected2, 0, 3);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringStartsWith() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void startsWith(String receiver, String prefix) {
                actual[0] = receiver;
                actual[1] = prefix;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.startsWith(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringStartsWith2() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void startsWith(String receiver, String prefix, int offset) {
                actual[0] = receiver;
                actual[1] = prefix;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.startsWith(expected2, 1);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringEndsWith() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void endsWith(String receiver, String suffix) {
                actual[0] = receiver;
                actual[1] = suffix;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.endsWith(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringIndexOf() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(String receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringIndexOf2() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(String receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2, 1);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringLastIndexOf() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(String receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringLastIndexOf2() {
        String expected1 = "Hello";
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(String receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2, 2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringContains() {
        String expected1 = "Hello";
        CharSequence expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void contains(String receiver, CharSequence target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.contains(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringReplace() {
        String expected1 = "Hello";
        CharSequence expected2 = "World";
        CharSequence expected3 = "!";
        final Object[] actual = {null, null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void replace(String receiver, CharSequence target, CharSequence replacement) {
                actual[0] = receiver;
                actual[1] = target;
                actual[2] = replacement;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.replace(expected2, expected3);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
        Assertions.assertSame(expected3, actual[2]);
    }

    @Test
    void stringBuilderIndexOf() {
        StringBuilder expected1 = new StringBuilder("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(StringBuilder receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBuilderIndexOf2() {
        StringBuilder expected1 = new StringBuilder("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(StringBuilder receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2, 2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBuilderLastIndexOf() {
        StringBuilder expected1 = new StringBuilder("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(StringBuilder receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBuilderLastIndexOf2() {
        StringBuilder expected1 = new StringBuilder("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(StringBuilder receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2, 2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBufferIndexOf() {
        StringBuffer expected1 = new StringBuffer("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(StringBuffer receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBufferIndexOf2() {
        StringBuffer expected1 = new StringBuffer("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void indexOf(StringBuffer receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.indexOf(expected2, 2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBufferLastIndexOf() {
        StringBuffer expected1 = new StringBuffer("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(StringBuffer receiver, String target) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @Test
    void stringBufferLastIndexOf2() {
        StringBuffer expected1 = new StringBuffer("Hello");
        String expected2 = "World";
        final Object[] actual = {null, null};
        ComparisonEventBroker.setSubscriber(new EmptyEventSubscriber() {
            @Override
            public void lastIndexOf(StringBuffer receiver, String target, int offset) {
                actual[0] = receiver;
                actual[1] = target;
            }
        });
        //noinspection ResultOfMethodCallIgnored
        expected1.lastIndexOf(expected2, 2);
        clearSubscriber();
        Assertions.assertSame(expected1, actual[0]);
        Assertions.assertSame(expected2, actual[1]);
    }

    @BeforeAll
    public static void loadClasses() {
        //noinspection ResultOfMethodCallIgnored
        Arrays.asList(String.class, StringBuilder.class, StringBuffer.class);
    }
}
