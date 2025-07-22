#!/bin/sh
# make the private keystore used by the server
#https://stackoverflow.com/questions/50928061/certificate-for-localhost-doesnt-match-any-of-the-subject-alternative-names
#https://ultimatesecurity.pro/post/san-certificate/
keytool -genkeypair -keyalg RSA -keysize 2048 -validity 3650 \
-ext "SAN:c=DNS:localhost,IP:127.0.0.1" \
-dname "CN=localhost,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=Unknown" \
-keystore keystore.p12  -alias https-hello \
-storepass password

# make the public truststore used by the client
# Building Example Trusted Certificate Authority
# certs only, no private keys, not password protected
# PEM File is Text File with Base64 Encoded Certificate Information

openssl pkcs12 -in ./src/main/resources/keystore.p12 -passin pass:password \
-out ./target/certs.pem \
-clcerts -nokeys -nodes  


# test
# Curl Accepting the Server Certificate After Added to Trusted Sources
curl -v -X GET https://localhost:8443/api/authn/hello?name=jim -u user:password 
--cacert ./target/certs.pem