# Keycloak Subgroup Claim Mapper

A Keycloak protocol mapper which decodes a parent group as claim and its subgroups as claim values.

## Example

For a Keycloak group and subgroups:

```
teams
  - red
  - blue
  - green

```
and a user which is member of `red` and `green`, and given the following mapper configuration on a client scope:

![keycloak-subgroup-claim-mapper-details.png](.screenshots/keycloak-subgroup-claim-mapper-details.png)

the following token claim is produced:

```
...
  "teams": [
    "red",
    "green"
  ],
...
```

## How to build

    $ mvn clean install

## How to release

Push a tag to the repository. The release action is thus performed.
