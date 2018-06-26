package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;

import java.util.HashSet;

public class SqlAssociationsDAO implements AssociationsDAO {
    @Override
    public void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException {

    }

    @Override
    public void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> ops) throws DatabaseException {

    }

    @Override
    public void deleteAssociation(long uaId, long targetId) throws DatabaseException {

    }
}
