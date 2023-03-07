package org.example.cli.parser;

import org.example.cli.commands.CreateCommand;
import org.example.cli.commands.DeleteCommand;
import org.example.cli.commands.ListCommand;
import org.example.cli.commands.UpdateCommand;
import org.example.cli.common.HelpOption;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import static picocli.CommandLine.Option;

@Command(
        subcommands = {CreateCommand.class, ListCommand.class, UpdateCommand.class, DeleteCommand.class},
        versionProvider = VersionProvider.class
)
public class CommandLineParser {

    @Mixin
    private HelpOption helpOption;
    @Option(names = {"-v", "--version"}, versionHelp = true, description = "Print version information and exit.")
    private boolean versionHelpRequested;
}
