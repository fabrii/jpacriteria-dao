package io.quarkus.playground.daos;

import io.quarkus.playground.dtos.ExampleEntityFilter;
import io.quarkus.playground.dtos.GenericFilter;
import io.quarkus.playground.entities.ExampleEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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

        if (filter.getId() != null) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
        
        /// More filters

        return predicates;
    }

}
