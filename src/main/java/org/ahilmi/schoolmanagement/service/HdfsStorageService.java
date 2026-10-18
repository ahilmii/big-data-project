package org.ahilmi.schoolmanagement.service;

import jakarta.annotation.PreDestroy;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class HdfsStorageService {

    private final FileSystem fileSystem;

    public HdfsStorageService() throws IOException, InterruptedException {
        Configuration configuration = new Configuration(false);

        configuration.set("fs.defaultFS", "hdfs://localhost:9000");
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

        this.fileSystem = FileSystem.get(URI.create("hdfs://localhost:9000"), configuration, "hadoop");
    }

    public String readFile(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);

        try (FSDataInputStream input = fileSystem.open(path)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void writeFile(String hdfsPath, byte[] data) throws IOException {

        Path path = new Path(hdfsPath);

        try (FSDataOutputStream output =
                     fileSystem.create(path, false)) {
            output.write(data);
        }
    }

    public void uploadImage(String hdfsPath, InputStream inputStream) throws IOException {

        Path path = new Path(hdfsPath);

        try (FSDataOutputStream output = fileSystem.create(path, false)) {
            inputStream.transferTo(output);
        }
    }

    public byte[] readImage(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);

        try (FSDataInputStream input = fileSystem.open(path)) {
            return input.readAllBytes();
        }
    }

    @PreDestroy
    public void close() throws IOException {
        fileSystem.close();
    }
}
