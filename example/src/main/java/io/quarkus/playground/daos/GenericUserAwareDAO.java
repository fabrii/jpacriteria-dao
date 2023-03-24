package io.quarkus.playground.daos;

import io.quarkus.playground.dtos.GenericFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lat.sofis.jpa.generic.dao.GenericDAO;
import lat.sofis.jpa.generic.dao.exceptions.DAOException;
import lat.sofis.jpa.generic.dao.security.DomainSecurity;

public class GenericUserAwareDAO<T> extends GenericDAO<T> implements Serializable {

    public GenericUserAwareDAO(EntityManager em, Class clase) throws Exception {
        super(em, clase, null);
    }

    public List<Predicate> generatePredicates(GenericFilter filtro, Root<T> root) throws Exception {
        //Implement generic generatePredicates
        return new ArrayList<>();
    }

    public List<T> searchByFilter(GenericFilter filtro, DomainSecurity domsec) throws Exception, DAOException {
        Boolean nativeQuery = (filtro.getIncludeFields()!= null && filtro.getIncludeFields().length > 0);
        List<Predicate> predicates = generatePredicates(filtro, getQueryRoot(nativeQuery));
        return this.searchByFilter(predicates, filtro.getFirst(), filtro.getMaxResults(), filtro.getOrderBy(), filtro.getAscending(), domsec, filtro.getIncludeFields());
    }

    public Long searchTotalByFilter(GenericFilter filtro, DomainSecurity domsec) throws Exception {
        List<Predicate> predicates = generatePredicates(filtro, getCountRoot());
        return this.searchTotalByFilter(predicates, domsec);
    }

}
