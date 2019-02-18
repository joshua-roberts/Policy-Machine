
# 1. Obligations Overview

# 2. Obligation YAML
Start an Obligation YAML script with:
```yaml
obligations:
    - obligation:
    ...
    - obligation:
```
There can be zero or more obligations per file, each one created with `obligation:`

##  Obligation
```yaml
label:
rules:
  - rule:
  ...
  - rule:
```
- **_label_** - A label to give the obligation.  If one is not specified, then a random value will be used.
- **_rules_** - Contains a set of zero or more rules.

##  Rule
```yaml
rule:
  label:
  event:
  response:
```
- **_label_** - A label to give the rule.  If one is not specified a random value will be used.
- **_event_** - The event pattern for this rule.
- **_response_** - The response to the event.
## Common Elements
First, some common elements that will be used throughout this specification.
### user
```yaml
user:
  name:
  properties:
  func_...:
```
Specifies a User with the given name and properties.  It is possible to specify a group of users that share a property by omitting the name field and adding a property that they share.  A function can be used to specify a user so long as the function returns a user or set of users.

_Example:_
```yaml
user:
  name: "aUser"
user: # all users who have the property key=sharedProperty
  properties:
    key: "sharedProperty"  
###
user:
  func_current_user:
``` 
### user_attribute
```yaml
user_attribute:
  name:
  properties:
  func_...:
```
Specifies a user attribute with the given name and properties. If multiple user attributes share a name, then this element will represent all of those user attributes at processing time.  Use the properties field to differentiate user attributes with the same name. A function can be used to specify a user_attribute so long as the function returns a user attribute or a set of user attributes.

_Example:_
```yaml
user_attribute:
  name: "uaName"
  properties:
    key: "value"  
###
user_attribute:
  func_uattrs_of_current_user:
``` 
### function
A function refers to a previously defined function that is supported by the Policy Machine Event Processing Point (EPP).  A list of valid functions, as well as tutorial on how to add functions can be found [here](#functions).  Every function must have the prefix `func_` this will allow the parser to know it's found a function, and to look for a way to parse it. 

_Example_
```yaml
func_FUNCTION_NAME:
  - arg1_name: "foo"
  - arg2_name:
    func_ANOTHER_FUNCTION:
  - arg3_name:
    user_attribute:
      name: "uaName"
```
## Event Pattern
```yaml
event:
  subject:
  policy_class:
  operations:
  target:
```
The Event Pattern specifies an event involving the policy elements of the Policy Machine.  An example is a user performing a read operation on an object.  This is called an access event, which is the primary focus of obligations as described in the NGAC standard. An access event has four components: The subject, policy class, operations, and target.  All of these are optional, but omitting them will have different consequences, which will be described in the sections below.

While the Policy Machine focuses on access events, it is possible to extend the functionality of the Event Pattern to other events such as time.  The section [How to Extend the Event Pattern](#how-to-extend-the-event-pattern) section provides a tutorial on how this is possible with the Policy Machine.

### Subject
```yaml
subject:
  user:
  any_user:
  process:
```
The subject specification can be a user, any user, any user from a set of users and/or user attributes, or a process.  If the subject is omitted than all events will match this component of an access event.

#### user
See [user](#user)
#### any_user
```yaml
- user:
- user_attribute:
- func_...:
```
If left empty, this event pattern will match any user.  The `any_user` element also accepts an array of `user`, `user_attribute`, and `func_...`.

_Example:_
```yaml
any_user: # any user
###
any_user: # any user in UAttr123 and user123
  - user_attribute:
    name: "UAttr123"
    properties: 
      key: "value"
  - user:
    name: "user123"
###
any_user: 
  func_a_function: # a function that returns a set of entities
```
#### process
```yaml
process: # can be a string value
  fun...: # or it can be a function
```
A process is optional, and if omitted than it will be ignored.  A process can be a string value (ex: 'a_process_id') or it an be a function such as `func_current_process`. If the process element is present but is empty than any process will match this pattern.

_Example:_
```yaml
process: "a_process_id" # pattern matches the process with "a_process_id" as it's ID.
###
process: # any event with a process ID present
###
process:
  func_current_process: 
```

### policy class
```yaml
policy_class:
  name:
  any:
  each:
```
The policy class specification can specify a particular policy class with a given name, any policy class, any policy class from a set, all policy classes from a set, or all policy classes.

_Example_
```yaml
policy_class: # policy class PC1
  name: "PC1"
###
policy_class: # any policy class
  any:
###
policy_class: # PC1 or PC2
  any:
    - "PC1"
    - "PC2"
###
policy_class: # PC1 and PC2
  each:
    - "PC1"
    - "PC2"
###
policy_class: # all policy classes
```
### Operations
```yaml
operations:
  - "operation_name"
```
The operations specification is just a string array of operation names.  If the array is not empty than any event with any of the listed operations will match this component of the pattern.  If the array is empty than any operation will match this pattern.

_Example:_
```yaml
operations:
  - "read"
  - "write"
```
### Target
```yaml
target:
  policy_element:
  - policy_element:
  in:
```
The target specification can be a specific policy element, any policy element, any policy element that is contained in other policy elements, or any policy element from a set of policy elements. All of these elements are optional, and if all are omitted then any event will match this component of the pattern. There are two means by which to express the target of an event.  The first is by using `policy_element` and `in` to express the target policy element(s) and optionally which containers they are assigned to. The second us by using an array of `policy_element`.  In this case an event will match this component of the pattern if the target of the event exists in the array of policy elements. Mixing these two cases will cause a parsing error. 

#### Policy Element
```yaml
policy_element:
  name:
  type:
  properties:
```
A policy element can be a user, user attribute, object, or object attribute.  If more than one policy element matches the given parameters, than the policy element component will be a set of elements. If the element is empty, then any element will match this component of the target specification.
#### In
```yaml
in:
  any:
    - policy_element:
  each:
    - policy_element:
```
The `in` clause of the policy element specification accepts `any` or `each`.  Both accept arrays, and will match an event if the target specification policy element is in any/each of the listed policy elements.

#### Examples
```yaml
# any example
target: # same as saying "any element that is assigned to pe1 and/or pe2"
  policy_element: # any policy element
  in: # the policy_element is in pe1 AND/OR pe2
    any:
      - policy_element:
        name: "pe1"
        type: "OA"
      - policy_element:
        name: "pe2"
        type: "OA"
###
# each example
target: # same as saying "any element that is assigned to pe1 and pe2""
  policy_element: # any policy element
  in: # the policy element is in pe1 AND pe2
    each:
      - policy_element:
        name: "pe1"
        type: "OA"
      - policy_element:
        name: "pe2"
        type: "OA"
###
# array example
target: # same as saying "pe1 or pe2"
  - policy_element:
      name: "pe1"
  - policy_element:
      name: "pe2"
```

## Functions
### Predefined Functions
1. current_user
2. current_process
### How to add a function
## How to Extend the Event Pattern


