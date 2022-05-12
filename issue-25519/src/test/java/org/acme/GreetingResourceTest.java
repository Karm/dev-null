package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello, test!"));
    }

    @Test
    public void testUsersEndpoint() {
        given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body(is("[user_1, user_2]"));
    }

}