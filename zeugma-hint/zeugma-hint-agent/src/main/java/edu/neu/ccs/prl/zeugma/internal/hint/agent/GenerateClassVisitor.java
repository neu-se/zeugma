package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_BRIDGE;

import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaAgent;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Label;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.MethodVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.commons.LocalVariablesSorter;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;

final class GenerateClassVisitor extends ClassVisitor {
    private static final String GENERATE_NAME = "generate";
    private static final String GENERATE_DESC = "(Lcom/pholser/junit/quickcheck/random/SourceOfRandomness;" + "Lcom" +
                                                "/pholser/junit/quickcheck/generator/GenerationStatus;)" + "Ljava" +
                                                "/lang/String;";

    GenerateClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((access & ACC_BRIDGE) == 0 &&
            (access & ACC_ABSTRACT) == 0 &&
            GENERATE_NAME.equals(name) &&
            GENERATE_DESC.equals(desc)) {
            return new GenerateMethodVisitor(api, mv, access, desc);
        }
        return mv;
    }

    private static final class GenerateMethodVisitor extends LocalVariablesSorter {
        /**
         * Label marking the jump target if the {@link GenerateEventBroker#checkFlag} returns true.
         */
        private final Label jumpTarget = new Label();
        /**
         * Label marking the end of the added local variable's scope.
         */
        private final Label varEnd = new Label();
        private int var;

        GenerateMethodVisitor(int api, MethodVisitor mv, int access, String desc) {
            super(api, access, desc, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            Label varStart = new Label();
            super.visitLabel(varStart);
            var = newLocal(Type.INT_TYPE);
            super.visitLocalVariable(ZeugmaAgent.ADDED_MEMBER_PREFIX + "start", "I", null,
                                     varStart, varEnd, var);
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "starting",
                "()I",
                false);
            mv.visitVarInsn(Opcodes.ISTORE, var);
            // Check if generation should be delegated to the broker
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/pholser/junit/quickcheck/random/SourceOfRandomness",
                "toJDKRandom",
                "()Ljava/util/Random;",
                false);
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "checkFlag",
                "(Ljava/util/Random;)Z",
                false);
            super.visitJumpInsn(Opcodes.IFNE, jumpTarget);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ARETURN) {
                super.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ILOAD, var);
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(GenerateEventBroker.class),
                    "finished",
                    "(Ljava/lang/Object;I)V",
                    false);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitLabel(jumpTarget);
            super.visitFrame(
                Opcodes.F_NEW,
                2,
                new Object[] {Opcodes.TOP, "com/pholser/junit/quickcheck/random/SourceOfRandomness"},
                0,
                new Object[0]);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/pholser/junit/quickcheck/random/SourceOfRandomness",
                "toJDKRandom",
                "()Ljava/util/Random;",
                false);
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "produceString",
                "(Ljava/util/Random;)Ljava/lang/String;",
                false);
            visitInsn(Opcodes.ARETURN);
            super.visitLabel(varEnd);
            super.visitMaxs(maxStack, maxLocals);
        }

        public void hasFrames(boolean hasFrames) {
        }
    }
}
