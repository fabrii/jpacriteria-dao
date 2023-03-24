package lat.sofis.jpa.generic.dao.security;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Interface implemented by entities with domain security
 * @param <T> 
 */
public interface DataSecurity<T> {

    
    /**
     * When persisting an element, it must be checked if the user has permissions on the parent element
     * @return Parent field name
     */
    public String securityCreate();       
    
  
    
    /**
     * Once the object is stored in the database you can add security to the search query
     * @param domsec security domain
     * @param cb hibernate criteria builder
     * @param root
     * @return predicates to add in the query to access only the indicated domain. They are customized for the entity implementing the interface.
     * 
     */  
    public List<Predicate> securityNavigation(DomainSecurity domsec, CriteriaBuilder cb, Root<T> root);

      
}
