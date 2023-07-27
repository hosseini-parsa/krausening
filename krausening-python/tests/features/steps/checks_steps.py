import os
from behave import given, when, then
from krausening.properties import PropertyManager


# Scenario: Ensuring property exists in base file
@given('a property, "{key}", exists in a base properties file')
def step_impl(context, key):
    os.environ["KRAUSENING_BASE"] = "tests/resources/config/"
    context.file = "test.properties"
    context.base_properties = PropertyManager.get_instance().get_properties(
        context.file
    )
    assert key in context.base_properties


@when('the base-specific property of "{key}" is requested')
def step_impl(context, key):
    assert context.base_properties.get(key) is not None


@then('the base-specific property value of "{key}" is "{value}"')
def step_impl(context, key, value):
    assert context.base_properties.getProperty(key) == value


# Scenario: Ensure the environment variable value is returned if the properties doesn't exist in both base and extension files
@given(
    'a property, "{key}", with value "{value}", does not exist in a base or extension, but does exist as an environment variable'
)
def step_impl(context, key, value):
    os.environ["KRAUSENING_EXTENSIONS"] = "tests/resources/config_extension/"
    context.file = "test.properties"
    context.ext_properties = PropertyManager.get_instance().get_properties(context.file)

    # we know `key` not in the base / extension, hence null-value returned
    none = "None"
    null_value = context.ext_properties.getProperty(key, none)

    # create an environment variable for `key`
    os.environ[key] = value

    # since `key` is not in Properties, get its os.environ value instead
    true_value = context.ext_properties.getProperty(key)
    assert null_value == none and true_value == value


@when('a property we know that is not present, like "{key}", is requested')
def step_impl(context, key):
    context.result = context.ext_properties.getProperty(key)


@then('the property value for key "{key}" is returned')
def step_impl(context, key):
    assert context.result == os.environ[key]
