package io.mawhebty.exceptions;

public class IndividualResearcherFileException extends BadDataException {
    public IndividualResearcherFileException() {
        super("Individual researcher cannot upload files");
    }
}
