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
import java.util.UUID;

@Command(name = "send", description = "Sends a large payload to SQS")
public class SendCommand implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SendCommand.class);

    @Mixin
    private HelpOption helpOption;

    @Option(names = "-bucketName", description = "The s3 bucket used for message", required = true)
    private String bucketName;
    @Option(names = "-queueName", description = "The name of the SQS queue")
    private String queueName;
    @Option(names = "-messageSize", description = "size of the message", required = true, defaultValue = "300000")
    private Integer messageSize;

    @Override
    public void run() {
        logger.info("Executing send command with bucketName = " + bucketName + ", queueName = " + queueName + ", messageSize = " + messageSize);

        final S3Client s3 = S3Client.builder().region(Region.EU_WEST_1).build();


        final ExtendedClientConfiguration extendedClientConfig =
                new ExtendedClientConfiguration()
                        .withLargePayloadSupportEnabled(s3, bucketName);

        SqsClient sqsClient = SqsClient.builder().region(Region.EU_WEST_1).build();
        final AmazonSQSExtendedClient sqsExtended =
                new AmazonSQSExtendedClient(sqsClient, extendedClientConfig);


        var url = sqsExtended.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).queueOwnerAWSAccountId("334423569906").build());

        int stringLength = messageSize;
        char[] chars = new char[stringLength];
        Arrays.fill(chars, 'x');
        final String myLongString = new String(chars);

        final SendMessageRequest myMessageRequest =
                SendMessageRequest.builder().queueUrl(url.queueUrl())
                        .messageBody(myLongString)
                        .messageGroupId("1")
                        .messageDeduplicationId(UUID.randomUUID().toString())
                        .build();
        sqsExtended.sendMessage(myMessageRequest);
        System.out.println("Sent the message.");

    }
}
