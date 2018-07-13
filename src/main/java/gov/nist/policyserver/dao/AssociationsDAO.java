package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public interface AssociationsDAO {

    void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException;

    void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> ops) throws DatabaseException;

    void deleteAssociation(long uaId, long targetId) throws DatabaseException;
}
