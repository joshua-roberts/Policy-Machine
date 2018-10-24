package gov.nist.csd.pm.pip.prohibitions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;

import java.util.HashSet;

public interface ProhibitionsDAO {

    void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException;

    void deleteProhibition(String prohibitionName) throws DatabaseException;

    void addResourceToProhibition(String prohibitionName, long resourceID, boolean compliment) throws DatabaseException;

    void deleteProhibitionResource(String prohibitionName, long resourceID) throws DatabaseException;

    void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException;

    void setProhibitionSubject(String prohibitionName, long subjectID, ProhibitionSubjectType subjectType) throws DatabaseException;

    void setProhibitionOperations(String prohibitionName, HashSet<String> ops) throws DatabaseException;
}
