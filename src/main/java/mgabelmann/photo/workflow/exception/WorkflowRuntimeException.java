package mgabelmann.photo.workflow.exception;

public class WorkflowRuntimeException extends RuntimeException {

    public WorkflowRuntimeException() {
        super();
    }

    public WorkflowRuntimeException(final String message) {
        super(message);
    }

    
    public WorkflowRuntimeException(final Throwable cause) {
        super(cause);
    }

    
    public WorkflowRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
