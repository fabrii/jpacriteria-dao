package io.quarkus.playground;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import java.util.logging.Logger;

@QuarkusTest
public class EntityResourceTest {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

    @Test
    public void test1() {

        given().contentType("application/json")
                .when().get("/entity/1/")
                .then()
                .statusCode(200);
        
        
        given().contentType("application/json")
                .when().get("/entity/2/")
                .then()
                .statusCode(200);
        
        
        given().contentType("application/json")
                .when().get("/entity/3/")
                .then()
                .statusCode(200);
        
        given().contentType("application/json")
                .when().get("/entity/4/")
                .then()
                .statusCode(200);
        
        given().contentType("application/json")
                .when().get("/entity/5/")
                .then()
                .statusCode(404);

    }
    
    
    @Test
    public void test2() {

        given().contentType("application/json")
                .when().get("/entity/search")
                .then()
                .statusCode(200).log().all();
        

    }

}
