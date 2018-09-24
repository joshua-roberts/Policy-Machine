package gov.nist.csd.pm.pip.dao;


import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.util.HashSet;

public interface AssociationsDAO {

    void createAssociation(long uaID, long targetID, HashSet<String> operations) throws DatabaseException;

    void updateAssociation(long uaID, long targetID, HashSet<String> ops) throws DatabaseException;

    void deleteAssociation(long uaID, long targetID) throws DatabaseException;
}
