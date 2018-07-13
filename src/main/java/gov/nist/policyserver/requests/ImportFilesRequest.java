package gov.nist.policyserver.requests;

import gov.nist.policyserver.model.imports.ImportFile;

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
