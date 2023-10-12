# anduril-web-docker

## How to use

You must set the following environment variables:

* SERVER_PORT=8080
* DOCKER_VOLUME=/your/selected/folder !!! Pick any folder, like /home/youruser/anduril Make sure this folder exists and is empty !!!
* DOCKER_IMAGE=siterelenby/anduril-builder:latest
* DOCKER_SOCKET=unix:///var/run/docker.sock
* GITHUB_BASEREPO=https://github.com/SiteRelEnby/anduril2/archive/refs/heads/main.zip

You must have docker installed and running to mount the docker socket.

You must have java installed.

You can now start the application jar and use the webinterface (Open your browser and enter: YOUR_SERVER_IP:YOUR_SELECTED_PORT_FROM_ABOVE)

## How to set an environment variable on ubuntu

1. nano ~/.bash_profile
2. Add: export THE_VARIABLE=THE_VALUE at the end of the file for each variable listed, separated by a new line.

## How to install docker on ubuntu

See https://docs.docker.com/engine/install/ubuntu/

## How to install java on ubuntu

1. apt-get install openjdk-17-jre-headless -y

## How to start the application jar

1. java -jar <filename>.jar