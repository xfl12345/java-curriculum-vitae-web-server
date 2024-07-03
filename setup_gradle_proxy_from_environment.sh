#!/usr/bin/env bash

mkdir -p $HOME/.gradle
if [[ ! -e $HOME/.gradle/gradle.properties ]]; then
    touch $HOME/.gradle/gradle.properties
fi

if [[ x"${HTTPS_PROXY}" != "x" ]]; then
    echo "HTTPS_PROXY found. HTTPS_PROXY=${HTTPS_PROXY}"
    cat $HOME/.gradle/gradle.properties | grep -v "systemProp\.https\." > $HOME/.gradle/gradle.properties
    https_proxy_host_and_port=$(echo $HTTPS_PROXY | sed "s#https://##g" | sed "s#http://##g")
    echo "systemProp.https.proxyHost=$(echo $https_proxy_host_and_port | cut -d':' -f1)" >> $HOME/.gradle/gradle.properties
    echo "systemProp.https.proxyPort=$(echo $https_proxy_host_and_port | cut -d':' -f2)" >> $HOME/.gradle/gradle.properties
fi

if [[ x"${HTTP_PROXY}" != "x" ]]; then
    echo "HTTP_PROXY found. HTTP_PROXY=${HTTPS_PROXY}"
    cat $HOME/.gradle/gradle.properties | grep -v "systemProp\.http\." > $HOME/.gradle/gradle.properties
    http_proxy_host_and_port=$(echo $HTTP_PROXY | sed "s#http://##g")
    echo "systemProp.http.proxyHost=$(echo $http_proxy_host_and_port | cut -d':' -f1)" >> $HOME/.gradle/gradle.properties
    echo "systemProp.http.proxyPort=$(echo $http_proxy_host_and_port | cut -d':' -f2)" >> $HOME/.gradle/gradle.properties
fi
