package org.example.cli.commands;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import org.example.cli.common.HelpOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Command(name = "receive", description = "Receive a large payload to SQS")
public class ReceiveCommand implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ReceiveCommand.class);

    @Mixin
    private HelpOption helpOption;

    @Option(names = "-bucketName", description = "The s3 bucket used for message", required = true)
    private String bucketName;
    @Option(names = "-queueName", description = "The name of the SQS queue")
    private String queueName;

    @Override
    public void run() {
        logger.info("Executing send command with bucketName = " + bucketName + ", queueName = " + queueName );

        final S3Client s3 = S3Client.builder().region(Region.EU_WEST_1).build();


        final ExtendedClientConfiguration extendedClientConfig =
                new ExtendedClientConfiguration()
                        .withLargePayloadSupportEnabled(s3, bucketName);

        SqsClient sqsClient = SqsClient.builder().region(Region.EU_WEST_1).build();
        final AmazonSQSExtendedClient sqsExtended =
                new AmazonSQSExtendedClient(sqsClient, extendedClientConfig);


        var url = sqsExtended.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).queueOwnerAWSAccountId("334423569906").build());

        // Receive the message.
        final ReceiveMessageRequest receiveMessageRequest =
                ReceiveMessageRequest.builder().queueUrl(url.queueUrl()).build();
        List<Message> messages = sqsExtended
                .receiveMessage(receiveMessageRequest).messages();

        // Print information about the message.
        for (Message message : messages) {
            System.out.println("\nMessage received.");
            System.out.println("  ID: " + message.messageId());
            System.out.println("  Receipt handle: " + message.receiptHandle());
            System.out.println("  Message body (first 5 characters): "
                    + message.body().substring(0, 5));
        }

        // Delete the message, the queue, and the bucket.
        final String messageReceiptHandle = messages.get(0).receiptHandle();
        sqsExtended.deleteMessage( DeleteMessageRequest.builder().queueUrl(url.queueUrl())
                .receiptHandle(messageReceiptHandle).build());

    }
}
