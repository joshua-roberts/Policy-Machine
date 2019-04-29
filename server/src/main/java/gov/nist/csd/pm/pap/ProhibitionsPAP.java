package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pip.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pip.loader.prohibitions.ProhibitionsLoader;
import gov.nist.csd.pm.pip.prohibitions.Neo4jProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.MemProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.model.Prohibition;

import java.util.Iterator;
import java.util.List;

public class ProhibitionsPAP implements ProhibitionsDAO {
    private ProhibitionsDAO    dbProhibitions;
    private MemProhibitionsDAO memProhibitions;

    public ProhibitionsPAP(MemProhibitionsDAO memProhibitions, Neo4jProhibitionsDAO dbProhibitions) throws PMException {
        this.memProhibitions = memProhibitions;
        this.dbProhibitions = dbProhibitions;
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

    public void reset() throws PMException {
        List<Prohibition> prohibitions = memProhibitions.getProhibitions();
        for (Iterator<Prohibition> iterator = prohibitions.iterator(); iterator.hasNext();) {
            Prohibition p = iterator.next();
            dbProhibitions.deleteProhibition(p.getName());
            iterator.remove();
        }
    }
}
