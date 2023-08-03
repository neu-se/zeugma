package edu.neu.ccs.prl.zeugma.internal.hint.runtime.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that calls to the annotated method should be added during instrumentation to publish information about
 * calls made to the method owned by the class with the internal name {@link Monitors#owner()}, with the name
 * {@link Monitors#name()}, the descriptor {@link Monitors#descriptor()}, that is static if and only if
 * {@link Monitors#isStatic()} is true.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Monitors {
    String owner();

    String name();

    String descriptor();

    boolean isStatic();
}