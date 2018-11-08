package gov.nist.csd.pm.model.prohibitions;

import gov.nist.csd.pm.model.exceptions.*;

import java.util.List;

/**
 * Interface to maintain Prohibitions for an NGAC environment.
 */
public interface ProhibitionsDAO {
    /**
     * Create a new prohibition and add it to the stored map of prohibitions
     * @param prohibition The prohibition to be created.
     */
    void createProhibition(Prohibition prohibition) throws DatabaseException, NullNameException, ProhibitionNameExistsException, LoadConfigException, LoaderException, SessionDoesNotExistException, NodeNotFoundException, MissingPermissionException, InvalidProhibitionSubjectTypeException;

    /**
     * Get a list of all prohibitions
     * @return A list of all prohibitions
     */
    List<Prohibition> getProhibitions() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException;

    /**
     * Retrieve a Prohibition and return the Object representing it.
     * @param prohibitionName The name of the Prohibition to retrieve.
     * @return The Prohibition with the given name.
     */
    Prohibition getProhibition(String prohibitionName) throws ProhibitionDoesNotExistException, DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException;

    /**
     * Update the given prohibition.  The name of the prohibition is provided in the parameter.
     * @param prohibition The prohibition to update.
     */
    void updateProhibition(Prohibition prohibition) throws DatabaseException, LoadConfigException, InvalidProhibitionSubjectTypeException, LoaderException;

    /**
     * Delete the prohibition, and remove it from the data structure.
     * @param prohibitionName The name of the prohibition to delete.
     */
    void deleteProhibition(String prohibitionName) throws DatabaseException, LoadConfigException, InvalidProhibitionSubjectTypeException, LoaderException, ProhibitionDoesNotExistException;
}
