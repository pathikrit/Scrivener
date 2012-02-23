#!/bin/sh

sudo apt-get update

echo "Installing couchdb..."
sudo apt-get install curl couchdb
curl -X GET http://localhost:5984/

echo "Installing nodejs..."
sudo add-apt-repository ppa:chris-lea/node.js
sudo apt-get update
sudo apt-get install nodejs
curl http://npmjs.org/install.sh | sudo sh
sudo npm install -g express
sudo npm install -g nano
sudo npm install -g jade
npm install express
npm install nano
npm install jade

echo "Installing shellinabox..."
#sudo shellinaboxd -p 443 -s /:LOGIN -t --css ~/Downloads/white-on-black.css

echo "Installing maven..."
sudo add-apt-repository ppa:natecarlson/maven3
sudo apt-get update && sudo apt-get install maven3

echo "Building project..."
cd ../../
export M2_HOME=/usr/local/apache-maven/apache-maven-3.0.4
export M2=$M2_HOME/bin.
export PATH=$M2:$PATH.
mvn --version
mvn clean install

echo "In IntelliJ please enable nodejs plugin and jslint"

