package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;


public class ApiStepDefinitions {

    Response response;

    @Given("the API is up and running")
    public void the_api_is_up_and_running() {
        // Optional: Can include a health check here
    }

    @When("I send a POST request to {string} with body:")
    public void i_send_a_get_request_to(String url, String body) {
        response = given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(url);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer expectedStatusCode) {
       assertEquals(expectedStatusCode.intValue(), response.getStatusCode());
    }
}

