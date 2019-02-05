package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.ProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.SQLProhibitionsLoader;

import java.util.List;

public class ProhibitionsPAP implements ProhibitionsDAO {
    private ProhibitionsDAO dbProhibitions;
    private MemProhibitionsDAO memProhibitions;

    public ProhibitionsPAP(DatabaseContext ctx) throws PMException {
        ProhibitionsLoader loader;
        if(ctx.getDatabase().equals("neo4j")) {
            dbProhibitions = new Neo4jProhibitionsDAO(ctx);
            loader = new Neo4jProhibitionsLoader(ctx);
        } else {
            dbProhibitions = new SQLProhibitionsDAO(ctx);
            loader = new SQLProhibitionsLoader(ctx);
        }
        memProhibitions = new MemProhibitionsDAO(loader);
    }

    @Override
    public void createProhibition(Prohibition prohibition) throws PMException {
        dbProhibitions.createProhibition(prohibition);
        memProhibitions.createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() throws PMException {
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
