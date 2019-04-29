
# Obligations Overview
Obligations provide a way to dynamically change the state of an NGAC graph in response to events. Obligations are defined using a yaml structure defined below, and are processed at the time an event occurs.

# Obligation YAML

## Common Elements
### Nodes
A node represents a node in an NGAC graph. A node has a name, type, and properties. A node can also be derived from a function.
```yaml
name:
type:
properties:
---
function:
```

### function
A function refers to a previously defined function that is supported by the Policy Machine Event Processing Point (EPP).  A list of valid functions, as well as tutorial on how to add functions can be found [here](#functions).

_Example_
```yaml
function:
  name:
  args:
    - name: "foo"
      value: "bar"
```
A function has a name and a list of arguments. The arguments are a list of name, value pairs, the name indicating the name of the argument and the value indicating the value of the argument.


##  Obligation
There is one obligation allowed per yaml file.
```yaml
label:
rules:
```
- **_label_** - A label to give the obligation.  If one is not specified, then a random value will be used.
- **_rules_** - Contains a set of zero or more rules.

##  Rule
```yaml
label:
event:
response:
```
- **_label_** - A label to give the rule.  If one is not specified a random value will be used.
- **_event_** - The event pattern for this rule.
- **_response_** - The response to the event.

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
any_user:
```
The `any_user` element accepts an array of `nodes`, but if left empty, this event pattern will match any user.

_Example:_
```yaml
any_user: # any user
###
any_user: # u1 or u2
  - name: "u1"
    type: "U"
  - name: "u2"
    type: "U"
###
any_user:
  function: # a function that returns a set of entities
```

#### process
```yaml
process: # can be a string value
  function: # or it can be a function
```
A process is optional, and if omitted than it will be ignored.  A process can be a string value (ex: 'a_process_id') or it an be a function such as `current_process`. If the process element is present but is empty than any process will match this pattern.

_Example:_
```yaml
process: "a_process_id" # pattern matches the process with "a_process_id" as it's ID.
###
process: # any event with a process ID present
###
process:
  function:
    name: current_process
```

### policy class
```yaml
policy_class:
  anyOf:
  eachOf:
```
The policy class specification can specify a particular policy class with a given name, any policy class, any policy class from a set, all policy classes from a set, or all policy classes.

_Example_
```yaml
###
policyClass: # any policy class
  anyOf:
###
policyClass: # PC1 or PC2
  anyOf:
    - "PC1"
    - "PC2"
###
policyClass: # PC1 and PC2
  eachOf:
    - "PC1"
    - "PC2"
###
policyClass: # all policy classes
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
  containers:
```

The target specification can be:

1. A specific policy element
2. Any policy element
3. Any policy element that is contained in other policy elements
4. Any policy element from a set of policy elements

All of these elements are optional, and if all are omitted then any event will match this component of the pattern.

_Example:_
```yaml
# A specific policy element
target:
  - "target pe"

# Any policy element, omitting the target will behave the same
target:

# Any policy element that is contained in other policy Elements
target:
  containers:
    - "container 1"
    - "container 2"

# Any policy element from a set of policy elements
target:
  - "pe1"
  - "pe2"
```


## Functions
### Predefined Functions
1. current_user
2. current_process
### How to add a function
## How to Extend the Event Pattern
