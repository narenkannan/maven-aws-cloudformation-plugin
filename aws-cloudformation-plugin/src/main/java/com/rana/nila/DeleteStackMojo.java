package com.rana.nila;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.PollingStrategy;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "delete")
public class DeleteStackMojo extends AbstractCloudFormationMojo<DeleteStackMojo> {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log.info(String.format("wating to complete delete stack :%s", stackName));
        AmazonCloudFormationWaiters waiters = AmazonCloudFormationClientBuilder.defaultClient().waiters();
        DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stackName);
        WaiterParameters<DescribeStacksRequest> waiterParameters = new WaiterParameters<>(request);
        waiterParameters.withPollingStrategy(new PollingStrategy(retryStrategy, delayStrategy));
        waiters.stackDeleteComplete().run(waiterParameters);
        log.info(String.format("completed delete stack :%s", stackName));
    }

    @Override
    public Waiter<DescribeStacksRequest> defineMojoCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackDeleteComplete();
    }


}