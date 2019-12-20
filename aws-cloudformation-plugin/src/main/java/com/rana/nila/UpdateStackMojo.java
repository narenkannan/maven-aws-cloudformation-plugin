package com.rana.nila;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
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

    public UpdateStackMojo(final AmazonCloudFormation amazonCloudFormation) {
        super(amazonCloudFormation);
    }
    
    @Override
    public Waiter<DescribeStacksRequest> defineMojoCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackUpdateComplete();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File[] templates = getCloudFormationTemplateFiles();
        UpdateStackRequest request;
        for (File file : templates) {
            log.info(String.format("Stack update with name %s, Client request token: %s ", file.getName(),
                    clientRequestToken));
            try {
                request = new UpdateStackRequest().withStackName(stackName)
                        .withTemplateBody(new String(Files.readAllBytes(file.toPath())))
                        .withParameters(getParameters())
                        .withCapabilities(capabilities);
                final UpdateStackResult result = amazonCloudFormation.updateStack(request);
                log.info(String.format("Stack update request submitted successfully with stack Id: %s",
                        result.getStackId()));
                describeStackResources();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}