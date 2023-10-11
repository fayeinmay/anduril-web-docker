# anduril-web-docker

## How to use

You must set the following environment variables:

* SERVER_PORT=8080
* DOCKER_VOLUME=/path/to/volume !!! Make sure this folder is empty !!!
* DOCKER_IMAGE=siterelenby/anduril-builder:latest
* DOCKER_SOCKET=unix:///var/run/docker.sock
* GITHUB_BASEREPO=https://github.com/SiteRelEnby/anduril2/archive/refs/heads/main.zip

You must have docker installed and running to mount the docker socket.

You may use tcp://ip:2375 when using an external docker socket.

You can now start the application jar and use the webinterface.