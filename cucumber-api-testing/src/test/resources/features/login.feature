Feature: Login

  Scenario Outline: Login functionality
    Given the API is up and running
    When I send a POST request to "/auth/login" with email "<email>" and password "<password>"
    Then the response status code should be <status>

    Examples:
      | email              | password    | status |
      | testuser@test.com  | password123 | 200    |
      | testuser1@test.com | password    | 401    |

