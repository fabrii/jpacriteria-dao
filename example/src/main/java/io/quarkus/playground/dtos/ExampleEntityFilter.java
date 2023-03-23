package io.quarkus.playground.dtos;

import java.io.Serializable;
import java.util.Objects;
import javax.ws.rs.QueryParam;

public class ExampleEntityFilter extends GenericFilter implements Serializable {

    @QueryParam("id")
    private Long id;

    public ExampleEntityFilter() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + super.hashCode();
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
        final ExampleEntityFilter other = (ExampleEntityFilter) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return super.equals(obj);
    }

}
