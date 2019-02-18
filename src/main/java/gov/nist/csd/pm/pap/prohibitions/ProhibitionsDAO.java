package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;

import java.util.List;

/**
 * Interface to maintain Prohibitions for an NGAC environment. This interface is in the common package because the
 * Prohibition service in the PDP will also implement this interface as well as any implementations in the PAP.
 */
public interface ProhibitionsDAO {
    /**
     * Create a new prohibition.
     * @param prohibition The prohibition to be created.
     * @ if there is an error creating a prohibition.
     */
    void createProhibition(Prohibition prohibition) throws PMDBException;

    /**
     * Get a list of all prohibitions
     * @return a list of all prohibitions
     * @ if there is an error getting the prohibitions.
     */
    List<Prohibition> getProhibitions() throws PMDBException, PMProhibitionException;

    /**
     * Retrieve a Prohibition and return the Object representing it.
     * @param prohibitionName The name of the Prohibition to retrieve.
     * @return the Prohibition with the given name.
     * @ if there is an error getting the prohibition with the given name.
     */
    Prohibition getProhibition(String prohibitionName) throws PMDBException, PMProhibitionException;

    /**
     * Update the given prohibition.  The name of the prohibition is provided in the parameter.
     * @param prohibition The prohibition to update.
     * @ if there is an error updating the prohibition.
     */
    void updateProhibition(Prohibition prohibition) throws PMDBException;

    /**
     * Delete the prohibition, and remove it from the data structure.
     * @param prohibitionName The name of the prohibition to delete.
     * @ if there is an error deleting the prohibition.
     */
    void deleteProhibition(String prohibitionName) throws PMDBException;
}
