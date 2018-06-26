package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;

import java.util.HashSet;

public interface ProhibitionsDAO {


    void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException;

    void deleteProhibition(String prohibitionName) throws DatabaseException;

    void addResourceToProhibition(String prohibitionName, long resourceId, boolean compliment) throws DatabaseException;

    void deleteProhibitionResource(String prohibitionName, long resourceId) throws DatabaseException;

    void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException;

    void setProhibitionSubject(String prohibitionName, long subjectId, ProhibitionSubjectType subjectType) throws DatabaseException;

    void setProhibitionOperations(String prohibitionName, HashSet<String> ops) throws DatabaseException;
}
