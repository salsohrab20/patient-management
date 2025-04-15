import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {

    @BeforeAll
    public static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }


    @Test
    public void shouldReturnPatientsWithValidToken(){

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

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());


    }

    @Test
    public void shouldNotReturnPatientsWithNullToken(){


        given()
                .header("Authorization", "Bearer " + null)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401)
                ;


    }

    @Test
    public void shouldNotReturnPatientsWithInvalidToken(){


        given()
                .header("Authorization", "Bearer " + "invalidtoken")
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401)
        ;


    }
}
