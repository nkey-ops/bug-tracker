package com.bluesky.bugtraker.exceptions;

public enum ErrorType {
    MISSING_REQUIRED_FIELD("Missing required field. Please check documentation for required fields"),
    RECORD_ALREADY_EXISTS("Record already exists"),
    RECORD_ALREADY_ADDED("Record already added"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    NO_RECORD_FOUND("Record with provided id is not found"),
    AUTHENTICATION_FAILED("Authentication failed"),
    COULD_NOT_UPDATE_RECORD("Could not update record"),
    COULD_NOT_DELETE_RECORD("Could not delete record"),
    EMAIL_ADDRESS_NOT_VERIFIED("Email address could not be verified"),
    EMAIL_VERIFICATION_TOKEN_IS_EXPIRED("Email verification token is expired");


    private String text;

    ErrorType(String text) {
        this.text = text;
    }

    /**
     * @return the errorMessage
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the errorMessage to set
     */
    public void setText(String text) {
        this.text = text;
    }

}
