package red.tetracube.core.models;

import io.quarkus.security.UnauthorizedException;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.exceptions.ConflictsRequestException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

public class BusinessValidationResult {

    private final Boolean success;
    private final FailureReason failureReason;
    private final String failureMessage;

    private BusinessValidationResult(Boolean success, FailureReason failureReason, String failureMessage) {
        this.success = success;
        this.failureReason = failureReason;
        this.failureMessage = failureMessage;
    }

    public static BusinessValidationResult success() {
        return new BusinessValidationResult(true, null, null);
    }

    public static BusinessValidationResult failed(FailureReason failureReason, String message) {
        return new BusinessValidationResult(false, failureReason, message);
    }

    public void mapAsResponse() throws ClientErrorException {
        switch (this.failureReason) {
            case CONFLICTS -> throw new ConflictsRequestException(this.failureMessage);
            case NOT_FOUND -> throw new NotFoundException(this.failureMessage);
            case BAD_REQUEST -> throw new BadRequestException(this.failureMessage);
            case UNAUTHORIZED -> throw new UnauthorizedException(this.failureMessage);
        }
    }

    public Boolean getSuccess() {
        return success;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
    
}
