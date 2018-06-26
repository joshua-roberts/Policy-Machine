package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;

import java.util.HashSet;

public interface AssociationsDAO {
    void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException;

    void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> ops) throws DatabaseException;

    void deleteAssociation(long uaId, long targetId) throws DatabaseException;
}
