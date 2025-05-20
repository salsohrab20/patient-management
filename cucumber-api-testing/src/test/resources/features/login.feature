Feature: Login

  Scenario Outline: Login functionality happy path
    Given the API is up and running
    When I send a POST request to "http://localhost:4004/auth/login" with body:
    """
    {
         "email": "<username>",
         "password": "<password>"
    }
    """
    Then the response status code should be <status>

    Examples:
      | username             | password    | status |
      | testuser@test.com    | password123 | 200    |

