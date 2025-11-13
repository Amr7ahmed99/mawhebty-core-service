package io.mawhebty.exceptions;

public class CompanyDocumentRequiredException extends BadDataException {
    public CompanyDocumentRequiredException(String companyName) {
        super("Company " + companyName + " must upload registration document");
    }
}
