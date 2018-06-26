package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.ProhibitionsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;

import java.util.HashSet;

public class SqlProhibitionsDAO implements ProhibitionsDAO {
    @Override
    public void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException {

    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {

    }

    @Override
    public void addResourceToProhibition(String prohibitionName, long resourceId, boolean compliment) throws DatabaseException {

    }

    @Override
    public void deleteProhibitionResource(String prohibitionName, long resourceId) throws DatabaseException {

    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {

    }

    @Override
    public void setProhibitionSubject(String prohibitionName, long subjectId, ProhibitionSubjectType subjectType) throws DatabaseException {

    }

    @Override
    public void setProhibitionOperations(String prohibitionName, HashSet<String> ops) throws DatabaseException {

    }
}
