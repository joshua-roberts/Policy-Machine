package gov.nist.csd.pm.common.model.obligations;

public class EvrProcess {
    private long processId;
    private EvrFunction evrFunction;

    public EvrProcess() {

    }

    public EvrProcess(long processId) {
        this.processId = processId;
    }

    public long getProcessId() {
        return this.processId;
    }

    public EvrProcess(EvrFunction evrFunction) {
        this.evrFunction = evrFunction;
    }

    public boolean isFunction() {
        return evrFunction != null;
    }

    public EvrFunction getFunction() {
        return evrFunction;
    }
}
