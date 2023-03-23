package io.quarkus.playground.dtos;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import javax.ws.rs.QueryParam;

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

    public GenericFilter() {
        this.first = 0;
    }

    public Integer getFirst() {
        return first;
    }

    public void setFirst(Integer first) {
        this.first = first;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public String[] getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String[] orderBy) {
        this.orderBy = orderBy;
    }

    public boolean[] getAscending() {
        return ascending;
    }

    public void setAscending(boolean[] ascending) {
        this.ascending = ascending;
    }

    public String[] getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(String[] includeFields) {
        this.includeFields = includeFields;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.first);
        hash = 13 * hash + Objects.hashCode(this.maxResults);
        hash = 13 * hash + Arrays.deepHashCode(this.orderBy);
        hash = 13 * hash + Arrays.hashCode(this.ascending);
        hash = 13 * hash + Arrays.deepHashCode(this.includeFields);
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
        final GenericFilter other = (GenericFilter) obj;
        if (!Objects.equals(this.first, other.first)) {
            return false;
        }
        if (!Objects.equals(this.maxResults, other.maxResults)) {
            return false;
        }
        if (!Arrays.deepEquals(this.orderBy, other.orderBy)) {
            return false;
        }
        if (!Arrays.equals(this.ascending, other.ascending)) {
            return false;
        }
        return Arrays.deepEquals(this.includeFields, other.includeFields);
    }

   

    
    

}
