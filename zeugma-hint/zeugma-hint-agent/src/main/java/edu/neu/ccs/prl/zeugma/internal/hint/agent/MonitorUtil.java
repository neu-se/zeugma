package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import java.io.IOException;
import java.io.InputStream;

import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaAgent;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.AnnotationVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassReader;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.tree.AnnotationNode;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.tree.ClassNode;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.tree.MethodNode;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.Monitors;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;

public final class MonitorUtil {
    private static final String MONITORS_DESC = Type.getDescriptor(Monitors.class);

    private MonitorUtil() {
        throw new AssertionError();
    }

    public static SimpleMap<MethodInfo, MethodInfo> createMonitorMap(Class<?>... classes) {
        SimpleMap<MethodInfo, MethodInfo> monitorMap = new SimpleMap<>();
        for (Class<?> clazz : classes) {
            ClassNode cn = getClassNode(clazz);
            for (MethodNode mn : cn.methods) {
                processMethodNode(cn.name, mn, monitorMap);
            }
        }
        return monitorMap;
    }

    private static ClassNode getClassNode(Class<?> clazz) {
        try {
            ClassNode cn = new ClassNode();
            String name = clazz.getName().replace('.', '/') + ".class";
            ClassLoader classLoader = clazz.getClassLoader();
            try (InputStream in = classLoader == null ? ClassLoader.getSystemResourceAsStream(name) :
                                  classLoader.getResourceAsStream(name)) {
                if (in == null) {
                    throw new IllegalStateException("Failed to read class: " + clazz);
                }
                new ClassReader(in).accept(cn, ClassReader.SKIP_CODE);
            }
            return cn;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read class: " + clazz, e);
        }
    }

    private static void processMethodNode(String className, MethodNode mn,
                                          SimpleMap<MethodInfo, MethodInfo> monitorMap) {
        if ((mn.access & Opcodes.ACC_STATIC) != 0 && mn.invisibleAnnotations != null) {
            for (AnnotationNode an : mn.invisibleAnnotations) {
                if (MONITORS_DESC.equals(an.desc)) {
                    MonitorRecordBuilder builder = new MonitorRecordBuilder(ZeugmaAgent.ASM_VERSION);
                    an.accept(builder);
                    MethodInfo record = builder.build();
                    monitorMap.put(record, new MethodInfo(className, mn.name, mn.desc, true));
                    return;
                }
            }
        }
    }

    private static class MonitorRecordBuilder extends AnnotationVisitor {
        private boolean isStatic;
        private String owner;
        private String name;
        private String descriptor;

        MonitorRecordBuilder(int api) {
            super(api);
        }

        @Override
        public void visit(String name, Object value) {
            if ("owner".equals(name)) {
                owner = (String) value;
            } else if ("name".equals(name)) {
                this.name = (String) value;
            } else if ("descriptor".equals(name)) {
                descriptor = (String) value;
            } else if ("isStatic".equals(name)) {
                isStatic = (Boolean) value;
            }
            super.visit(name, value);
        }

        MethodInfo build() {
            return new MethodInfo(owner, name, descriptor, isStatic);
        }
    }
}
