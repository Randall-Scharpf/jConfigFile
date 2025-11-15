package com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest;

import com.randallscharpf.java.jconfigfile.ConfigLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SystemStreamTester {

    public static enum StreamType {
        INPUT,
        OUTPUT,
        ERROR
    }

    public static enum InteractionType {
        STREAM_RAW_DATA_LINETERM,
        STREAM_RAW_DATA_NOTERM,
        STREAM_ANY_DATA_LINETERM
    }

    public static class Interaction {
        public Interaction(String data, StreamType stream, InteractionType type) {
            this.data = data;
            this.stream = stream;
            this.type = type;
        }

        public static Interaction inputLine(String input) {
            return new Interaction(input, StreamType.INPUT, InteractionType.STREAM_RAW_DATA_LINETERM);
        }

        public static Interaction outputLine(String output) {
            return new Interaction(output, StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_LINETERM);
        }

        public static Interaction errorLine(String error) {
            return new Interaction(error, StreamType.ERROR, InteractionType.STREAM_RAW_DATA_LINETERM);
        }

        private final String data;
        private final StreamType stream;
        private final InteractionType type;
    }

    public static class TestInputStream extends InputStream {
        private final Queue<Integer> bytes;
        private final Object dataNotifier;

        public TestInputStream() {
            bytes = new ConcurrentLinkedDeque<>();
            dataNotifier = new Object();
        }

        public void addInput(String data) {
            for (char c : data.toCharArray()) {
                bytes.add((int) c);
            }
            synchronized (dataNotifier) {
                dataNotifier.notifyAll();
            }
        }

        @Override
        public int read() throws IOException {
            Integer result;
            do {
                result = bytes.poll();
                if (result == null) {
                    try {
                        synchronized (dataNotifier) {
                            dataNotifier.wait();
                        }
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                }
            } while (result == null);
            return result;
        }
    }

    public static class TestOutputStream extends OutputStream {
        private final List<Integer> bytes;
        private final Object dataLock;

        public TestOutputStream() {
            this.bytes = new ArrayList<>();
            dataLock = new Object();
        }

        @Override
        public void write(int b) {
            synchronized (dataLock) {
                bytes.add(b);
                dataLock.notifyAll();
            }
        }

        public List<Integer> getNewBytes() {
            List<Integer> bufferCopy = new ArrayList<>();
            synchronized (dataLock) {
                while (bytes.isEmpty()) {
                    try {
                        dataLock.wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
                bufferCopy.addAll(bytes);
                bytes.clear();
            }
            return bufferCopy;
        }
    }

    private boolean bound;
    private InputStream systemInput;
    private PrintStream systemOutput;
    private PrintStream systemErrors;

    private final TestInputStream testInput;
    private final TestOutputStream testOutput;
    private final TestOutputStream testErrors;

    public SystemStreamTester() {
        bound = false;
        testInput = new TestInputStream();
        testOutput = new TestOutputStream();
        testErrors = new TestOutputStream();
    }

    public void bindToSystemStreams() {
        if (!bound) {
            systemInput = System.in;
            systemOutput = System.out;
            systemErrors = System.err;
            bound = true;
            System.setIn(testInput);
            System.setOut(new PrintStream(testOutput));
            System.setErr(new PrintStream(testErrors));
        }
    }

    public void unbindFromSystemStreams() {
        if (bound) {
            System.setIn(systemInput);
            System.setOut(systemOutput);
            System.setErr(systemErrors);
            bound = false;
        }
    }

    private boolean pullFromStream(TestOutputStream stream, Queue<Integer> byteCache, String data) {
        for (int i = 0; i < data.length(); i++) {
            while (byteCache.isEmpty()) {
                byteCache.addAll(stream.getNewBytes());
            }
            int next_byte = byteCache.poll();
//            systemErrors.printf("got %c, expected %c\n", next_byte, data.charAt(i));
            if (next_byte != data.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private void flushLineFromStream(TestOutputStream stream, Queue<Integer> byteCache) {
        int polled_byte;
        do {
            while (byteCache.isEmpty()) {
                byteCache.addAll(stream.getNewBytes());
            }
            polled_byte = byteCache.poll();
        } while (polled_byte != '\n');
    }

    public boolean validateStreamedData(Queue<Interaction> expectedBehavior) {
        Queue<Integer> outputBytes = new ArrayDeque<>();
        Queue<Integer> errorBytes = new ArrayDeque<>();
        for (Interaction chunk : expectedBehavior) {
            if (!chunk.data.trim().isEmpty()) {
                systemOutput.println("Validating " + chunk.stream + "\t" + chunk.type + ":  \t" + chunk.data);
            }
            switch (chunk.stream) {
                case INPUT:
                {
                    switch (chunk.type) {
                        case STREAM_RAW_DATA_NOTERM:
                            testInput.addInput(chunk.data);
                            break;
                        case STREAM_RAW_DATA_LINETERM:
                            testInput.addInput(chunk.data + System.getProperty("line.separator"));
                            break;
                        case STREAM_ANY_DATA_LINETERM:
                            // invalid interaction type for input stream
                            return false;
                    }
                    break;
                }
                case OUTPUT:
                {
                    switch (chunk.type) {
                        case STREAM_RAW_DATA_NOTERM:
                            if (!pullFromStream(testOutput, outputBytes, chunk.data)) {
                                return false;
                            }
                            break;
                        case STREAM_RAW_DATA_LINETERM:
                            if (!pullFromStream(testOutput, outputBytes, chunk.data)) {
                                return false;
                            }
                            if (!pullFromStream(testOutput, outputBytes, System.getProperty("line.separator"))) {
                                return false;
                            }
                            break;
                        case STREAM_ANY_DATA_LINETERM:
                            flushLineFromStream(testOutput, outputBytes);
                            break;
                    }
                    break;
                }
                case ERROR:
                {
                    switch (chunk.type) {
                        case STREAM_RAW_DATA_NOTERM:
                            if (!pullFromStream(testErrors, errorBytes, chunk.data)) {
                                return false;
                            }
                            break;
                        case STREAM_RAW_DATA_LINETERM:
                            if (!pullFromStream(testErrors, errorBytes, chunk.data)) {
                                return false;
                            }
                            if (!pullFromStream(testErrors, errorBytes, System.getProperty("line.separator"))) {
                                return false;
                            }
                            break;
                        case STREAM_ANY_DATA_LINETERM:
                            flushLineFromStream(testErrors, errorBytes);
                            break;
                    }
                    break;
                }
            }
        }
        return true;
    }

    public static Queue<Interaction> CREATE_NEW_FILE_INTERACTIONS(ConfigLocation loc) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                loc.toString()
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "NO"
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_NEW_FILE_ERROR_INTERACTIONS(ConfigLocation loc) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CREATE_NEW_FILE_INTERACTIONS(loc));
        result.add(new Interaction("Failed to open config file with message:", StreamType.ERROR, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(new Interaction("", StreamType.ERROR, InteractionType.STREAM_ANY_DATA_LINETERM));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_NEW_FILE_BAD_INPUT_INTERACTIONS() {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "this is not a valid input"
        ));
        result.add(Interaction.errorLine("Input \"this is not a valid input\" was not accepted"));
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_NEW_FILE_BAD_INPUT_ERROR_INTERACTIONS() {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CREATE_NEW_FILE_BAD_INPUT_INTERACTIONS());
        result.add(Interaction.errorLine(
                "User canceled config initializer without creating config file"
        ));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

    public static Queue<Interaction> CANCEL_INTERACTIONS() {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        return result;
    }
    
    public static Queue<Interaction> CANCEL_ERROR_INTERACTIONS() {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CANCEL_INTERACTIONS());
        result.add(Interaction.errorLine(
                "User canceled config initializer without creating config file"
        ));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_INTERACTIONS(ConfigLocation loc, String templatePath) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                loc.toString()
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "YES"
        ));
        result.add(Interaction.outputLine(
                "  Enter the file path to copy (or type 'cancel')"
        ));
        result.add(new Interaction("  ", StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(Interaction.inputLine(
                templatePath
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_ERROR_INTERACTIONS(ConfigLocation loc, String templatePath) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CREATE_COPY_INTERACTIONS(loc, templatePath));
        result.add(new Interaction("Failed to open config file with message:", StreamType.ERROR, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(new Interaction("", StreamType.ERROR, InteractionType.STREAM_ANY_DATA_LINETERM));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_BAD_INPUT_INTERACTIONS(ConfigLocation loc, String templatePath) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                loc.toString()
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "YES"
        ));
        result.add(Interaction.outputLine(
                "  Enter the file path to copy (or type 'cancel')"
        ));
        result.add(new Interaction("  ", StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(Interaction.inputLine(
                templatePath
        ));
        result.add(Interaction.errorLine(
                "  Input \"" + templatePath + "\" was not accepted"
        ));
        result.add(Interaction.outputLine(
                "  Enter the file path to copy (or type 'cancel')"
        ));
        result.add(new Interaction("  ", StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_BAD_INPUT_ERROR_INTERACTIONS(ConfigLocation loc, String templatePath) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CREATE_COPY_BAD_INPUT_INTERACTIONS(loc, templatePath));
        result.add(Interaction.errorLine(
                "User canceled config initializer without creating config file"
        ));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_AND_CONTINUE_INTERACTIONS(ConfigLocation loc) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                loc.toString()
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "YES"
        ));
        result.add(Interaction.outputLine(
                "  Enter the file path to copy (or type 'cancel')"
        ));
        result.add(new Interaction("  ", StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "NO"
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_AND_CANCEL_INTERACTIONS(ConfigLocation loc) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.add(Interaction.outputLine(
                "Select a location to place this application's config file. Valid options are "
                + "APPDATA, ETC, USERPROFILE, DOCUMENTS, and SIBLING (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                loc.toString()
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "YES"
        ));
        result.add(Interaction.outputLine(
                "  Enter the file path to copy (or type 'cancel')"
        ));
        result.add(new Interaction("  ", StreamType.OUTPUT, InteractionType.STREAM_RAW_DATA_NOTERM));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        result.add(Interaction.outputLine(
                "Is there an existing file to use to import configuration settings? Valid options are "
                + "YES and NO (or type 'cancel')"
        ));
        result.add(Interaction.inputLine(
                "cancel"
        ));
        return result;
    }

    public static Queue<Interaction> CREATE_COPY_AND_CANCEL_ERROR_INTERACTIONS(ConfigLocation loc) {
        Queue<Interaction> result = new ArrayDeque<>();
        result.addAll(CREATE_COPY_AND_CANCEL_INTERACTIONS(loc));
        result.add(Interaction.errorLine(
                "User canceled config initializer without creating config file"
        ));
        result.add(Interaction.errorLine(
                "Configuration changes will not be persistent."
        ));
        return result;
    }

}
