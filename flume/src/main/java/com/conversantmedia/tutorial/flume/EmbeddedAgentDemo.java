package com.conversantmedia.tutorial.flume;

import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;
import org.apache.flume.agent.embedded.

/**
 * Created by mkeane on 3/24/16.
 */
public class EmbeddedAgentDemo {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmbeddedAgentDemo.class);

    public static void main(String[] args) {
        BasicParser basicParser = new BasicParser();
        Option o = new Option("p", "properties", true, "path to properties file");
        o.setRequired(true);
        Options options = new Options()
                .addOption(o);
        String propertiesFile = null;

        try {
            basicParser.parse(options, args);
            CommandLine commandLine = basicParser.parse(options, args);
            propertiesFile = commandLine.getOptionValue("p");
        } catch (ParseException parseException) {
            log.error("Unable to parse command line options. " + parseException.getMessage());
        }

    }

}
