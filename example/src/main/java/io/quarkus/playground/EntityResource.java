package io.quarkus.playground;

import io.quarkus.playground.daos.ExampleDAO;
import io.quarkus.playground.dtos.ExampleEntityFilter;
import io.quarkus.playground.entities.ExampleEntity;
import java.util.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lat.sofis.jpa.generic.dao.security.DomainSecurity;

@Path("/entity")
public class EntityResource {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/{id}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response findById(@PathParam long id) {
        try {
            //In real world application, obtain domainSecurity from JWT
            DomainSecurity dom = new DomainSecurity();
            dom.setScope("INSTITUTION");
            dom.setContext(1L);
            dom.setContextSystemId(1L);

            ExampleDAO dao = new ExampleDAO(em);
            ExampleEntity entity = dao.findById(id, dom);
            if (entity == null) {
                return Response.status(404).build();
            } else {
                return Response.status(200).entity(entity).build();
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/search/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response search(@BeanParam ExampleEntityFilter filter) {

        try {
            ExampleDAO dao = new ExampleDAO(em);
            
            DomainSecurity dom = new DomainSecurity();
            dom.setScope("INSTITUTION");
            dom.setContext(2L);
            dom.setContextSystemId(1L);
            
            //Only these fields
            filter.setIncludeFields(new String[]{"executingUnit.id", "executingUnit.name"});
            
            return Response.status(200).entity(dao.searchByFilter(filter, dom)).build();
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            return Response.status(500).build();
        }

    }
}
