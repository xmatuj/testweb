// src/main/java/com/musicstreaming/controller/TrackStreamController.java
package com.musicstreaming.controller;

import com.musicstreaming.model.Track;
import com.musicstreaming.model.TrackStatistics;
import com.musicstreaming.repository.TrackRepository;
import com.musicstreaming.repository.TrackStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/tracks")
public class TrackStreamController {

    private static final Logger logger = LoggerFactory.getLogger(TrackStreamController.class);

    private final TrackRepository trackRepository;
    private final TrackStatisticsRepository statsRepository;
    private final ServletContext servletContext;

    @Autowired
    public TrackStreamController(TrackRepository trackRepository,
                                 TrackStatisticsRepository statsRepository,
                                 ServletContext servletContext) {
        this.trackRepository = trackRepository;
        this.statsRepository = statsRepository;
        this.servletContext = servletContext;
    }

    /**
     * Получение пути к папке uploads/music
     */
    private Path getUploadPath() {
        // Получаем путь к папке проекта
        String rootPath = servletContext.getRealPath("/");
        // Поднимаемся от target/.../webapp к корню проекта
        Path projectRoot = Paths.get(rootPath).getParent().getParent().getParent();
        Path uploadPath = projectRoot.resolve("uploads").resolve("music");

        // Создаём папку если её нет
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create upload directory", e);
        }

        return uploadPath;
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<StreamingResponseBody> streamTrack(@PathVariable Integer id,
                                                             HttpServletRequest request) {
        logger.info("Streaming track with id: {}", id);

        Track track = trackRepository.findById(id).orElse(null);
        if (track == null) {
            logger.error("Track not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        String filePath = track.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            logger.error("Track has no file path: {}", id);
            return ResponseEntity.notFound().build();
        }

        Path uploadPath = getUploadPath();
        File audioFile = uploadPath.resolve(filePath).toFile();

        logger.info("Looking for file: {}", audioFile.getAbsolutePath());

        if (!audioFile.exists()) {
            logger.error("Audio file not found: {}", audioFile.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        long fileSize = audioFile.length();
        logger.info("File size: {} bytes", fileSize);

        // Поддержка Range запросов для перемотки
        String rangeHeader = request.getHeader("Range");
        long rangeStart = 0;
        long rangeEnd = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
            try {
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid range header: {}", rangeHeader);
            }
            if (rangeEnd >= fileSize) {
                rangeEnd = fileSize - 1;
            }
        }

        long finalRangeStart = rangeStart;
        long finalRangeEnd = rangeEnd;
        long contentLength = finalRangeEnd - finalRangeStart + 1;

        logger.info("Streaming bytes {}-{}/{}", finalRangeStart, finalRangeEnd, fileSize);

        StreamingResponseBody responseBody = outputStream -> {
            try (RandomAccessFile file = new RandomAccessFile(audioFile, "r")) {
                byte[] buffer = new byte[8192];
                file.seek(finalRangeStart);
                long bytesRemaining = contentLength;
                int bytesRead;
                while (bytesRemaining > 0 && (bytesRead = file.read(buffer, 0,
                        (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }
                outputStream.flush();
                logger.debug("Finished streaming track {}", id);
            } catch (IOException e) {
                logger.error("Error streaming track {}", id, e);
            }
        };

        HttpHeaders headers = new HttpHeaders();

        // Определяем MIME тип
        String contentType = "audio/mpeg";
        if (filePath.endsWith(".wav")) {
            contentType = "audio/wav";
        }
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");

        if (rangeHeader != null) {
            headers.add(HttpHeaders.CONTENT_RANGE,
                    "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(responseBody);
        } else {
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            return ResponseEntity.ok().headers(headers).body(responseBody);
        }
    }

    @PostMapping("/record/{id}")
    public ResponseEntity<Void> recordPlay(@PathVariable Integer id) {
        trackRepository.findById(id).ifPresent(track -> {
            TrackStatistics stats = new TrackStatistics(track);
            statsRepository.save(stats);
            logger.debug("Recorded play for track {}", id);
        });
        return ResponseEntity.ok().build();
    }
}