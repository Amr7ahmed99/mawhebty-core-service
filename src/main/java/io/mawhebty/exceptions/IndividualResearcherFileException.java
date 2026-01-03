package io.mawhebty.exceptions;

public class IndividualResearcherFileException extends BadDataException {
    public IndividualResearcherFileException() {
        super("Individual researcher cannot upload files");
    }

    public IndividualResearcherFileException(String msg) {
        super(msg);
    }
}
