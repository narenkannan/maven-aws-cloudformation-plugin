package com.rana.nila;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.waiters.PollingStrategy;
import com.amazonaws.waiters.PollingStrategyContext;
import com.amazonaws.waiters.WaiterParameters;

public class WaiterParametersBuilder {

    static int delayBeforeNextRetry;

    private static PollingStrategy.RetryStrategy retryStrategy = new PollingStrategy.RetryStrategy() {
        @Override
        public boolean shouldRetry(PollingStrategyContext pollingStrategyContext) {
            return true;
        }
    };

    private static PollingStrategy.DelayStrategy delayStrategy = new PollingStrategy.DelayStrategy() {
        @Override
        public void delayBeforeNextRetry(PollingStrategyContext pollingStrategyContext) throws InterruptedException {
            pollingStrategyContext.wait(delayBeforeNextRetry);
        }
    };

    static public WaiterParameters<DescribeStacksRequest> getWaiterParameters(String stackName) {
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
        WaiterParameters<DescribeStacksRequest> waiterParameters = new WaiterParameters<>(describeStacksRequest);
        waiterParameters.withPollingStrategy(new PollingStrategy(retryStrategy, delayStrategy));
        return waiterParameters;
    }

}