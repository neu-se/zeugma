package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_STATIC;
import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ILOAD;

import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.MethodVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;

/**
 * Adds code to publish monitor events.
 */
final class MonitorEventClassVisitor extends ClassVisitor {
    private static final SimpleMap<MethodInfo, MethodInfo> monitorMap = MonitorUtil.createMonitorMap(
        ComparisonEventBroker.class);
    /**
     * Name of the class being visited.
     */
    private String className;

    MonitorEventClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private void pushArguments(MethodInfo m, MethodVisitor mv) {
        int argumentIndex = 0;
        for (Type arg : m.getReceiverAndArgumentTypes()) {
            // Push the argument onto the stack
            mv.visitVarInsn(arg.getOpcode(ILOAD), argumentIndex);
            argumentIndex += arg.getSize();
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        MethodInfo key = new MethodInfo(className, name, desc, (access & ACC_STATIC) != 0);
        if ((access & ACC_ABSTRACT) == 0 && monitorMap.containsKey(key)) {
            mv = new MethodVisitor(api, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    pushArguments(key, mv);
                    MethodInfo value = monitorMap.get(key);
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        value.getOwner(),
                        value.getName(),
                        value.getDescriptor(),
                        false);
                }
            };
        }
        return mv;
    }
}
