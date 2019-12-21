package com.rana.nila;

import java.io.IOException;
import java.nio.file.Files;

import com.amazonaws.services.cloudformation.model.AlreadyExistsException;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "create")
public class CreateStackMojo extends AbstractCloudFormationMojo<CreateStackMojo> {

    public CreateStackMojo() {
        super();
    }

    public CreateStackMojo(final CloudFormationMojo cloudFormationMojo) {
        super(cloudFormationMojo);
    }

    @Override
    public Waiter<DescribeStacksRequest> defineStackCompleteAction(final AmazonCloudFormationWaiters waiters) {
        return waiters.stackCreateComplete();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            log.info(String.format("Creating Stack with name %s, Client request token: %s ", template.getName(),
                    clientRequestToken));
            final CreateStackRequest request = new CreateStackRequest().withStackName(stackName)
                    .withTemplateBody(new String(Files.readAllBytes(template.toPath())))
                    .withParameters(getParameterList()).withEnableTerminationProtection(enableTerminationProtection)
                    .withOnFailure(onFailure).withTimeoutInMinutes(timeoutInMinutes).withCapabilities(capabilities);
            final CreateStackResult result = amazonCloudFormation.createStack(request);
            log.info(String.format("Stack creation request submitted successfully with stack Id: %s",
                    result.getStackId()));
            waitForCompleteAndDescribe();
        } catch (final AlreadyExistsException e) {
            if (updateIfExists) {
                log.warn(String.format("Stack with name(%s) already exists. <updateIfExists> is set to: %s", stackName,
                        updateIfExists));
                autoUpdateIfExists();
            } else {
                System.out.print("\n\n\t Add the below in configuraion for auto update request."
                        + "\n\n\t\t <updateIfExists>true</updateIfExists> \n\n");
                throw e;
            }
        } catch (final IOException | OutOfMemoryError e) {
            log.error(e.fillInStackTrace());
        } finally {
            amazonCloudFormation.shutdown();
        }
    }

    private void autoUpdateIfExists() throws MojoExecutionException, MojoFailureException {
        log.warn(String.format("Updating stack with name(%s) ...", stackName));
        new UpdateStackMojo(this).execute();
    }
}