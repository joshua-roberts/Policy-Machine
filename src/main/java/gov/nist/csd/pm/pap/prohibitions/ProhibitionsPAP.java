package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMProhibitionException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.ProhibitionsLoader;

import java.util.List;

public class ProhibitionsPAP implements ProhibitionsDAO {
    private ProhibitionsDAO dbProhibitions;
    private MemProhibitionsDAO memProhibitions;

    public ProhibitionsPAP(DatabaseContext ctx) throws PMDBException, PMProhibitionException {
        dbProhibitions = new Neo4jProhibitionsDAO(ctx);
        ProhibitionsLoader loader = new Neo4jProhibitionsLoader(ctx);
        memProhibitions = new MemProhibitionsDAO(loader);
    }

    @Override
    public void createProhibition(Prohibition prohibition) throws PMDBException {
        dbProhibitions.createProhibition(prohibition);
        memProhibitions.createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() {
        return memProhibitions.getProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) {
        return memProhibitions.getProhibition(prohibitionName);
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws PMDBException {
        dbProhibitions.updateProhibition(prohibition);
        memProhibitions.updateProhibition(prohibition);
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws PMDBException {
        dbProhibitions.deleteProhibition(prohibitionName);
        memProhibitions.deleteProhibition(prohibitionName);
    }
}
