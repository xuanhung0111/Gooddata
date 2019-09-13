package com.gooddata.qa.graphene.utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

public class ProcessBuilderUtils {
    protected static final Logger log = Logger.getLogger(ProcessBuilderUtils.class.getName());

    public static String runCommandLine(List<String> commands, File directoryFile) {
        try {
            if (!directoryFile.exists()) {
                directoryFile.mkdirs();
            }
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(directoryFile);
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String inputStream;
            String inputStreams = StringUtils.EMPTY;
            while ((inputStream = reader.readLine()) != null) {
                inputStreams += inputStream + '\n';
            }
            log.info("Output stream: " + inputStreams);
            return inputStreams;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("there is an error while running command line", e);
        }
    }
}
