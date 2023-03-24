package lat.sofis.jpa.generic.dao.utils;

import java.util.logging.Logger;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.PluralJoin;

/**
 * Utilities for JPA Criteria Api
 */
public class CriteriaApiUtils {

    private static final Logger LOGGER = Logger.getLogger(CriteriaApiUtils.class.getName());

    protected CriteriaBuilder cb;

    /**
     * Given a criteriaRoot, it is responsible for returning the Join necessary to navigate to the indicated attribute.
     * If the Join was already created for root, it returns it. If it does not exist, create it with the indicated JoinType. 
     * @param <X>
     * @param <Y>
     * @param criteriaRoot
     * @param attribute
     * @param joinType
     * @return 
     */
    public static <X, Y> Join<X, Y> join(From<X, Y> criteriaRoot,
            String attribute,
            JoinType joinType) {

        return (Join<X, Y>) criteriaRoot.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(attribute))
                .findFirst()
                .orElseGet(() -> criteriaRoot.join(attribute, joinType));
    }

    /**
     * Given a criteriaRoot, it navigates through the Joins made to identify if there are any of type PluralJoin
     * <p>
     * It is useful to know whether to perform distinct or not.
     * @param <X>
     * @param <Y>
     * @param criteriaRoot
     * @return true if there is a join of type PluralJoin, false otherwise
     */
    public static <X, Y> boolean hasPluralJoin(From<X, Y> criteriaRoot) {
        for (Join j : criteriaRoot.getJoins()) {
            if (PluralJoin.class.isAssignableFrom(j.getClass())) {
                return true;
            } else if (hasPluralJoin(j)) {
                return true;
            }
        }
        return false;
    }

}
