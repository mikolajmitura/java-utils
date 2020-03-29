package pl.jalokim.utils.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static pl.jalokim.utils.constants.Constants.COLON;
import static pl.jalokim.utils.constants.Constants.NEW_LINE;
import static pl.jalokim.utils.string.StringUtils.concat;

/**
 * Utility Class for invoke some commands in system.
 */
public final class TerminalCommandsInvoker {

    private static final String OS_NAME = "os.name";
    private static final String AMPERSANDS = " && ";

    private TerminalCommandsInvoker() {

    }

    /**
     * Check that is Linux system.
     * @return true if is Linux
     */
    public static boolean isLinux() {
        return getOsName().contains("Linux");
    }

    /**
     * Check that is Windows system.
     * @return true if is Windows
     */
    public static boolean isWindows() {
        return getOsName().contains("Windows");
    }

    /**
     * get name of OS.
     * @return name of OS.
     */
    public static String getOsName() {
        return System.getProperty(OS_NAME);
    }

    /**
     * It invokes provided command in provided path.
     *
     * @param path path where should be invoked command
     * @param command command which will be invoked in provided path
     * @return output from result of command
     */
    public static String invokeCommand(String path, String command) {
        return invokeCommand(path, command, UTF_8);
    }

    /**
     * It invokes provided command in provided path.
     *
     * @param path path where should be invoked command
     * @param command command which will be invoked in provided path
     * @param charset for command result
     * @return output from result of command
     */
    public static String invokeCommand(String path, String command, Charset charset) {
        try {
            List<String> execCommands = getExecCommands(path, command);
            String[] commandArgs = execCommands.toArray(new String[0]);
            ProcessBuilder builder = new ProcessBuilder(commandArgs);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            return getResultAsText(charset, process);
        } catch (IOException ex) {
            throw new CommandException(ex);
        }
    }

    private static List<String> getExecCommands(String path, String command) {
        String moveToPathCommand = "cd " + path;
        List<String> execCommands = new ArrayList<>();
        if (isWindows()) {
            if (path.contains(COLON)) {
                String driveLetter = path.split(COLON)[0];
                moveToPathCommand = concat(driveLetter, COLON, AMPERSANDS, moveToPathCommand);
            }
            execCommands.add("cmd.exe");
            execCommands.add("/c");
        } else if (isLinux()) {
            execCommands.add("/bin/bash");
            execCommands.add("-c");
        } else {
            throw new CommandException("Not supported system: " + getOsName());
        }
        String lastCommand = moveToPathCommand + AMPERSANDS + command;
        execCommands.add(lastCommand);
        return execCommands;
    }

    private static String getResultAsText(Charset charset, Process process) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
            StringBuilder result = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                result.append(line);
                result.append(NEW_LINE);
            }
            return result.toString();
        }
    }
}
