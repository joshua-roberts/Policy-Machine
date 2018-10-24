package gov.nist.csd.pm.pip.model;

import gov.nist.csd.pm.pip.graph.NGACMem;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.model.graph.Search;

public class NGACBackend {
    private NGAC    ngacDB;
    private NGAC    ngacMem;
    private Search  search;

    public NGACBackend(NGAC ngacDB, NGACMem ngacMem, Search search) {
        this.ngacDB = ngacDB;
        this.ngacMem = ngacMem;
        this.search = search;
    }

    public NGAC getDB() {
        return ngacDB;
    }

    public NGAC getNGACMem() {
        return ngacMem;
    }

    public Search getSearch() {
        return search;
    }
}
