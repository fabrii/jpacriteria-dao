package lat.sofis.jpa.generic.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.transaction.Transactional;
import lat.sofis.jpa.generic.dao.exceptions.DAOException;
import lat.sofis.jpa.generic.dao.exceptions.DAOSecurityException;
import lat.sofis.jpa.generic.dao.security.DataSecurity;
import lat.sofis.jpa.generic.dao.security.DomainSecurity;
import lat.sofis.jpa.generic.dao.utils.CriteriaApiUtils;
import lat.sofis.jpa.generic.dao.utils.PersistenceUtils;
import lat.sofis.jpa.generic.dao.utils.ReflectionUtils;
import org.hibernate.Session;


/**
 * GenericDAO provides data access functionalities for generic entities
 *
 * @param <T>
 */
public abstract class GenericDAO<T> implements Serializable {

    protected EntityManager em;
    protected final Class<T> clazz;
    protected Session session;
    protected CriteriaBuilder cb;

    private CriteriaQuery cqlong;
    private CriteriaQuery cqclass;
    private CriteriaQuery cqarrayObj;
    private Root rootLong;
    private Root rootClass;
    private Root rootArrayObj;

    private static final Logger LOGGER = Logger.getLogger(GenericDAO.class.getName());

    public GenericDAO(EntityManager em, Class clase, String clientIdentifier) throws Exception {

        this.em = em;
        this.clazz = clase;
        this.session = em.unwrap(Session.class);
        this.cb = session.getCriteriaBuilder();

        cqclass = cb.createQuery(clazz);
        cqlong = cb.createQuery(Long.class);
        cqarrayObj = cb.createQuery(Object[].class);

        rootClass = cqclass.from(clazz);
        rootLong = cqlong.from(clazz);
        rootArrayObj = cqarrayObj.from(clazz);

        LOGGER.log(Level.FINE, "Client identifier: " + clientIdentifier);

        if (clientIdentifier != null) {

            String dialect = (String) em.getEntityManagerFactory().getProperties().get("hibernate.dialect");

            if (dialect != null && dialect.toLowerCase().contains("oracle")) {

                session.doWork(new org.hibernate.jdbc.Work() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        conn.setClientInfo("E2E_CONTEXT.CLIENT_IDENTIFIER", clientIdentifier);
                    }
                });

            }
        }

    }

    /**
     * Query the elements that satisfy the criteria
     *
     * @param predicates search predicates
     * @param first first result position
     * @param maxResults maximum number of results
     * @param orderBy array of fields to sort by
     * @param ascending array indicating if the ordering of each field is asc
     * (true) or desc (false)
     * @param domsec security domain
     * @param fields fields included in the query result. Allow navigation.
     * @return
     * @throws Exception if an internal error occurs
     * @throws DAOException if any parameter is wrong
     */
    public List<T> searchByFilter(List<Predicate> predicates, Integer first, Integer maxResults, String[] orderBy, boolean[] ascending, DomainSecurity domsec, String[] fields) throws Exception, DAOException {

        Boolean nativeQuery = (fields != null && fields.length > 0);
        List<String> propertyNamesList = null;

        CriteriaQuery cq = null;
        Root root = null;
        if (nativeQuery) {
            cq = cqarrayObj;
            root = rootArrayObj;
        } else {
            cq = cqclass;
            root = rootClass;
        }

        predicates.addAll(createSecurityNavigationPredicate(clazz, domsec, root));

        if (nativeQuery) {
            propertyNamesList = new ArrayList<>(Arrays.asList(fields));
            List<Selection<?>> selectedFields = new ArrayList<>();
            String idField = ReflectionUtils.obtenerNombreCampoAnotado(clazz, Id.class);
            if (idField != null) {
                //Always add ID field. TODO: add id of nested fields
                propertyNamesList.add(0, idField);
            }
            for (String s : propertyNamesList) {
                String[] nested = s.split("\\.");

                From p = root;
                Path finalPath = null;
                Boolean optional = false;
                Class objectClass = clazz;
                for (int i = 0; i < nested.length; i++) {
                    String n = nested[i];

                    Field field = ReflectionUtils.getDeclaredField(objectClass, n);
                    if (field == null) {
                        throw new DAOException("FIELD_INCORRECT", s);
                    }
                    objectClass = field.getType();
                    optional = optional || PersistenceUtils.fieldIsOptional(field);

                    if (i < nested.length - 1) {
                        p = CriteriaApiUtils.join(p, n, optional ? JoinType.LEFT : JoinType.INNER);
                    } else {
                        finalPath = p.get(n);
                    }

                }
                selectedFields.add(finalPath);
            }
            cq.multiselect(selectedFields).distinct(CriteriaApiUtils.hasPluralJoin(root));
        } else {
            cq.select(root).distinct(CriteriaApiUtils.hasPluralJoin(root));
        }
        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        List<Order> orderList = new ArrayList<>();
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                String orderByField = orderBy[i];

                String[] nested = orderByField.split("\\.");

                From p = root;
                Path finalPath = null;
                Boolean optional = false;
                Class objectClass = clazz;
                for (int f = 0; f < nested.length; f++) {
                    String n = nested[f];

                    Field field = ReflectionUtils.getDeclaredField(objectClass, n);
                    if (field == null) {
                        throw new DAOException("FIELD_ORDERBY_INCORRECT", orderByField);
                    }
                    objectClass = field.getType();
                    optional = optional || PersistenceUtils.fieldIsOptional(field);

                    if (f < nested.length - 1) {
                        p = CriteriaApiUtils.join(p, n, optional ? JoinType.LEFT : JoinType.INNER);
                    } else {
                        finalPath = p.get(n);
                    }

                }

                if (ascending != null && ascending.length > i) {
                    Boolean asc = ascending[i];
                    if (Boolean.TRUE.equals(asc)) {
                        orderList.add(cb.asc(finalPath));
                    } else {
                        orderList.add(cb.desc(finalPath));
                    }
                } else {
                    throw new DAOException("ASCENDING_FIELD_EMPTY", orderByField);
                }
            }
        }
        cq.orderBy(orderList);

        Query q = session.createQuery(cq);

        if (maxResults != null) {
            q.setMaxResults(maxResults);
        }
        if (first != null) {
            q.setFirstResult(first);
        }

        if (nativeQuery) {
            List<Object[]> result = q.getResultList();
            List<T> referenceList = new ArrayList<T>(result.size());
            for (Object[] row : result) {
                T ent = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < row.length; i++) {
                    Object o = row[i];
                    if (o != null) {
                        String propertyName = propertyNamesList.get(i);
                        PersistenceUtils.setProperty(ent, propertyName, o, o.getClass());
                    }
                }
                referenceList.add(ent);
            }
            return referenceList;

        } else {
            return q.getResultList();
        }

    }

    /**
     * Returns the total number of elements that satisfy the criteria
     *
     * @param predicates search predicates
     * @param domsec security domain
     * @return
     * @throws Exception if an internal error occurs
     */
    public Long searchTotalByFilter(List<Predicate> predicates, DomainSecurity domsec) throws Exception {
        CriteriaQuery<Long> cq = cqlong;
        Root root = rootLong;
        predicates.addAll(createSecurityNavigationPredicate(clazz, domsec, root));
        if (CriteriaApiUtils.hasPluralJoin(root)) {
            cq.select(cb.countDistinct(root));
        } else {
            cq.select(cb.count(root));
        }
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        return session.createQuery(cq).getSingleResult();
    }

    /**
     * Save the object
     *
     * @param elemento entity
     * @param domsec security domain
     * @return
     * @throws DAOSecurityException if the object does not belong to the
     * consumer's domain
     * @throws Exception if an internal error occurs
     */
    @Transactional(rollbackOn = Exception.class)
    public T save(T elemento, DomainSecurity domsec) throws Exception, DAOSecurityException {

        Field campoId = ReflectionUtils.obtenerCampoAnotado(clazz, Id.class);
        Object value = campoId.get(elemento);

        checkEntitySaveSecurity(elemento, domsec);

        if (value == null) {
            em.persist(elemento);
        } else {
            Field campoVersion = ReflectionUtils.obtenerCampoAnotado(clazz, Version.class);
            if (campoVersion != null) {
                Object version = campoVersion.get(elemento);
                if (version == null) {
                    campoVersion.set(elemento, 0);
                }
            }
            elemento = em.merge(elemento);
        }
        return elemento;
    }

    /**
     * Deletes the object with the given id
     *
     * @param id object identifier
     * @param domsec security domain
     * @throws DAOSecurityException if the object does not belong to the
     * consumer's domain
     * @throws Exception if an internal error occurs
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteById(Long id, DomainSecurity domsec) throws DAOSecurityException, Exception {
        T object = this.findById(id, domsec);
        if (object != null) {
            em.remove(object);
        } else {
            throw new DAOSecurityException("NOT_FOUND");
        }
    }

    /**
     * Returns whether the object exists
     *
     * @param id object identifier
     * @param domsec security domain
     * @return
     * @throws Exception if an internal error occurs
     */
    public Boolean existsById(Object id, DomainSecurity domsec) throws Exception {
        String campoId = ReflectionUtils.obtenerCampoAnotado(clazz, Id.class).getName();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<T> root = cq.from(clazz);
        cq.select(cb.count(root));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(campoId), id));
        predicates.addAll(createSecurityNavigationPredicate(clazz, domsec, root));
        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Long count = session.createQuery(cq).getSingleResult();
        if (count > 0L) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Returns the object of the type that has the indicated id
     *
     * @param id object identifier
     * @param domsec security domain
     * @return the object if it exists and belongs to the consumer's security
     * domain, null otherwise
     * @throws Exception if an internal error occurs
     */
    public T findById(Long id, DomainSecurity domsec) throws Exception {
        String fieldId = ReflectionUtils.obtenerCampoAnotado(clazz, Id.class).getName();
        CriteriaQuery cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);
        cq.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(fieldId), id));
        predicates.addAll(createSecurityNavigationPredicate(clazz, domsec, root));
        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query q = session.createQuery(cq);

        List<T> toReturnList = q.getResultList();
        if (toReturnList.isEmpty()) {
            return null;
        }
        return toReturnList.get(0);
    }

    /**
     * Method in charge of creating the predicates for the security domain of
     * the consumer and the entities that implement the DataSecurity interface
     *
     * @param entityClass
     * @param securityOperation
     * @param root
     * @return
     * @throws Exception
     */
    private List<Predicate> createSecurityNavigationPredicate(Class<T> entityClass, DomainSecurity securityOperation, Root<T> root) throws Exception {
        if (DataSecurity.class.isAssignableFrom(entityClass) && securityOperation != null) {
            DataSecurity dataSec = (DataSecurity) entityClass.getDeclaredConstructor().newInstance();
            return dataSec.securityNavigation(securityOperation, cb, root);
        }
        return new ArrayList<>();
    }

    /**
     * Method in charge of verifying if the consumer has permissions to create
     * an entity that implements the DataSecurity interface
     *
     * @param entity
     * @param domsec security domain
     * @throws DAOSecurityException
     * @throws Exception
     */
    private void checkEntitySaveSecurity(T entity, DomainSecurity domsec) throws DAOSecurityException, Exception {

        LOGGER.log(Level.FINE, "Verify data security on: " + entity.getClass().getCanonicalName());
        LOGGER.log(Level.FINE, "Class instance of DataSecurity?: " + (entity instanceof DataSecurity));
        LOGGER.log(Level.FINE, "Domain required: " + domsec);

        //if security domain is null or entity is not an instance of DataSecurity, then do not apply security
        if (!(entity instanceof DataSecurity) || domsec == null) {
            LOGGER.log(Level.FINE, "Security not required. Returning...");
            return;
        }

        DataSecurity dataSecEntity = (DataSecurity) entity;

        try {

            //For now, all entities that extend DataSecurity must have @Id
            Field campoId = ReflectionUtils.obtenerCampoAnotado(clazz, Id.class);
            Object value = campoId.get(entity);
            if (value != null) {

                LOGGER.log(Level.FINE, "Save operation: MERGE. Verify self security");

                if (!this.existsById(value, domsec)) {
                    LOGGER.log(Level.FINE, "Entity not found");
                    throw new DAOSecurityException("NOT_FOUND");
                }

                LOGGER.log(Level.FINE, "Entity found");

            } else {

                LOGGER.log(Level.FINE, "Save operation: PERSIST. Verify parent security");

                String ambitoNavigation = dataSecEntity.securityCreate();
                LOGGER.log(Level.FINE, "Ambit navigation: " + ambitoNavigation);
                //if the parent scope is null then it is not validated
                if (ambitoNavigation != null) {
                    Object parentEntity = PersistenceUtils.getPropertyValue(dataSecEntity, ambitoNavigation);
                    Class parentClass = parentEntity.getClass();

                    Field parentFieldId = ReflectionUtils.obtenerCampoAnotado(parentClass, Id.class);
                    Object parentValueId = parentFieldId.get(parentEntity);

                    LOGGER.log(Level.FINE, "Parent value id: " + parentValueId);
                    LOGGER.log(Level.FINE, "Parent class: " + parentClass);

                    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                    Root<T> root = cq.from(parentClass);
                    cq.select(cb.count(root));

                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get(parentFieldId.getName()), parentValueId));
                    predicates.addAll(createSecurityNavigationPredicate(parentClass, domsec, root));
                    cq.where(predicates.toArray(new Predicate[predicates.size()]));

                    Long count = session.createQuery(cq).getSingleResult();

                    if (count == 0L) {
                        LOGGER.log(Level.FINE, "Parent entity not found");
                        throw new DAOSecurityException("NOT_FOUND", ambitoNavigation);
                    }

                    LOGGER.log(Level.FINE, "Parent entity found");

                }

            }

        } catch (DAOSecurityException dse) {
            throw dse;
        } catch (Exception w) {
            throw w;
        }

    }

    protected Root getCountRoot() {
        return this.rootLong;
    }

    protected Root getQueryRoot(Boolean nativeQuery) {
        if (Boolean.TRUE.equals(nativeQuery)) {
            return this.rootArrayObj;
        } else {
            return this.rootClass;
        }
    }

}
