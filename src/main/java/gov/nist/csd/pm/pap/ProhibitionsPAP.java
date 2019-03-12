package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pip.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pip.loader.prohibitions.ProhibitionsLoader;
import gov.nist.csd.pm.pip.prohibitions.Neo4jProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.MemProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.model.Prohibition;

import java.util.List;

public class ProhibitionsPAP implements ProhibitionsDAO {
    private ProhibitionsDAO    dbProhibitions;
    private MemProhibitionsDAO memProhibitions;

    public ProhibitionsPAP(DatabaseContext ctx) throws PMException {
        dbProhibitions = new Neo4jProhibitionsDAO(ctx);
        ProhibitionsLoader loader = new Neo4jProhibitionsLoader(ctx);
        memProhibitions = new MemProhibitionsDAO();
        List<Prohibition> prohibitions = loader.loadProhibitions();
        for(Prohibition prohibition : prohibitions) {
            memProhibitions.createProhibition(prohibition);
        }
    }

    @Override
    public void createProhibition(Prohibition prohibition) throws PMException {
        dbProhibitions.createProhibition(prohibition);
        memProhibitions.createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() {
        return memProhibitions.getProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) throws PMException {
        return memProhibitions.getProhibition(prohibitionName);
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws PMException {
        dbProhibitions.updateProhibition(prohibition);
        memProhibitions.updateProhibition(prohibition);
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws PMException {
        dbProhibitions.deleteProhibition(prohibitionName);
        memProhibitions.deleteProhibition(prohibitionName);
    }
}
