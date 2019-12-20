package com.rana.nila;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
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

    public CreateStackMojo(AmazonCloudFormation amazonCloudFormation) {
        super(amazonCloudFormation);
    }

    @Override
    public Waiter<DescribeStacksRequest> defineMojoCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackCreateComplete();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        File[] templates = getCloudFormationTemplateFiles();
        CreateStackRequest request;
        for (File file : templates) {
            log.info(String.format("Creating Stack with name %s, Client request token: %s ", file.getName(),
                    clientRequestToken));
            try {
                //withClientRequestToken(clientRequestToken)
                request = new CreateStackRequest().withStackName(stackName)
                        .withTemplateBody(new String(Files.readAllBytes(file.toPath())))
                        .withParameters(getParameters())
                        .withEnableTerminationProtection(enableTerminationProtection).withOnFailure(onFailure)
                        .withTimeoutInMinutes(timeoutInMinutes).withCapabilities(capabilities);
                final CreateStackResult result = amazonCloudFormation.createStack(request);
                log.info(String.format("Stack creation request submitted successfully with stack Id: %s",
                        result.getStackId()));
                describeStackResources();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }











}