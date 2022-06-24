package nl.knaw.dans.shared;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class App {
    static final int BUFFER_SIZE = 1024;

    public static String scanStream(InputStream inputStream) throws IOException {
        try (var socket = new Socket("localhost",
            3310); var outputStream = new BufferedOutputStream(socket.getOutputStream()); var socketInputStream = socket.getInputStream()) {

            var buffer = new byte[BUFFER_SIZE];
            var bytesRead = inputStream.read(buffer);

            outputStream.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            while (bytesRead >= 0) {
                // create a 4 byte sequence indicating the size of the payload (in network byte order)
                var header = ByteBuffer.allocate(4)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putInt(bytesRead)
                    .array();

                // write the size of the payload
                outputStream.write(header);
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();

                // check if the virus scan daemon already has something to say
                if (socketInputStream.available() > 0) {
                    throw new IOException(
                        "Error reply from server: " + new String(socketInputStream.readAllBytes()));
                }

                bytesRead = inputStream.read(buffer);
            }

            // last payload should be 0 length to indicate we are done
            outputStream.write(new byte[] { 0, 0, 0, 0 });
            outputStream.flush();

            // now return what it has to say
            // this should be checked against a certain output format to verify it is OK
            return new String(socketInputStream.readAllBytes());
        }
    }

    public static void main(String[] args) throws IOException {
        var filenames = new String[] {
            "/audiences.zip",
            "/eicar.com.txt",
            "/eicarcom2.zip",
        };

        for (var filename : filenames) {
            var inputStream = App.class.getResourceAsStream(filename);
            //            var inputStream = new FileInputStream(Path.of(filename)
            //                .toFile());

            var reply = scanStream(inputStream);
            System.out.println("Report for " + filename + ":\n" + reply);
        }
    }
}
