package com.rana.nila;

import java.io.IOException;
import java.nio.file.Files;

import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackResult;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "update")
public class UpdateStackMojo extends AbstractCloudFormationMojo<UpdateStackMojo> {

    public UpdateStackMojo() {
        super();
    };

    public UpdateStackMojo(final CloudFormationMojo cloudFormationMojo) {
        super(cloudFormationMojo);
    }

    @Override
    public Waiter<DescribeStacksRequest> defineStackCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackUpdateComplete();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log.info(String.format("Stack update with name %s, Client request token: %s ", stackName, clientRequestToken));
        try {
            UpdateStackRequest request = new UpdateStackRequest().withStackName(stackName)
                    .withTemplateBody(new String(Files.readAllBytes(template.toPath())))
                    .withParameters(getParameterList()).withCapabilities(capabilities);
            final UpdateStackResult result = amazonCloudFormation.updateStack(request);
            log.info(String.format("Stack update request submitted successfully with stack Id: %s",
                    result.getStackId()));
            waitForCompleteAndDescribe();
        } catch (AmazonCloudFormationException e) {
            if (e.getStatusCode() == 400 && e.getErrorCode().equals("ValidationError")
                    && e.getMessage().startsWith("No updates are to be performed.")
                    && e.getServiceName().equals("AmazonCloudFormation")) {
                log.info(e.getLocalizedMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}