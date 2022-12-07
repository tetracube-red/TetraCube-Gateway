package red.tetracube.core.exceptions;

import red.tetracube.core.models.Result;

public class PipelineInterruptionException extends RuntimeException {

    private final Result<?> pessimisticOperationResult;

    public PipelineInterruptionException(Result<?> pessimisticOperationResult) {
        super();
        this.pessimisticOperationResult = pessimisticOperationResult;
    }
}
