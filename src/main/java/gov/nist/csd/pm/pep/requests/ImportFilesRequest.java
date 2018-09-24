package gov.nist.csd.pm.pep.requests;

import gov.nist.csd.pm.demos.cloud.ImportFile;

public class ImportFilesRequest {
    private ImportFile[] files;
    private String source;

    public ImportFile[] getFiles() {
        return files;
    }

    public void setFiles(ImportFile[] files) {
        this.files = files;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
