package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.json.JSONObject;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;


public class ApiStepDefinitions {

    Response response;

    @Given("the API is up and running")
    public void the_api_is_up_and_running() {
        baseURI = "http://localhost:4004";
    }


    @When("I send a POST request to {string} with email {string} and password {string}")
    public void i_send_a_post_request_to_with_email_and_password(String url, String email, String password)  {

       try {
           JSONObject body = new JSONObject();
           body.put("email", email);
           body.put("password", password);

           response = given()
                   .header("Content-Type", "application/json")
                   .body(body.toString())
                   .post(url);
       }
       catch (Exception e) {

       }
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer expectedStatusCode) {
       assertEquals(expectedStatusCode.intValue(), response.getStatusCode());
    }
}

