# Obligations

## 1. Obligation YAML
### 1.1. Obligations
Start writing Obligation YAML with:
~~~yaml
obligations:
    - obligation:
    ...
    - obligation:
~~~
There can be zero or more obligations per file, each one created with `obligation:`

### 1.2 Obligation
~~~yaml
label:
rules:
  - rule:
  ...
  - rule:
~~~
- _label_ - A label to give the obligation.  If one is not specified, then a random value will be used.
- _rules_ - Contains a set of zero or more rules.

### 1.3 Rule
~~~yaml
label:
event:
response:
~~~
- _rule_ -  Add a new rule to the obligation
- _label_ - A label to give the rule.  If one is not specified a random value will be used.
- _event_ - The event pattern for this rule.
- _response_ - The response to the event.

#### 1.3.1 Event
~~~yaml
subject:
policy_class:
operations:
target:
~~~
##### 1.3.1.1 Subject
The subject of the obligation.  This can be a specific user, any user, or any user from a set of user attributes.
~~~yaml
user:
any_user:
process:
~~~
- _user_ - Specify a user with a name, type, and properties.  All of the fields are optional and if none are present, it will be treated as "any user".
  ~~~yaml
   name:
   type:
   properties:
   ~~~
   _Example_:
   ~~~yaml
   name: "user123"
   type: "U"
   properties: 
     prop1: "value1"
   ~~~
- _any_user_ - 

##### 1.3.1.2 Policy Class

##### 1.3.1.3 Operations

##### 1.3.1.4 Target


