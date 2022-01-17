package io.kaoto.backend.deployment;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.kaoto.backend.model.deployment.Integration;
import io.kaoto.backend.model.deployment.kamelet.KameletBinding;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.ConstructorException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ClusterService {

    private KubernetesClient kubernetesClient;

    @Inject
    public void setKubernetesClient(final KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public List<Integration> getIntegrations() {
        List<Integration> res = new ArrayList<>();
        final var resources =
                kubernetesClient.resources(KameletBinding.class)
                .list().getItems();
        for (KameletBinding integration : resources) {
            Integration i = new Integration();
            i.setName(integration.getMetadata().getName());
            i.setRunning(true);
            i.setResource(integration);
            res.add(i);
        }
        return res;
    }

    public boolean start(final String input) {
        try {
            Yaml yaml = new Yaml(new Constructor(KameletBinding.class));
            KameletBinding binding = yaml.load(input);
            if (binding.getMetadata() == null) {
                binding.setMetadata(new ObjectMeta());
            }
            final var name = binding.getMetadata().getName();
            if (name == null || name.isEmpty()) {
                binding.getMetadata().setName(
                        "integration-" + System.currentTimeMillis());
            }
            return start(binding);
        } catch (ConstructorException e) {
            return false;
        }
    }

    public boolean start(final KameletBinding binding) {

        try {
            kubernetesClient.resources(KameletBinding.class)
                    .createOrReplace(binding);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean stop(final Integration i) {
        return kubernetesClient.resources(KameletBinding.class)
                .withName(i.getName())
                .delete();
    }
}