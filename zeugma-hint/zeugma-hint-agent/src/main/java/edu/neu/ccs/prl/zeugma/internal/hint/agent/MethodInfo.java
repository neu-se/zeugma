package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;

public final class MethodInfo {
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isStatic;

    public MethodInfo(String owner, String name, String descriptor, boolean isStatic) {
        if (owner == null || name == null || descriptor == null) {
            throw new NullPointerException();
        }
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isStatic = isStatic;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo that = (MethodInfo) o;
        if (isStatic != that.isStatic) {
            return false;
        }
        if (!owner.equals(that.owner)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        return descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + descriptor.hashCode();
        result = 31 * result + (isStatic ? 1 : 0);
        return result;
    }

    public Type[] getReceiverAndArgumentTypes() {
        Type[] args = Type.getArgumentTypes(descriptor);
        if (!isStatic) {
            Type[] temp = new Type[args.length + 1];
            temp[0] = Type.getObjectType(owner);
            System.arraycopy(args, 0, temp, 1, args.length);
            args = temp;
        }
        return args;
    }
}
