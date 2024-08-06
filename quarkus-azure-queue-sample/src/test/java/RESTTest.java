import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWithIgnoringCase;

@QuarkusTest
public class RESTTest {

    @Test
    public void test() {
        RestAssured.registerParser("text/plain", Parser.TEXT);
        given()
                .when()
                .body("Hello!")
                .when()
                .post("/azurite")
                .then()
                .statusCode(202)
                .body(startsWithIgnoringCase(String.valueOf(Calendar.getInstance().get(Calendar.YEAR))));
    }
}
