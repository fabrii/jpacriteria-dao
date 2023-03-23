# JPA Criteria DAO library

## GenericDAO class

Internally uses javax.persistence.criteria classes provided by JPA. It exposes an abstract class called GenericDAO, which provides the following methods:


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
    public List<T> searchByFilter(List<Predicate> predicates, Integer first, Integer maxResults, String[] orderBy, boolean[] ascending, DomainSecurity domsec, String[] fields) throws Exception, DAOException;

    /**
     * Returns the total number of elements that satisfy the criteria
     *
     * @param predicates search predicates
     * @param domsec security domain
     * @return
     * @throws Exception if an internal error occurs
     */
    public Long searchTotalByFilter(List<Predicate> predicates, DomainSecurity domsec) throws Exception;

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
    public T save(T elemento, DomainSecurity domsec) throws Exception, DAOSecurityException;

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
    public void deleteById(Long id, DomainSecurity domsec) throws DAOSecurityException, Exception;

    /**
     * Returns whether the object exists
     *
     * @param id object identifier
     * @param domsec security domain
     * @return
     * @throws Exception if an internal error occurs
     */
    public Boolean existsById(Object id, DomainSecurity domsec) throws Exception;

    /**
     * Returns the object of the type that has the indicated id
     *
     * @param id object identifier
     * @param domsec security domain
     * @return the object if it exists and belongs to the consumer's security
     * domain, null otherwise
     * @throws Exception if an internal error occurs
     */
    public T findById(Long id, DomainSecurity domsec) throws Exception;

## Functionality

The library implements various optimizations and security controls:
- If it detects that the underlying database is Oracle, it sends the attribute “E2E_CONTEXT.CLIENT_IDENTIFIER” with the user identifier.
- It is in charge of automatically applying domain security to all entities that implement the “DataSecurity” interface.
- Allows you to include a subset of fields that you want to query. Performs automatic mapping between the result of a native query and the entity.
- Automatically detects if the executed query performs a JOIN with a collection and applies DISTINCT.
- Reads the “optional” field of *ToOne relations. If the relationship is optional=false, the library performs INNER JOINS instead of LEFT OUTER JOINS, which are more performant.

Returns two types of exception:
- DAOSecurityException: used to represent data access errors in domains for which there are no permissions.
- DAOException: used to represent errors in the filters sent to the DAO to perform searches.

The GenericDAO class constructor receives three parameters:
- Entity manager
- Entity class
- User identifier (can be null)


## Use in a project


### Apply domain security to an entity

Users of a system may have access to a given domain of data. For example: system > institution > executing unit. In other words:
 - A collection of institutions belong to a system.
 - An executing unit belongs to an institution. One institution may have multiple units.
 
If a user is assigned a profile within an executing unit, they will only be able to work with the data that belongs to it. 
At the REST layer level, the API used to perform queries is the same, but depending on the JWT (Json Web Token) domain, the returned data may vary. 
Entities with domain security must implement the DataSecurity interface provided by the GenericDAO library. 

An example of the "ExampleEntity" with DataSecurity is shown below:


```
@Entity
@Table(name = "example_entity")
public class ExampleEntity implements Serializable, DataSecurity<ExampleEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executing_unit_id")
    private ExecutingUnit executingUnit;

    ...

    @Override
    public String securityCreate() {
        return "executingUnit";
    }

    @Override
    public List<Predicate> securityNavigation(DomainSecurity dom, CriteriaBuilder cb, Root<ExampleEntity> root) {

        List<Predicate> predicates = new ArrayList<>();

        if (dom.getScope().equalsIgnoreCase("SYSTEM")) {
            //No security
        } else if (dom.getScope().equalsIgnoreCase("INSTITUTION")) {
            predicates.add(cb.equal(root.get("executingUnit").get("institution").get("id"), dom.getContext()));
        } else if (dom.getScope().equalsIgnoreCase("EXECUTING_UNIT")) {
         predicates.add(cb.equal(root.get("executingUnit").get("id"), dom.getContext()));
        } else {
            predicates.add(cb.equal(root.get("id"), -1));
        }

        return predicates;
    }

}
```

Special attention should be paid to the securityCreate and securityNavigation methods:

* securityCreate

It is used when the entity is being saved for the first time. When persisting an element, it must be checked if the user has permissions on the parent domain. In the example, it must be verified that the user has permissions on the relationship "executingUnit". Internally, the library fires a query on the ExecutingUnit entity, which also implements DataSecurity, to verify if the user has permissions on it.

* securityNavigation

It is used for entity query, update, and delete operations. In these cases, the entity already exists in the database, so it is possible to apply security directly to it. The method must return a list of predicates that will be added to the query that is executed against the database. In the example, we have the following cases:
- If the security domain is "SYSTEM", then the user has access to all entities. Empty predicate list is returned.
- If the security domain is "INSTITUTION", a predicate is returned that filters the entities by the user institution id.
- If the security domain is "EXECUTING_UNIT", a predicate is returned that filters the entiyies by the user executing unit id.
- In any other case, a predicate is returned so that the query does not return any results.

In addition to the implementation of the DataSecurity interface, it is necessary to send the consumer's DomainSecurity to the genericdao library. Therefore, when you have an entity of this type, the vast majority of methods in the business layer must receive the security domain as a parameter and propagate it to the DAO layer. Sending this DTO is optional since internally the backend may require executing database queries without domain restrictions. It is the responsibility of the developer to verify that the methods propagate this data when necessary.


### REST API

The user's domain is obtained from authenticatedUser.getDomain() and is propagated to the findById method of the business layer.

```
@Inject
@Named(ConstantesCommon.AUTH_USER_BEAN)
AuthenticatedUser authenticatedUser; // Implementation specific. Must have a way to get the user domain in the API.

@GET
@Path("/{id:[1-9][0-9]*}")
@RolesAllowed({XXX})
public Response findById(@PathParam("id") Long id) {
    try {
        ExampleEntity entity = service.findById(id, authenticatedUser.getDomain());
        if (entity == null) {
            return Response.status(HttpStatus.SC_NOT_FOUND).build();
        } else {
            return Response.status(HttpStatus.SC_OK).entity(entity).build();
        }
    } catch (Exception ex) {
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
    }
}
```


### Business layer

```
public ExampleEntity findById(Long id, DomainSecurity domsec) throws GeneralException {
    if (id != null) {
        try {
            ExampleDAO dao = new ExampleDAO(em); //ExampleDAO extends GenericUserAwareDAO
            return dao.findById(id, domsec);
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }
    }
    return null;
}
```

### DAO Layer - ExampleDAO and GenericUserAwareDAO

```
public class ExampleDAO extends GenericUserAwareDAO<ExampleEntity> implements Serializable {

    public ExampleDAO(EntityManager em) throws Exception {
        super(em, ExampleEntity.class);
    }

	/**
	* Overrides generatePredicates method with custom ExampleEntity predicates
	*/
    @Override
    public List<Predicate> generatePredicates(GenericFilter genericFilter, Root<ExampleEntity> root) {

        ExampleEntityFilter filter = (ExampleEntityFilter) genericFilter;

        List<Predicate> predicates = new ArrayList();

        if (filter.getId()!= null) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
       
		...
       
        return predicates;
    }

}
```

```
/**
 * GenericUserAwareDAO
 * <p>
 * Provides data access functionalities for generic entity
 * Utility that finds userId using CDI class AuthenticatedUser and automatically propagates it to GenericDAO
 * It also handles the invocation of GenericDAO methods using the attributes from the filter DTO.
 * This class is implementation specific.
 */
public class GenericUserAwareDAO<T> extends GenericDAO<T> implements Serializable {

    public GenericUserAwareDAO(EntityManager em, Class clase) throws Exception {
        super(em, clase, CDI.current().select(AuthenticatedUser.class, new NamedAnnotation(ConstantesCommon.AUTH_USER_BEAN)).get().getUserId());
    }

	/**
	* Generic method that should be overrided by the DAO extending GenericUserAwareDAO
	public List<Predicate> generatePredicates(FiltroGenerico filtro, Root<T> root) throws Exception {
		...
    }

    public List<T> searchByFilter(GenericFilter filtro, DomainSecurity domsec) throws Exception, DAOException {
        Boolean nativeQuery = (filtro.getIncludeFields() != null && filtro.getIncludeFields().length > 0);
        List<Predicate> predicates = generatePredicates(filtro, getQueryRoot(nativeQuery));
        return this.searchByFilter(predicates, filtro.getFirst(), filtro.getMaxResults(), filtro.getOrderBy(), filtro.getAscending(), domsec, filtro.getIncludeFields());
    }

    public Long searchTotalByFilter(GenericFilter filtro, DomainSecurity domsec) throws Exception {
        List<Predicate> predicates = generatePredicates(filtro, getCountRoot());
        return this.searchTotalByFilter(predicates, domsec);
    }

}
```

### DTO Filters

```
public class ExampleEntityFilter extends GenericFilter implements Serializable {

    @QueryParam("id")
    private Long id;

    public ExampleEntityFilter() {
    }
	
	...
}

public class GenericFilter implements Serializable {

   
    @QueryParam("first")
    private Integer first;

    @QueryParam("maxResults")
    private Integer maxResults;

    @QueryParam("orderBy[]")
    private String[] orderBy;

    @QueryParam("ascending[]")
    private boolean[] ascending;

    @QueryParam("if[]")
    private String[] includeFields;
	
	...
	
}
```

## About

Created within a project led by [MEF](https://www.mef.gub.uy/) and [Sofis Solutions](https://sofis-solutions.com/).