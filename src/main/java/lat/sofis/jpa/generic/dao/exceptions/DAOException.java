package lat.sofis.jpa.generic.dao.exceptions;

/**
 * Exception used to represent a data error
 */
public class DAOException extends Exception {

    public DAOException() {

    }
    
    private String field;
    
    public DAOException(String msg) {
        super(msg);
    }

    public DAOException(String msg, String field) {
        super(msg);
        this.field = field;
    }

    public DAOException(Exception msg) {
        super(msg);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    
}
