package io.quarkus.playground.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lat.sofis.jpa.generic.dao.security.DataSecurity;
import lat.sofis.jpa.generic.dao.security.DomainSecurity;

@Entity
@Table(name = "example_entities")
public class ExampleEntity implements Serializable, DataSecurity<ExampleEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executing_unit_id")
    private ExecutingUnit executingUnit;

    @Column(name = "name")
    private String name;

    public ExampleEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public ExecutingUnit getExecutingUnit() {
        return executingUnit;
    }

    public void setExecutingUnit(ExecutingUnit executingUnit) {
        this.executingUnit = executingUnit;
    }

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExampleEntity other = (ExampleEntity) obj;
        return Objects.equals(this.id, other.id);
    }

}
