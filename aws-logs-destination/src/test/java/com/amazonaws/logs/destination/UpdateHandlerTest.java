package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {
    private static final String PRIMARY_ID = "{\"/properties/DestinationName\":[\"DestinationName\"]}";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final Destination destination = Destination.builder()
            .destinationName("DestinationName")
            .accessPolicy("AccessPolicy")
            .roleArn("RoleArn")
            .targetArn("TargetArn")
            .build();

        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder()
            .destinations(Collections.singletonList(destination))
            .build();


        final Destination postPutDestination = Destination.builder()
            .destinationName("DestinationName")
            .accessPolicy("DifferentPolicy")
            .roleArn("DifferentArn")
            .targetArn("DifferentArn")
            .build();

        final PutDestinationResponse putResponse = PutDestinationResponse.builder()
            .destination(destination)
            .build();

        final PutDestinationPolicyResponse policyResponse = PutDestinationPolicyResponse.builder()
            .build();

        final DescribeDestinationsResponse postPutDescribeResponse = DescribeDestinationsResponse.builder()
            .destinations(postPutDestination)
            .build();

        doReturn(describeResponse, putResponse, policyResponse, postPutDescribeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .destinationName("DestinationName")
            .destinationPolicy("DifferentPolicy")
            .roleArn("DifferentArn")
            .targetArn("DifferentArn")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFound() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder()
            .destinations(Collections.emptyList())
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .destinationName("DestinationName")
            .destinationPolicy("AccessPolicy")
            .roleArn("RoleArn")
            .targetArn("TargetArn")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
