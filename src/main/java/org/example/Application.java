package org.example;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import org.example.cli.parser.CommandLineParser;
import picocli.CommandLine;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Application {

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new CommandLineParser());

        if (args.length == 0) {
            commandLine.usage(System.out);
            System.exit(1);
        }

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);

    }
}
