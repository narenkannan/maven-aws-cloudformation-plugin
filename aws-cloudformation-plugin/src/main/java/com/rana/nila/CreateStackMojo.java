package com.rana.nila;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Stack;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "create-stack")
public class CreateStackMojo extends AbstractMojo {

    @Parameter(alias = "StackName", property = "project.artifactId")
    String stackName;

    @Parameter(alias = "ClientRequestToken", defaultValue = "123")
    String clientRequestToken;

    @Parameter(alias = "EnableTerminationProtection", defaultValue = "false")
    boolean enableTerminationProtection;

    @Parameter(alias = "OnFailure", defaultValue = "ROLLBACK")
    OnFailure onFailure;

    @Parameter(alias = "RoleARN")
    String roleARN;

    @Parameter(alias = "TimeoutInMinutes")
    Integer timeoutInMinutes;

    @Parameter(alias = "CAPABILITIES")
    List<String> capabilities;

    @Parameter(alias = "Parameters")
    Map<String, String> parameters;

    @Parameter(alias = "TemplateDirectory", defaultValue = "templates")
    File templateDirectory;

    Log log = getLog();

    public void execute() throws MojoExecutionException, MojoFailureException {
        File[] templates = templateDirectory.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".template");
            }
        });
        CreateStackRequest request;
        for (File file : templates) {
            log.info(String.format("Creating Stack with name %s, Client request token: %s ", file.getName(),
                    clientRequestToken));
            try {
                request = new CreateStackRequest().withStackName(stackName)
                        .withTemplateBody(new String(Files.readAllBytes(file.toPath())))
                        .withClientRequestToken(clientRequestToken).withParameters(getParameters())
                        .withEnableTerminationProtection(enableTerminationProtection).withOnFailure(onFailure)
                        .withTimeoutInMinutes(timeoutInMinutes).withCapabilities(capabilities);
                AmazonCloudFormation awsClient = AmazonCloudFormationClientBuilder.defaultClient();
                final CreateStackResult result = awsClient.createStack(request);
                log.info(String.format("Stack creation request submitted successfully with Stack Id: %s",
                        result.getStackId()));
                DescribeStacksRequest describeRequest = new DescribeStacksRequest().withStackName(stackName);
                DescribeStacksResult describeResult;
                Stack stack;
                String currentStatus = null;
                do {
                    describeResult = awsClient.describeStacks(describeRequest);
                    stack = describeResult.getStacks().get(0);
                    if (!stack.getStackStatus().equals(currentStatus)) {
                        currentStatus = stack.getStackStatus();
                        log.info("Status : " + currentStatus);
                    }
                    Thread.sleep(5000);
                } while (!currentStatus.equals("CREATE_COMPLETE") && !currentStatus.endsWith("_FAILED"));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<com.amazonaws.services.cloudformation.model.Parameter> getParameters() {
        List<com.amazonaws.services.cloudformation.model.Parameter> parametersList = new ArrayList<com.amazonaws.services.cloudformation.model.Parameter>();
        parameters.forEach((k, v) -> {
            com.amazonaws.services.cloudformation.model.Parameter param = new com.amazonaws.services.cloudformation.model.Parameter();
            param.setParameterKey(k);
            param.setParameterValue(v);
            parametersList.add(param);
        });
        return parametersList;
    }
}