# anduril-web-docker

## How to use

Please do not use root for any of the following steps.

You must set the following environment variables:

* SERVER_PORT=8080
* DOCKER_VOLUME=/your/selected/folder !!! Pick any folder, like /home/youruser/anduril Make sure this folder exists and is empty !!!
* DOCKER_IMAGE=siterelenby/anduril-builder:latest
* GITHUB_BASEREPO=https://github.com/SiteRelEnby/anduril2/archive/refs/heads/main.zip

You must have docker installed and running to mount the docker socket.

Your user account must have permissions to access docker.

You must have java installed.

You can now start the application jar and use the webinterface (Open your browser and enter: YOUR_SERVER_IP:YOUR_SELECTED_PORT_FROM_ABOVE)

## How to set an environment variable on ubuntu

1. nano ~/.bash_profile
2. Add: export THE_VARIABLE=THE_VALUE at the end of the file for each variable listed, separated by a new line.
3. source ~/.bash_profile

## How to install docker on ubuntu

See https://docs.docker.com/engine/install/ubuntu/

## How to allow your user account to access docker

1. sudo usermod -a -G docker youruser
2. Close and reopen your ssh session or if you are not using a server, log off and log in again

## How to install java on ubuntu

1. sudo apt-get install openjdk-17-jre-headless -y

## How to start the application jar

1. Adjust the following commands depending on the actual version you're going to use. All version links can be found on https://github.com/fayeinmay/anduril-web-docker/releases
2. wget https://github.com/fayeinmay/anduril-web-docker/releases/download/1.0.0/anduril-web-docker-backend-0.0.1-SNAPSHOT.jar
3. java -jar anduril-web-docker-backend-0.0.1-SNAPSHOT.jar 