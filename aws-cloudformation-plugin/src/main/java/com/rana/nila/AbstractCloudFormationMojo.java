package com.rana.nila;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.PollingStrategy;
import com.amazonaws.waiters.PollingStrategyContext;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractCloudFormationMojo<T extends AbstractMojo> extends AbstractMojo {

    @Parameter(alias = "StackName", property = "project.artifactId")
    String stackName;

    @Parameter(alias = "ClientRequestToken")
    int clientRequestToken = new Random().nextInt();

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

    @Parameter(alias = "delayBeforeNextRetry")
    int delayBeforeNextRetry = 5000;

    Log log = getLog();

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public AmazonCloudFormation amazonCloudFormation;

    public AbstractCloudFormationMojo() {
        this.amazonCloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
    };

    public AbstractCloudFormationMojo(AmazonCloudFormation amazonCloudFormation){
        this.amazonCloudFormation = amazonCloudFormation;
    };

    public T withStackName(String stackName) {
        this.stackName = stackName;
        return (T)this;
    }

    public abstract Waiter<DescribeStacksRequest> defineMojoCompleteAction(AmazonCloudFormationWaiters waiters);


    public void describeStackResources( ) throws MojoExecutionException, MojoFailureException {
        WaiterParameters<DescribeStacksRequest> waiterParameters = WaiterParametersBuilder
                        .getWaiterParameters(stackName);
        defineMojoCompleteAction(amazonCloudFormation.waiters()).run(waiterParameters);
        DescribeStackResourcesRequest describeStackResourcesRequest = new DescribeStackResourcesRequest();
        describeStackResourcesRequest.withStackName(stackName);
        DescribeStackResourcesResult result = amazonCloudFormation.describeStackResources(describeStackResourcesRequest);
        List<StackResource> stackResources = result.getStackResources();
        stackResources.stream().forEach(stackResource -> {
            log.info(String.format("%50s%50s%25s%50s", stackResource.getLogicalResourceId(),
                    stackResource.getResourceType(), stackResource.getResourceStatus(),
                    stackResource.getTimestamp().toGMTString()));
        });
    }

    protected List<com.amazonaws.services.cloudformation.model.Parameter> getParameters() {
        List<com.amazonaws.services.cloudformation.model.Parameter> parametersList = new ArrayList<com.amazonaws.services.cloudformation.model.Parameter>();
        parameters.forEach((k, v) -> {
            com.amazonaws.services.cloudformation.model.Parameter param = new com.amazonaws.services.cloudformation.model.Parameter();
            param.setParameterKey(k);
            param.setParameterValue(v);
            parametersList.add(param);
        });
        return parametersList;
    }

    protected PollingStrategy.RetryStrategy retryStrategy = new PollingStrategy.RetryStrategy() {
        @Override
        public boolean shouldRetry(PollingStrategyContext pollingStrategyContext) {
            log.info("retries attempted: " + pollingStrategyContext.getRetriesAttempted());
            return true;
        }
    };

    protected PollingStrategy.DelayStrategy delayStrategy = new PollingStrategy.DelayStrategy(){
        @Override
        public void delayBeforeNextRetry(PollingStrategyContext pollingStrategyContext)
                throws InterruptedException {
                    pollingStrategyContext.wait(delayBeforeNextRetry);
        }
    };

    protected File[] getCloudFormationTemplateFiles() {
        File[] templates = templateDirectory.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".template");
            }
        });
        return templates;
    }
}