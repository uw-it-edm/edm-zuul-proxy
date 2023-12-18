# edm-zuul-proxy
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6373db7b6557414f89a28cb57a374c38)](https://app.codacy.com/app/uw-it-edm/edm-zuul-proxy?utm_source=github.com&utm_medium=referral&utm_content=uw-it-edm/edm-zuul-proxy&utm_campaign=Badge_Grade_Settings)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=uw-it-edm/edm-zuul-proxy)](https://dependabot.com)

develop: [![Build Status](https://travis-ci.org/uw-it-edm/edm-zuul-proxy.svg?branch=develop)](https://travis-ci.org/uw-it-edm/edm-zuul-proxy) [![Coverage Status](https://coveralls.io/repos/github/uw-it-edm/edm-zuul-proxy/badge.svg?branch=develop)](https://coveralls.io/github/uw-it-edm/edm-zuul-proxy?branch=develop)
master: [![Build Status](https://travis-ci.org/uw-it-edm/edm-zuul-proxy.svg?branch=master)](https://travis-ci.org/uw-it-edm/edm-zuul-proxy) [![Coverage Status](https://coveralls.io/repos/github/uw-it-edm/edm-zuul-proxy/badge.svg?branch=master)](https://coveralls.io/github/uw-it-edm/edm-zuul-proxy?branch=master)

# Setup for Local Developement
## Set up Personal Access Token (PAT)
A PAT is required to access github packages from your local machine.
- Create your (classic) PAT, if not done so already. See [managing your personal access tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) for details.
- Authorize your PAT. See [authorizing personal access token](https://docs.github.com/en/enterprise-cloud@latest/authentication/authenticating-with-saml-single-sign-on/authorizing-a-personal-access-token-for-use-with-saml-single-sign-on) for details.
- Store your PAT in the env variable TOKEN

## Step local DynamoDB
- Use docker to boot a fake dynamodb 
    ```
    docker run -p 8123:8000 -it --rm instructure/dynamo-local-admin
    ```

- Navigate to DB ui at http://localhost:8123/

- (Using UI) Create a table whithin the UI with 'Name'=`edm-zuul-proxy-certificate-authorization`, 'Hash Attribute Name'=`certificateName`, 'Range Attribute Name'=`methodAndURI` and 'Range Attribute Type'=`String`.

- (Using UI) Create an item in the new table with:
    ```
    {
        "authorizedProfiles": "*",
        "certificateName": "uwitconcert-dev.s.uw.edu",
        "httpMethods": "*",
        "methodAndURI": "* .*",
        "notes": "Local test",
        "uriRegex": ".*",
        "uwGroups": "*"
    }
    ```

## Setup local forwarding server

The goal of edm-zuul-proxy is to forward requests to different end-points (called routes by zuul). To simplify testing a server is setup that will receive and 'echo' the requests back to the caller.

```
npx http-echo-server 12345
```

## Run local zuul proxy

- Copy contents of `config/application-example.yml` into a new file `config/application-local.yml`.
- Update setting `gws.keystoreLocation` with local path to your .jks file.
- Update setting `gws.keystorePassword` with the password of the .jks file.
- Run the project specifying the location of `config/application-local.yml` file. For example:
    ```
    SPRING_CONFIG_LOCATION=./config/application-local.yml ./gradlew bootRun
    ```


## Test local server

To test the server, make sure to provide a certificate name in the `X_CERTIFICATE_SUBJECT_NAME` header that matches the certificate that was entered in the DynamoDB table and a valid NetId for the `x-uw-act-as` header. (Note: both header names can be configured in the .yml file).

### Sample Request:
```
curl --location --request GET 'http://localhost:8080/test' \
--header 'X_CERTIFICATE_SUBJECT_NAME: uwitconcert-dev.s.uw.edu' \
--header 'x-uw-act-as: <YOUR_NET_ID>'
```

### Sample Response:
If everything is setup correctly, Zuul proxy will forward the request to http://localhost:12345/test, which will be echoed back by the http-echo-server. The response should be something like:
```
GET /test HTTP/1.1
user-agent: curl/7.64.1
accept: */*
x_certificate_subject_name: uwitconcert-dev.s.uw.edu
x-uw-act-as: YOUR_NET_ID
x-uw-authorized-profiles: *
x-forwarded-host: localhost:8080
x-forwarded-proto: http
x-forwarded-port: 8080
x-forwarded-for: 0:0:0:0:0:0:0:1
Accept-Encoding: gzip
Host: localhost:12345
Connection: Keep-Alive
```


## (OLD) CertificateManagement CLI

This will allow you to create a new entry in the dynamodb table

    java -cp build/libs/edm-zuul-proxy-1.111.0-detached-SNAPSHOT+dirty.jar -Dspring.profiles.active=your_user -Dspring.config.additional-location=/Development/Projects/edm-zuul-proxy/config -Dserver.port=0 -Dloader.main=edu.uw.edm.CertificateManagementApplication org.springframework.boot.loader.PropertiesLauncher --certName=toto --uriRegex=/content/.\* --httpMethods=GET --httpMethods=POST --uwGroups=u_maximed --uwGroups=\*
