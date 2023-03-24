package lat.sofis.jpa.generic.dao.security;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a security domain
 */
public class DomainSecurity implements Serializable {
    
    private String scope;
    private Integer scopeOrder;
    private Object context;
    private Long contextSystemId;
    private Long userId;

    public DomainSecurity() {
    }
    
    public DomainSecurity(String scope, Integer scopeOrder, Object context, Long contextSystemId, Long userId) {
        this.scope = scope;
        this.scopeOrder = scopeOrder;
        this.context = context;
        this.contextSystemId = contextSystemId;
        this.userId = userId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getScopeOrder() {
        return scopeOrder;
    }

    public void setScopeOrder(Integer scopeOrder) {
        this.scopeOrder = scopeOrder;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public Long getContextSystemId() {
        return contextSystemId;
    }

    public void setContextSystemId(Long contextSystemId) {
        this.contextSystemId = contextSystemId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.scope);
        hash = 73 * hash + Objects.hashCode(this.scopeOrder);
        hash = 73 * hash + Objects.hashCode(this.context);
        hash = 73 * hash + Objects.hashCode(this.contextSystemId);
        hash = 73 * hash + Objects.hashCode(this.userId);
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
        final DomainSecurity other = (DomainSecurity) obj;
        if (!Objects.equals(this.scope, other.scope)) {
            return false;
        }
        if (!Objects.equals(this.scopeOrder, other.scopeOrder)) {
            return false;
        }
        if (!Objects.equals(this.context, other.context)) {
            return false;
        }
        if (!Objects.equals(this.contextSystemId, other.contextSystemId)) {
            return false;
        }
        return Objects.equals(this.userId, other.userId);
    }

    
    
    
    
    

}
