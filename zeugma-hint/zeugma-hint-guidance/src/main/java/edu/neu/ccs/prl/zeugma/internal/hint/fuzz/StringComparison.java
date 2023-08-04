package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

public final class StringComparison {
    private final String operand1;
    private final String operand2;

    public StringComparison(String operand1, String operand2) {
        if (operand1 == null || operand2 == null) {
            throw new NullPointerException();
        }
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof StringComparison)) {
            return false;
        }
        StringComparison that = (StringComparison) o;
        if (!operand1.equals(that.operand1)) {
            return false;
        }
        return operand2.equals(that.operand2);
    }

    @Override
    public int hashCode() {
        int result = operand1.hashCode();
        result = 31 * result + operand2.hashCode();
        return result;
    }

    public String getOperand1() {
        return operand1;
    }

    public String getOperand2() {
        return operand2;
    }
}

