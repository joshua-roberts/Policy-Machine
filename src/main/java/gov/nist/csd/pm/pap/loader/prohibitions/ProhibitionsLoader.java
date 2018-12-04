package gov.nist.csd.pm.pap.loader.prohibitions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;

import java.util.List;

public interface ProhibitionsLoader {

    /**
     * Load prohibitions from a data source into a List of Prohibition objects.
     * @return A list of the prohibitions loaded.
     */
    List<Prohibition> loadProhibitions() throws DatabaseException, InvalidProhibitionSubjectTypeException;
}
