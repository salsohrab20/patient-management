import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatientIntegrationTest {

    public static final String INVALIDTOKEN = "invalidtoken";

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Order(1)
    @Test
    public void shouldNotReturnPatientsWithNullToken() {


        given()
                .header("Authorization", "Bearer " + null)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401)
        ;


    }

    @Order(2)
    @Test
    public void shouldNotReturnPatientsWithInvalidToken() {


        given()
                .header("Authorization", "Bearer " + INVALIDTOKEN)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401)
        ;


    }


    @Order(3)
    @Test
    public void shouldReturnPatientsWithValidToken() {

        String validToken = getValidToken();

        given()
                .header("Authorization", "Bearer " + validToken)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());


    }

    @Order(4)
    @Test
    public void shouldReturn429AfterLimitExceeded() throws InterruptedException {
        // 429 - Too many request

        String validToken = getValidToken();
        int requestTotal = 10;
        int tooManyRequests = 0;

        for (int i = 1; i <= requestTotal; i++) {
            Response response = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + validToken)
                    .get("/api/patients");

            System.out.printf("Request %d -> Status: %d%n ", i, response.statusCode());

            if (response.statusCode() == 429) {
                tooManyRequests++;
            }

            Thread.sleep(50);
        }
        assertTrue(tooManyRequests >= 1,
                "Expected at least 1 request to be rate limited ");


    }


    private static String getValidToken() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
        return token;
    }

}
