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

public abstract class AbstractCloudFormationMojo<T extends AbstractMojo> extends AbstractMojo
        implements CloudFormationMojo {

    @Parameter(alias = "stackName", property = "project.artifactId")
    String stackName;

    @Parameter(alias = "clientRequestToken")
    String clientRequestToken;// = Integer.toString(new Random().nextInt());

    @Parameter(alias = "enableTerminationProtection", defaultValue = "false")
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

    @Parameter(alias = "template", defaultValue = "cloudformation.template")
    File template;

    @Parameter(alias = "delayBeforeNextRetry")
    int delayBeforeNextRetry = 5000;

    @Parameter(alias = "updateIfExists", defaultValue = "false")
    boolean updateIfExists;

    Log log = getLog();

    public AmazonCloudFormation amazonCloudFormation;

    @Override
    public String getStackName() {
        return this.stackName;
    }

    public String getClientRequestToken() {
        return clientRequestToken;
    }

    public AmazonCloudFormation getAmazonCloudFormation() {
        return amazonCloudFormation;
    }

    @Override
    public boolean getEnableTerminationProtection() {
        return enableTerminationProtection;
    }

    @Override
    public OnFailure onFailure() {
        return onFailure;
    }

    @Override
    public String getRoleARN() {
        return roleARN;
    }

    @Override
    public Integer getTimeoutInMinutes() {
        return timeoutInMinutes;
    }

    @Override
    public List<String> getCapabilities() {
        return capabilities;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public File getTemplate() {
        return template;
    }

    @Override
    public int getDelayBeforeNextRetry() {
        return delayBeforeNextRetry;
    }

    @Override
    public boolean isUpdateIfExists() {
        return updateIfExists;
    }

    public abstract Waiter<DescribeStacksRequest> defineStackCompleteAction(AmazonCloudFormationWaiters waiters);

    public void waitForCompleteAndDescribe() throws MojoExecutionException, MojoFailureException {
        final WaiterParameters<DescribeStacksRequest> waiterParameters = WaiterParametersBuilder
                .getWaiterParameters(stackName);
        defineStackCompleteAction(amazonCloudFormation.waiters()).run(waiterParameters);
        describeStack();
    }

    private void describeStack() {
        final DescribeStackResourcesRequest describeStackResourcesRequest = new DescribeStackResourcesRequest();
        describeStackResourcesRequest.withStackName(stackName);
        final DescribeStackResourcesResult result = amazonCloudFormation
                .describeStackResources(describeStackResourcesRequest);
        final List<StackResource> stackResources = result.getStackResources();
        stackResources.stream().forEach(stackResource -> {
            log.info(String.format("%50s%50s%25s%50s", stackResource.getLogicalResourceId(),
                    stackResource.getResourceType(), stackResource.getResourceStatus(),
                    stackResource.getTimestamp()));
        });
    }

    protected List<com.amazonaws.services.cloudformation.model.Parameter> getParameterList() {
        final List<com.amazonaws.services.cloudformation.model.Parameter> parametersList = new ArrayList<com.amazonaws.services.cloudformation.model.Parameter>();
        parameters.forEach((k, v) -> {
            final com.amazonaws.services.cloudformation.model.Parameter param = new com.amazonaws.services.cloudformation.model.Parameter();
            param.setParameterKey(k);
            param.setParameterValue(v);
            parametersList.add(param);
        });
        return parametersList;
    }

    protected PollingStrategy.RetryStrategy retryStrategy = new PollingStrategy.RetryStrategy() {
        @Override
        public boolean shouldRetry(final PollingStrategyContext pollingStrategyContext) {
            log.info("retries attempted: " + pollingStrategyContext.getRetriesAttempted());
            return true;
        }
    };

    protected PollingStrategy.DelayStrategy delayStrategy = new PollingStrategy.DelayStrategy() {
        @Override
        public void delayBeforeNextRetry(final PollingStrategyContext pollingStrategyContext)
                throws InterruptedException {
            pollingStrategyContext.wait(delayBeforeNextRetry);
        }
    };

    protected File[] getCloudFormationTemplateFiles() {
        final File[] templates = template.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".template");
            }
        });
        return templates;
    }

    public AbstractCloudFormationMojo() {
        if (clientRequestToken == null)
            clientRequestToken = Integer.toString(new Random().nextInt());
        this.amazonCloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
    };

    public AbstractCloudFormationMojo(final CloudFormationMojo cloudFormationMojo) {
        this.amazonCloudFormation = cloudFormationMojo.getAmazonCloudFormation();
        this.clientRequestToken = cloudFormationMojo.getClientRequestToken();
        this.capabilities = cloudFormationMojo.getCapabilities();
        this.delayBeforeNextRetry = cloudFormationMojo.getDelayBeforeNextRetry();
        this.enableTerminationProtection = cloudFormationMojo.getEnableTerminationProtection();
        this.onFailure = cloudFormationMojo.onFailure();
        this.parameters = cloudFormationMojo.getParameters();
        this.roleARN = cloudFormationMojo.getRoleARN();
        this.stackName = cloudFormationMojo.getStackName();
        this.template = cloudFormationMojo.getTemplate();
        this.timeoutInMinutes = cloudFormationMojo.getTimeoutInMinutes();
        this.updateIfExists = cloudFormationMojo.isUpdateIfExists();
    };

}