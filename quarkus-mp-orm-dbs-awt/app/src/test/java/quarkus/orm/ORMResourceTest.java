package quarkus.orm;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
public class ORMResourceTest {

    @Test
    public void testDB1Entity() {
        given()
                .body("{\"field\":\"test\"}")
                .header("Content-Type", "application/json")
                .when().post("orm/entities/db1")
                .then()
                .statusCode(201);
        given()
                .when().get("orm/entities/db1")
                .then()
                .statusCode(200)
                // 3 items were imported from the importDB1.sql file + 1 POSTed
                .body("$.size()", greaterThanOrEqualTo(4))
                .and()
                .body("field", hasItem("test"));
    }

    @Test
    public void testDB2Entity() {
        given()
                .body("{\"field\":\"TEST\"}")
                .header("Content-Type", "application/json")
                .when().post("orm/entities/db2")
                .then()
                .statusCode(201);
        given()
                .when().get("orm/entities/db2")
                .then()
                .statusCode(200)
                // 3 items were imported from the importDB2.sql file + 1 POSTed
                .body("$.size()", greaterThanOrEqualTo(4))
                .and()
                .body("field", hasItem("TEST"));
    }
}
