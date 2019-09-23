package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class HandlerHelper {
    static ProgressEvent<ResourceModel, CallbackContext> putDestination(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final ReadHandler readHandler = new ReadHandler();

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
                ClientBuilder.getClient()::putDestination);
            proxy.injectCredentialsAndInvokeV2(Translator.translateToPutPolicyRequest(model),
                ClientBuilder.getClient()::putDestinationPolicy);
            return readHandler.handleRequest(proxy, request, callbackContext, logger);
        } catch (final ResourceNotFoundException e) {
            throw new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
        }
    }
}
