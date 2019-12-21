package com.rana.nila;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "describe")
public class DescribeStackMojo extends AbstractCloudFormationMojo<DescribeStackMojo> {

    public DescribeStackMojo() {
        super();
    }

    public DescribeStackMojo(AmazonCloudFormation amazonCloudFormation) {
        super(amazonCloudFormation);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        waitForCompleteAndDescribe();
    }

    @Override
    public Waiter<DescribeStacksRequest> defineStackCompleteAction(AmazonCloudFormationWaiters waiters) {
        return waiters.stackExists();
    }

}