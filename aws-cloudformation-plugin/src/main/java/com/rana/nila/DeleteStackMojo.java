package com.rana.nila;

import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "delete")
public class DeleteStackMojo extends AbstractCloudFormationMojo<DeleteStackMojo> {

    public DeleteStackMojo() {
        super();
    }

    public DeleteStackMojo(CloudFormationMojo cloudFormationMojo) {
        super(cloudFormationMojo);
    }

    public Waiter<DescribeStacksRequest> defineStackCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackDeleteComplete();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log.info(String.format("wating to complete delete stack :%s", stackName));
        DeleteStackRequest request = new DeleteStackRequest().withStackName(stackName);
        amazonCloudFormation.deleteStack(request);
        final WaiterParameters<DescribeStacksRequest> waiterParameters = WaiterParametersBuilder
                .getWaiterParameters(stackName);
        amazonCloudFormation.waiters().stackDeleteComplete().run(waiterParameters);
        log.info(String.format("completed delete stack :%s", stackName));
    }
}