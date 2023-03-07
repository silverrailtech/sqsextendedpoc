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

import java.util.Arrays;
import java.util.List;

@Command(name = "create", description = "Creates named value with given id and value")
public class CreateCommand implements Runnable {

    private Logger logger = LoggerFactory.getLogger(CreateCommand.class);

    @Mixin
    private HelpOption helpOption;

    @Option(names = "-id", description = "value id", required = true)
    private String id;
    @Option(names = "-name", description = "value name")
    private String name;
    @Option(names = "-value", description = "value content", required = true)
    private String value;

    @Override
    public void run() {
        logger.info("Executing create command with id = " + id + ", name = " + name + ", value = " + value);

        final S3Client s3 = S3Client.builder().region(Region.EU_WEST_1).build();


        final ExtendedClientConfiguration extendedClientConfig =
                new ExtendedClientConfiguration()
                        .withLargePayloadSupportEnabled(s3, "testkrakenbucket");

        SqsClient sqsClient = SqsClient.builder().region(Region.EU_WEST_1).build();
        final AmazonSQSExtendedClient sqsExtended =
                new AmazonSQSExtendedClient(sqsClient, extendedClientConfig);


        var url = sqsExtended.getQueueUrl(GetQueueUrlRequest.builder().queueName("testkraken.fifo").queueOwnerAWSAccountId("334423569906").build());

        int stringLength = 300000;
        char[] chars = new char[stringLength];
        Arrays.fill(chars, 'x');
        final String myLongString = new String(chars);

        final SendMessageRequest myMessageRequest =
                SendMessageRequest.builder().queueUrl(url.queueUrl())
                        .messageBody(myLongString)
                        .messageGroupId("1")
                        .build();
        sqsExtended.sendMessage(myMessageRequest);
        System.out.println("Sent the message.");

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
        sqsExtended.deleteMessage(DeleteMessageRequest.builder().queueUrl(url.queueUrl()).queueUrl(messageReceiptHandle).build());
        System.out.println("Deleted the message.");
    }
}
