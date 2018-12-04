package gov.nist.csd.pm.demos.ndac.translator;

import gov.nist.csd.pm.demos.ndac.translator.algorithms.*;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pdp.services.Service;
import gov.nist.csd.pm.pep.response.TranslateResponse;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.io.IOException;
import java.sql.SQLException;

public class TranslatorService extends Service {
    public TranslatorService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    public TranslateResponse translate(String sql, String host, int port, String dbUsername, String dbPassword, String database) throws SQLException, IOException, ClassNotFoundException, JSQLParserException, PMException, InvalidEntityException {
        DatabaseContext ctx = new DatabaseContext(host, port, dbUsername, dbPassword, database);

        Statement statement = CCJSqlParserUtil.parse(sql);
        Algorithm algorithm = null;
        if (statement instanceof Select) {
            algorithm = new SelectAlgorithm((Select) statement, ctx);
        } else if (statement instanceof Insert) {
            algorithm = new InsertAlgorithm((Insert) statement, ctx);
        } else if (statement instanceof Update) {
            algorithm = new UpdateAlgorithm((Update) statement, ctx);
        } else if (statement instanceof Delete) {
        }

        if(algorithm != null) {
            return new TranslateResponse(algorithm.run());
        } else {
            throw new PMException(6000, "Algorithm returned null");
        }
    }

    public PAP getPAP() {
        return getPAP();
    }

}
