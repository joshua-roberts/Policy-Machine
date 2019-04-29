package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.pep.sessions.Sessions;
import gov.nist.csd.pm.pip.obligations.Obligations;
import gov.nist.csd.pm.prohibitions.ProhibitionsDAO;

/**
 * PAP is the Policy Information Point. The purpose of the PAP is to expose the underlying policy data to the PDP and EPP.
 * It initializes the backend using the connection properties in /resource/db.config.  This servlet can
 * be access via ../index.jsp upon starting the server.The PAP also stores the in memory graph that will be used for
 * decision making.
 */
public class PAP {

    private static PAP pap;

    public static void init(Graph graphPAP, ProhibitionsDAO prohibitionsPAP, Obligations obligationsPAP) {
        pap = new PAP(graphPAP, prohibitionsPAP, obligationsPAP);
    }

    public static PAP getPAP() {
        if(pap == null) {
            throw new IllegalStateException("PAP is not initialized.  Initialize the PAP with PAP.init(...).");
        }

        return pap;
    }

    private Graph           graphPAP;
    private ProhibitionsDAO prohibitionsPAP;
    private Obligations     obligationsPAP;

    public PAP(Graph graphPAP, ProhibitionsDAO prohibitionsPAP, Obligations obligationsPAP) {
        this.graphPAP = graphPAP;
        this.prohibitionsPAP = prohibitionsPAP;
        this.obligationsPAP = obligationsPAP;
    }

    public Graph getGraphPAP() {
        return graphPAP;
    }

    public void setGraphPAP(Graph graphPAP) {
        this.graphPAP = graphPAP;
    }

    public ProhibitionsDAO getProhibitionsPAP() {
        return prohibitionsPAP;
    }

    public void setProhibitionsPAP(ProhibitionsDAO prohibitionsPAP) {
        this.prohibitionsPAP = prohibitionsPAP;
    }

    public Obligations getObligationsPAP() {
        return obligationsPAP;
    }

    public void setObligationsPAP(Obligations obligationsPAP) {
        this.obligationsPAP = obligationsPAP;
    }
}
