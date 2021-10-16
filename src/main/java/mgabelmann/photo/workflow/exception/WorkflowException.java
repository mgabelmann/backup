package mgabelmann.photo.workflow.exception;


public class WorkflowException extends Exception {

    public WorkflowException() {
        
    }
    
    public WorkflowException(String message) {
        super(message);
    }

    
    public WorkflowException(Throwable cause) {
        super(cause);
    }

    
    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }

}
