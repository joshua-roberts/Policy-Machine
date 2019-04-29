package gov.nist.csd.pm.common.model.obligations;

import gov.nist.csd.pm.common.model.obligations.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class Response {

    private List<Action> actions;

    public Response() {
        this.actions = new ArrayList<>();
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }
}
