package lat.sofis.jpa.generic.dao.exceptions;

/**
 * Exception used to represent access to a domain not owned by the consumer
 */
public class DAOSecurityException extends Exception {

    public DAOSecurityException() {

    }
    
    private String field;
    
    public DAOSecurityException(String msg) {
        super(msg);
    }

    public DAOSecurityException(String msg, String field) {
        super(msg);
        this.field = field;
    }

    public DAOSecurityException(Exception msg) {
        super(msg);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    
}
