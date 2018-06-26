package gov.nist.policyserver.service;

import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ALL_OPERATIONS;
import static gov.nist.policyserver.common.Constants.ANY_OPERATIONS;

public class AnalyticsService extends Service{

    public List<PmAnalyticsEntry> getUsersPermissionsOn(long targetId)
            throws NodeNotFoundException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //check that the target node exists
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }

        return getAnalytics().getUsersWithAccessOn(target);
    }

    public PmAnalyticsEntry getUserPermissionsOn(long targetId, long userId)
            throws NodeNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //check that the target and user nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        Node user = getGraph().getNode(userId);
        if(user == null){
            throw new NodeNotFoundException(userId);
        }

        //get permitted ops for the subject on the target
        PmAnalyticsEntry userAccess = getAnalytics().getUserAccessOn(user, target);

        //get prohibited ops for the subject on the target
        HashSet<String> prohibitedOps = getProhibitedOps(target.getId(), user.getId(), ProhibitionSubjectType.U.toString());

        //remove prohibited ops
        userAccess.getOperations().removeAll(prohibitedOps);

        return userAccess;
    }

    public List<PmAnalyticsEntry> getAccessibleChildren(long targetId, long userId) throws NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //check that a user id is present
        if(userId == 0){
            throw new NoUserParameterException();
        }

        //check that the user and target nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        Node user = getGraph().getNode(userId);
        if(user == null){
            throw new NodeNotFoundException(userId);
        }

        return getAnalytics().getAccessibleChildrenOf(target, user);
    }

    public List<PmAnalyticsEntry> getAccessibleNodes(long userId) throws NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //check that the user id is present
        if(userId == 0){
            throw new NoUserParameterException();
        }

        //check that the user exists
        Node user = getGraph().getNode(userId);
        if(user == null){
            throw new NodeNotFoundException(userId);
        }

        //get the accessible nodes and add it to the cache

        return getAnalytics().getAccessibleNodes(user);
    }

    public List<PmAnalyticsEntry> getAccessibleNodes(Node user) throws ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //get the accessible nodes and add it to the cache

        return getAnalytics().getAccessibleNodes(user);
    }

    public HashSet<String> getProhibitedOps(long targetId, long subjectId, String subjectType) throws NodeNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        if(subjectId == 0){
            throw new NoSubjectParameterException();
        }

        //check if the subject type is valid
        ProhibitionSubjectType type = ProhibitionSubjectType.toProhibitionSubjectType(subjectType);

        //check that the user and target nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        if(type.equals(ProhibitionSubjectType.U)) {
            Node user = getGraph().getNode(subjectId);
            if (user == null) {
                throw new NodeNotFoundException(subjectId);
            }
        }

        return getAnalytics().getProhibitedOps(targetId, subjectId);
    }

    public void checkPermissions(Node user, long process, long targetId, String reqPerm) throws MissingPermissionException, NoSubjectParameterException, NodeNotFoundException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        if(user == null && process == 0) {
            throw new MissingPermissionException("No User or Process found in checking permissions");
        }

        if(user != null) {
            PmAnalyticsEntry perms = getUserPermissionsOn(targetId, user.getId());
            HashSet<String> operations = perms.getOperations();

            if (!perms.getOperations().contains(reqPerm)
                    && !perms.getOperations().contains(ALL_OPERATIONS)
                    && !(reqPerm.equals(ANY_OPERATIONS) && !operations.isEmpty())) {
                throw new MissingPermissionException("User " + user.getName() + " does not have the correct permissions on " + targetId + ": " + reqPerm);
            }
        }

        if(process != 0) {
            HashSet<String> prohibitedOps = getProhibitedOps(targetId, process, ProhibitionSubjectType.P.toString());
            if(prohibitedOps.contains(reqPerm)) {
                throw new MissingPermissionException("Process " + process + " does not have the correct permissions on " + targetId + ": " + reqPerm);
            }
        }
    }
}
