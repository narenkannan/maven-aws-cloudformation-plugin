package com.rana.nila;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.PollingStrategy;
import com.amazonaws.waiters.PollingStrategyContext;
import com.amazonaws.waiters.WaiterParameters;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "delete")
public class DeleteStackMojo extends AbstractMojo {

    Log log = getLog();

    @Parameter(alias = "StackName", property = "project.artifactId")
    String stackName;

    @Parameter(alias = "shouldRetry")
    int shouldRetry = 10;

    @Parameter(alias = "delayBeforeNextRetry")
    int delayBeforeNextRetry=5000;

    @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        log.info(String.format("wating to complete delete stack :%s",stackName));
        AmazonCloudFormationWaiters waiters = AmazonCloudFormationClientBuilder.defaultClient().waiters();
        DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stackName);
        WaiterParameters<DescribeStacksRequest> waiterParameters = new WaiterParameters<>(request);
        waiterParameters.withPollingStrategy(new PollingStrategy(retryStrategy, delayStrategy));
        waiters.stackDeleteComplete().run(waiterParameters);
        log.info(String.format("completed delete stack :%s",stackName));
    }

    private PollingStrategy.RetryStrategy retryStrategy = new PollingStrategy.RetryStrategy(){
        @Override
        public boolean shouldRetry(PollingStrategyContext pollingStrategyContext) {
            log.info("retries attempted: "+ pollingStrategyContext.getRetriesAttempted());
            return true;
        }
    };

    private PollingStrategy.DelayStrategy delayStrategy = new PollingStrategy.DelayStrategy(){
        @Override
        public void delayBeforeNextRetry(PollingStrategyContext pollingStrategyContext)
                throws InterruptedException {
                    pollingStrategyContext.wait(delayBeforeNextRetry);
        }
    };
}