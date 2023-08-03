package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that instrumentation was applied to a class.
 */
@Retention(RetentionPolicy.CLASS)
public @interface ZeugmaHintInstrumented {
}