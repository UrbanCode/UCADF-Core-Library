#!/bin/bash
sed -i "s/ucdUrl.*/ucdUrl=`echo $UCD_SERVER_URL | sed 's@\/@\\\/@g'`/g" /ucadf/UCADF-Store/Instances/example/instance.properties
sed -i "s/ucdAuthToken.*/ucdAuthToken=`echo $UCD_AUTH_TOKEN | sed 's@\/@\\\/@g'`/g" /ucadf/UCADF-Store/Instances/example/instance.secure.properties 
sed -i "s/ucdAuthToken.*/ucdAuthToken=`echo $UCD_AUTH_TOKEN | sed 's@\/@\\\/@g'`/g" /ucadf/UCADF-Store/Instances/example/ABC-UCADF-Package.secure.properties 
cat /ucadf/UCADF-Store/Instances/example/instance.properties

#temporary workarounds
java -jar /opt/ucadf/udclient.jar -weburl https://ucd:8443 -username PasswordIsAuthToken -password '{"token": "'$UCD_AUTH_TOKEN'"}' createGroup -group uc_support -authorizationRealm "Internal Security"
java -jar /opt/ucadf/udclient.jar -weburl https://ucd:8443 -username PasswordIsAuthToken -password '{"token": "'$UCD_AUTH_TOKEN'"}' createTeam -team "Public Viewer"

#Still need to create role manually and upload plugin

ucadfclient -f /ucadf/UCADF-Store/Development/ABC-UCADF/UCADF-Package/Actions/AbcUcAdfPush.yml -Djavax.net.debug=all -DucAdfInstance=example -DpackageVersion=1.0.0 -DpackageDir=$ABC_PACKAGEDIR