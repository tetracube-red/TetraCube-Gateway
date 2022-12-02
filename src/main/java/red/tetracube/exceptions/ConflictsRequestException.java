package red.tetracube.exceptions;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class ConflictsRequestException extends ClientErrorException {

    public ConflictsRequestException() {
        super(Response.Status.CONFLICT);
    }

    public ConflictsRequestException(String message) {
        super(message, Response.Status.CONFLICT);
    }

    public ConflictsRequestException(Throwable cause) {
        super(Response.Status.CONFLICT, cause);
    }

    public ConflictsRequestException(String message, Throwable cause) {
        super(message, Response.Status.CONFLICT, cause);
    }
}
