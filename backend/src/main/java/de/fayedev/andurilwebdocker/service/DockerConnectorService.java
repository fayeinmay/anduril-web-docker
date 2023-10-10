package de.fayedev.andurilwebdocker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import de.fayedev.andurilwebdocker.model.AndurilFile;
import de.fayedev.andurilwebdocker.util.NameHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DockerConnectorService {

    private final ApacheDockerHttpClient apacheDockerHttpClient = new ApacheDockerHttpClient.Builder().dockerHost(URI.create("unix:///var/run/docker.sock")).build();
    private final DockerClient dockerClient = DockerClientImpl.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build(), apacheDockerHttpClient);
    private final Map<String, List<String>> fileBuildLogs = new HashMap<>();
    @Value("${docker.volume}")
    private String dockerVolume;
    @Value("${docker.image}")
    private String dockerImage;
    @Value("docker.socket")
    private String dockerSocket; // TODO: Make modifiable

    public void build(String fileName) {
        killRunningContainers();
        startContainer(fileName);
    }

    public List<AndurilFile> populateLogs(List<AndurilFile> andurilFiles) {
        for (AndurilFile file : andurilFiles) {
            List<String> logs = fileBuildLogs.get(file.getName());

            if (logs != null) {
                file.setLogs(logs);
            }
        }

        return andurilFiles;
    }

    private void startContainer(String fileName) {
        try {
            fileBuildLogs.remove(fileName);

            dockerClient.pullImageCmd(dockerImage).exec(new PullImageResultCallback()).awaitCompletion();
            String id = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withBinds(Bind.parse(dockerVolume + "/anduril2-main" + ":/src"))
                            .withAutoRemove(true))
                    .withCmd(NameHelper.convertNameToDockerArgument(fileName))
                    .exec()
                    .getId();
            dockerClient.startContainerCmd(id).exec();


            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(id).withStdErr(true).withFollowStream(true);

            logContainerCmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame item) {
                    fileBuildLogs.computeIfAbsent(fileName, k -> new ArrayList<>()).add(item.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Docker error: " + e);
            Thread.currentThread().interrupt();
        }
    }

    private void killRunningContainers() {
        List<Container> containers = dockerClient.listContainersCmd().exec();

        for (Container container : containers) {
            if (container.getImage().equals(dockerImage)) {
                dockerClient.stopContainerCmd(container.getId()).exec();
                dockerClient.removeContainerCmd(container.getId()).exec();
            }
        }
    }

}
