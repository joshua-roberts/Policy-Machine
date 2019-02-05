package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.PMException;
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
     * @throws PMException If there is an error loading the Prohibitions into memory.
     */
    public MemProhibitionsDAO(ProhibitionsLoader loader) throws PMException {
        prohibitions = loader.loadProhibitions();
    }

    /**
     * Add the provided prohibition to the list of prohibitions.
     * @param prohibition The prohibition to be created.
     */
    @Override
    public void createProhibition(Prohibition prohibition) {
        prohibitions.add(prohibition);
    }

    /**
     * @return The list of prohibition objects.
     */
    @Override
    public List<Prohibition> getProhibitions() {
        return prohibitions;
    }

    /**
     * @param prohibitionName The name of the Prohibition to retrieve.
     * @return The prohibition with the given name.  If one does not exist, return null.
     */
    @Override
    public Prohibition getProhibition(String prohibitionName) {
        for(Prohibition prohibition : prohibitions) {
            if(prohibition.getName().equals(prohibitionName)) {
                return prohibition;
            }
        }
        return null;
    }

    /**
     * Update an existing prohibition with the same name as the provided prohibition.
     * @param prohibition The prohibition to update.
     */
    @Override
    public void updateProhibition(Prohibition prohibition) {
        for(int i = 0; i < prohibitions.size(); i++) {
            Prohibition p = prohibitions.get(i);
            if(p.getName().equals(prohibition.getName())) {
                prohibitions.set(i, prohibition);
            }
        }
    }

    /**
     * Remove the prohibition with the given name from the list.
     * @param prohibitionName The name of the prohibition to delete.
     */
    @Override
    public void deleteProhibition(String prohibitionName) {
        //find the prohibition with the given name and remove it from the list
        prohibitions.removeIf((prohibition) -> prohibition.getName().equals(prohibitionName));
    }
}
