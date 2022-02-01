package io.kaoto.backend.api.service.deployment.generator;

import io.kaoto.backend.api.service.deployment.generator.kamelet.KameletBindingDeploymentGeneratorService;
import io.quarkus.test.junit.QuarkusTest;
import io.kaoto.backend.model.step.Step;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
class KameletBindingDeploymentGeneratorServiceTest {

    private KameletBindingDeploymentGeneratorService parser;

    @Inject
    public void setParser(
            final KameletBindingDeploymentGeneratorService parser) {
        this.parser = parser;
    }

    @Test
    void parse() {
        List<Step> steps = new ArrayList();
        Assertions.assertEquals("", parser.parse("", steps));
        steps.add(new Step());
        Assertions.assertEquals("", parser.parse("", steps));
        steps.add(new Step());
        Assertions.assertEquals("", parser.parse("", steps));
        for (Step step : steps) {
            step.setKind("Kamelet");
        }
        Assertions.assertNotEquals("", parser.parse("", steps));
    }
}
