package io.kaoto.backend.api.resource;

import io.kaoto.backend.api.resource.response.ViewDefinitionResourceResponse;
import io.kaoto.backend.api.service.step.parser.StepParserService;
import io.kaoto.backend.api.service.viewdefinition.ViewDefinitionService;
import io.kaoto.backend.model.step.Step;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 🐱class ViewDefinitionResource
 * 🐱relationship compositionOf ViewDefinitionService, 0..1
 *
 * This endpoint will return a list of views based on the parameters.
 *
 */
@Path("/viewdefinition")
@ApplicationScoped
@OpenAPIDefinition(
        info = @Info(
            title = "View Definition API",
            version = "1.0.0",
            description = "With this API, backend and frontend agree on what "
                    + "views and extensions can be used for the "
                    + "integration workflow.",
            contact = @Contact(
                    name = "Kaoto Team",
                    url = "https://kaoto.io"),
            license = @License(
                    name = "Apache 2.0",
                    url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
public class ViewDefinitionResource {

    private Logger log = Logger.getLogger(ViewDefinitionResource.class);

    private ViewDefinitionService viewDefinitionService;

    private StepParserService<Step> stepParserService;

    @Inject
    public void setViewDefinitionService(
            final ViewDefinitionService viewDefinitionService) {
        this.viewDefinitionService = viewDefinitionService;
    }

    @Inject
    public void setStepParserService(
            final StepParserService<Step> stepParserService) {
        this.stepParserService = stepParserService;
    }
    /*
     * 🐱method views:
     * 🐱param yaml: String
     *
     * Based on the YAML provided, offer a list of possible views
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("text/yaml")
    @Operation(summary = "Get views",
            description = "Get view definitions for a specific integration."
            + " This is an idempotent operation.")
    public ViewDefinitionResourceResponse views(
            final @RequestBody String yaml) {
        ViewDefinitionResourceResponse res =
                new ViewDefinitionResourceResponse();
        res.setSteps(stepParserService.parse(yaml));
        res.setViews(viewDefinitionService.views(yaml, res.getSteps()));
        return res;
    }


    @ServerExceptionMapper
    public Response mapException(final Exception x) {
        log.error("Error processing views definitions.", x);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error processing views definitions: "
                        + x.getMessage())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    @ServerExceptionMapper
    public Response mapException(final IllegalArgumentException x) {
        log.error("Error trying to return YAML.", x);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't parse the YAML provided: "
                        + x.getMessage())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

}
