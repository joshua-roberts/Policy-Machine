package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.loader.prohibitions.ProhibitionsLoader;

import java.util.List;

/**
 * An in memory implementation of the ProhibitionsDAO interface, that stores prohibitions in a list.
 */
public class MemProhibitionsDAO implements ProhibitionsDAO {

    /**
     * Data structure to store prohibitions.
     */
    public List<Prohibition> prohibitions;

    /**
     * Create a new in-memory prohibitions DAO.  The provided loader will load ny prohibitions from a database.
     * @param loader The ProhibitionsLoader to load any existing prohibitions form a database into memory.
     * @throws DatabaseException If there is an error loading the Prohibitions into memory.
     * @throws InvalidProhibitionSubjectTypeException If any of the prohibitions in the database have invalid subject types.
     */
    public MemProhibitionsDAO(ProhibitionsLoader loader) throws DatabaseException, InvalidProhibitionSubjectTypeException {
        prohibitions = loader.loadProhibitions();
    }

    @Override
    public void createProhibition(Prohibition prohibition) {
        prohibitions.add(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() {
        return prohibitions;
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) {
        for(Prohibition prohibition : prohibitions) {
            if(prohibition.getName().equals(prohibitionName)) {
                return prohibition;
            }
        }
        return null;
    }

    @Override
    public void updateProhibition(Prohibition prohibition) {
        for(int i = 0; i < prohibitions.size(); i++) {
            Prohibition p = prohibitions.get(i);
            if(p.getName().equals(prohibition.getName())) {
                prohibitions.set(i, prohibition);
            }
        }
    }

    @Override
    public void deleteProhibition(String prohibitionName) {
        //find the prohibition with the given name and remove it from the list
        prohibitions.removeIf((prohibition) -> prohibition.getName().equals(prohibitionName));
    }
}
