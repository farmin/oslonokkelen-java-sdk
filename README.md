# oslonokkelen-java-sdk

[![Build Status](https://travis-ci.com/oslokommune/oslonokkelen-java-sdk.svg?branch=master)](https://travis-ci.com/oslokommune/oslonokkelen-java-sdk)

Java SDK for Oslonøkkelen (proof of concept)


Artefacts 
---------
The project is separated into three different artefacts

1. Messages
2. Java adapter
3. Simple CLI

### Messages 

Defines the messages that are sent between client and backend. 

### Java Adapter 

A proof of concept reference implementation of an adapter.

### Simple CLI

An opinionated approach to an adapter distributed as an executable jar file.

Travis
------
Secrets are handled according to https://docs.travis-ci.com/user/encryption-keys/

Publishing to Bintray
---------------------
Pushing to master will make Travis build and deploy a new version to Bintray. 
In order to make the version public you will have to log into Bintray manually publish it.

