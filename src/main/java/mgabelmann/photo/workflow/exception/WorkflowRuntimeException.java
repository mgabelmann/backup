package mgabelmann.photo.workflow.exception;

public class WorkflowRuntimeException extends RuntimeException {

    public WorkflowRuntimeException() {
        
    }

    public WorkflowRuntimeException(String message) {
        super(message);
    }

    
    public WorkflowRuntimeException(Throwable cause) {
        super(cause);
    }

    
    public WorkflowRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
