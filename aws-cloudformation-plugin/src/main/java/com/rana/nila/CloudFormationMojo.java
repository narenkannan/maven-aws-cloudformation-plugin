package com.rana.nila;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.OnFailure;

public interface CloudFormationMojo {

    AmazonCloudFormation getAmazonCloudFormation();

    String getStackName();

    String getClientRequestToken();

    boolean getEnableTerminationProtection();

    OnFailure onFailure();

    String getRoleARN();

    Integer getTimeoutInMinutes();

    List<String> getCapabilities();

    Map<String, String> getParameters();

    File getTemplate();

    int getDelayBeforeNextRetry();

    boolean isUpdateIfExists();

}