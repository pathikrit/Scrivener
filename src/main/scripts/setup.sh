#!/bin/sh

sudo apt-get update

echo "Installing maven..."
sudo add-apt-repository ppa:natecarlson/maven3
sudo apt-get update && sudo apt-get install maven3

echo "Building project..."
cd ../../../
export M2_HOME=/usr/local/apache-maven/apache-maven-3.0.3
export M2=$M2_HOME/bin.
export PATH=$M2:$PATH.
mvn --version
mvn clean install

echo "Installing couchdb..."
sudo aptitude install curl couchdb
curl -X GET http://localhost:5984/

echo "Installing nodejs..."
sudo add-apt-repository ppa:chris-lea/node.js
sudo apt-get update
sudo apt-get install nodejs
curl http://npmjs.org/install.sh | sudo sh
sudo npm install -g express
sudo npm install -g nano

echo "Installing shellinabox..."


