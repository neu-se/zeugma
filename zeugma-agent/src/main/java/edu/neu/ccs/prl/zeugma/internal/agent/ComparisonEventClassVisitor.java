package edu.neu.ccs.prl.zeugma.internal.agent;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ILOAD;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.ComparisonMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Adds code to publish events to the {@link ComparisonEventBroker}.
 */
final class ComparisonEventClassVisitor extends ClassVisitor {

    /**
     * Name of the class being visited.
     */
    private String className;

    ComparisonEventClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private Type[] pushArguments(String desc, int access, MethodVisitor mv) {
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        int argumentIndex = 0;
        Type[] args = getReceiverAndArgumentTypes(className, desc, isStatic);
        for (Type arg : args) {
            // Push the argument onto the stack
            mv.visitVarInsn(arg.getOpcode(ILOAD), argumentIndex);
            argumentIndex += arg.getSize();
        }
        return args;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((access & ACC_ABSTRACT) == 0) {
            for (ComparisonMethod m : ComparisonMethod.values()) {
                if (m.matches(className, name, desc, (access & ACC_STATIC) != 0)) {
                    // Method is concrete and monitored
                    return new MethodVisitor(api, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            Type[] types = pushArguments(desc, access, mv);
                            mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ComparisonEventBroker.class),
                                name,
                                Type.getMethodDescriptor(Type.VOID_TYPE, types),
                                false);
                        }
                    };
                }
            }

        }
        return mv;
    }

    private static Type[] getReceiverAndArgumentTypes(String owner, String desc, boolean isStatic) {
        Type[] args = Type.getArgumentTypes(desc);
        if (!isStatic) {
            Type[] temp = new Type[args.length + 1];
            temp[0] = Type.getObjectType(owner);
            System.arraycopy(args, 0, temp, 1, args.length);
            args = temp;
        }
        return args;
    }
}
