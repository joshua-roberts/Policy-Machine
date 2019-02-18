package gov.nist.csd.pm.pap.loader.prohibitions;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.exceptions.PMProhibitionException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;

import java.util.List;

public interface ProhibitionsLoader {

    /**
     * Load prohibitions from a data source into a List of Prohibition objects.
     * @return a list of the prohibitions loaded.
     * @throws PMException if there is an error loading the prohibitions from the data source.
     */
    List<Prohibition> loadProhibitions() throws PMDBException, PMProhibitionException;
}
