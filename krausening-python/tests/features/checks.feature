Feature: Property Externalization

  Scenario: Ensuring property exists in base file
    Given a property, "new_key", exists in a base properties file
    When the base-specific property of "new_key" is requested
    Then the base-specific property value of "new_key" is "base_value"

  Scenario: Ensure the environment variable value is returned if the properties doesn't exist in both base and extension files
    Given a property, "xyz", with value "abc", does not exist in a base or extension, but does exist as an environment variable
    When a property we know that is not present, like "xyz", is requested
    Then the property value for key "xyz" is returned