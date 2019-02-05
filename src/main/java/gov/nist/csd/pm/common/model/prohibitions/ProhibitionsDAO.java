package gov.nist.csd.pm.common.model.prohibitions;

import gov.nist.csd.pm.common.exceptions.*;

import java.util.List;

/**
 * Interface to maintain Prohibitions for an NGAC environment. This interface is in the common package because the
 * Prohibition service in the PDP will also implement this interface as well as any implementations in the PAP.
 */
public interface ProhibitionsDAO {
    /**
     * Create a new prohibition.
     * @param prohibition The prohibition to be created.
     */
    void createProhibition(Prohibition prohibition) throws PMException;

    /**
     * Get a list of all prohibitions
     * @return A list of all prohibitions
     */
    List<Prohibition> getProhibitions() throws PMException;

    /**
     * Retrieve a Prohibition and return the Object representing it.
     * @param prohibitionName The name of the Prohibition to retrieve.
     * @return The Prohibition with the given name.
     */
    Prohibition getProhibition(String prohibitionName) throws PMException;

    /**
     * Update the given prohibition.  The name of the prohibition is provided in the parameter.
     * @param prohibition The prohibition to update.
     */
    void updateProhibition(Prohibition prohibition) throws PMException;

    /**
     * Delete the prohibition, and remove it from the data structure.
     * @param prohibitionName The name of the prohibition to delete.
     */
    void deleteProhibition(String prohibitionName) throws PMException;
}
