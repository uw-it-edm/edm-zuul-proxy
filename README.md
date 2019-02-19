# edm-zuul-proxy
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6373db7b6557414f89a28cb57a374c38)](https://app.codacy.com/app/uw-it-edm/edm-zuul-proxy?utm_source=github.com&utm_medium=referral&utm_content=uw-it-edm/edm-zuul-proxy&utm_campaign=Badge_Grade_Settings)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=uw-it-edm/edm-zuul-proxy)](https://dependabot.com)

develop: [![Build Status](https://travis-ci.org/uw-it-edm/edm-zuul-proxy.svg?branch=develop)](https://travis-ci.org/uw-it-edm/edm-zuul-proxy) [![Coverage Status](https://coveralls.io/repos/github/uw-it-edm/edm-zuul-proxy/badge.svg?branch=develop)](https://coveralls.io/github/uw-it-edm/edm-zuul-proxy?branch=develop)
master: [![Build Status](https://travis-ci.org/uw-it-edm/edm-zuul-proxy.svg?branch=master)](https://travis-ci.org/uw-it-edm/edm-zuul-proxy) [![Coverage Status](https://coveralls.io/repos/github/uw-it-edm/edm-zuul-proxy/badge.svg?branch=master)](https://coveralls.io/github/uw-it-edm/edm-zuul-proxy?branch=master)


## Run Locally

Use docker to boot a fake dynamodb 
    
    docker run -p 8123:8000 -it --rm instructure/dynamo-local-admin

You'll find a db ui at http://localhost:8123/



## CertificateManagement CLI

This will allow you to create a new entry in the dynamodb table

    java -cp build/libs/edm-zuul-proxy-1.111.0-detached-SNAPSHOT+dirty.jar -Dspring.profiles.active=your_user -Dspring.config.additional-location=/Development/Projects/edm-zuul-proxy/config -Dserver.port=0 -Dloader.main=edu.uw.edm.CertificateManagementApplication org.springframework.boot.loader.PropertiesLauncher --certName=toto --uriRegex=/content/.\* --httpMethods=GET --httpMethods=POST --uwGroups=u_maximed --uwGroups=\*