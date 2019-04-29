package gov.nist.csd.pm.common.model.obligations;

import gov.nist.csd.pm.common.model.obligations.functions.Function;

public class EvrProcess {
    private String   value;
    private Function function;

    public EvrProcess(String process) {
        this.value = process;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public boolean equals(Object o) {
        if(!(o instanceof EvrProcess)) {
            return false;
        }

        EvrProcess p = (EvrProcess)o;

        if(this.value == null) {
            return false;
        }

        return this.value.equals(p.value);
    }
}
