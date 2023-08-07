package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaAgent;
import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaTransformer;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassReader;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassTooLargeException;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassWriter;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.MethodTooLargeException;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.tree.AnnotationNode;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.tree.ClassNode;

public final class ZeugmaHintTransformer implements ClassFileTransformer {
    private static final String ANNOTATION_DESC = Type.getDescriptor(ZeugmaHintInstrumented.class);
    private static final boolean isAnalysis = Boolean.getBoolean("zeugma.hint.analysis");
    private final ZeugmaTransformer delegate = new ZeugmaTransformer();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        try {
            if (isAnalysis) {
                // Skip base Zeugma instrumentation
                return transform(classFileBuffer);
            } else {
                byte[] instrumented = delegate.transform(
                    loader,
                    className,
                    classBeingRedefined,
                    protectionDomain,
                    classFileBuffer);
                return instrumented == null ? transform(classFileBuffer) : transform(instrumented);
            }
        } catch (Throwable t) {
            // Print the stack trace for the error to prevent it from being silently swallowed by the JVM
            t.printStackTrace();
            throw t;
        }
    }

    public byte[] transform(byte[] classFileBuffer) {
        try {
            ClassReader cr = new ClassReader(classFileBuffer);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.EXPAND_FRAMES);
            if (isAnnotated(cn)) {
                // This class has already been instrumented; return null to indicate that the class was unchanged
                return null;
            }
            // Add an annotation indicating that the class has been instrumented
            cn.visitAnnotation(ANNOTATION_DESC, false);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new GenerateClassVisitor(ZeugmaAgent.ASM_VERSION, cw);
            if (!isAnalysis) {
                cv = new MonitorEventClassVisitor(ZeugmaAgent.ASM_VERSION, cv);
            }
            cn.accept(cv);
            return cw.toByteArray();
        } catch (ClassTooLargeException | MethodTooLargeException e) {
            return null;
        }
    }

    private static boolean isAnnotated(ClassNode cn) {
        if (cn.invisibleAnnotations != null) {
            for (AnnotationNode a : cn.invisibleAnnotations) {
                if (ANNOTATION_DESC.equals(a.desc)) {
                    return true;
                }
            }
        }
        return false;
    }
}
