package com.musicstreaming.controller;

import com.musicstreaming.model.Track;
import com.musicstreaming.model.TrackStatistics;
import com.musicstreaming.model.User;
import com.musicstreaming.repository.TrackRepository;
import com.musicstreaming.repository.TrackStatisticsRepository;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.RecommendationService;
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
    private AlbumService albumService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    public TrackStreamController(TrackRepository trackRepository,
                                 TrackStatisticsRepository statsRepository,
                                 ServletContext servletContext) {
        this.trackRepository = trackRepository;
        this.statsRepository = statsRepository;
        this.servletContext = servletContext;
    }

    private Path getUploadPath() {
        String userDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(userDir, "uploads", "music");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create upload directory", e);
        }

        logger.info("Upload path resolved to: {}", uploadPath.toAbsolutePath());
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

        Path audioFilePath;
        File audioFile;

        if (filePath.contains(File.separator) || filePath.contains("/")) {
            audioFile = new File(filePath);
            if (!audioFile.exists()) {
                Path uploadPath = getUploadPath();
                String fileName = Paths.get(filePath).getFileName().toString();
                audioFilePath = uploadPath.resolve(fileName);
                audioFile = audioFilePath.toFile();
            }
        } else {
            Path uploadPath = getUploadPath();
            audioFilePath = uploadPath.resolve(filePath);
            audioFile = audioFilePath.toFile();
        }

        logger.info("Looking for file: {}", audioFile.getAbsolutePath());

        if (!audioFile.exists()) {
            logger.error("Audio file not found: {}", audioFile.getAbsolutePath());
            try {
                Path uploadPath = getUploadPath();
                if (Files.exists(uploadPath)) {
                    Files.list(uploadPath).forEach(f -> logger.info("File in upload dir: {}", f.getFileName()));
                }
            } catch (IOException e) {
                logger.error("Error listing upload directory", e);
            }
            return ResponseEntity.notFound().build();
        }

        long fileSize = audioFile.length();
        logger.info("File size: {} bytes", fileSize);

        String contentType = "audio/mpeg";
        String fileName = audioFile.getName().toLowerCase();
        if (fileName.endsWith(".wav")) {
            contentType = "audio/wav";
        } else if (fileName.endsWith(".ogg")) {
            contentType = "audio/ogg";
        } else if (fileName.endsWith(".m4a")) {
            contentType = "audio/mp4";
        }

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

        final File finalAudioFile = audioFile;
        final String finalContentType = contentType;

        StreamingResponseBody responseBody = outputStream -> {
            try (RandomAccessFile file = new RandomAccessFile(finalAudioFile, "r")) {
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
        headers.add(HttpHeaders.CONTENT_TYPE, finalContentType);
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
    public ResponseEntity<Void> recordPlay(@PathVariable Integer id, HttpServletRequest request) {
        logger.info("=== RECORD PLAY CALLED: trackId={} ===", id);

        trackRepository.findById(id).ifPresent(track -> {
            // Запись статистики прослушивания трека
            TrackStatistics stats = new TrackStatistics(track);
            statsRepository.save(stats);
            logger.debug("Recorded TrackStatistics for track {}", id);

            // Запись в историю рекомендаций
            User currentUser = authService.getCurrentUser(request);
            logger.info("Current user from session: {}", currentUser != null ? currentUser.getUsername() + " (id=" + currentUser.getId() + ")" : "NULL");

            if (currentUser != null) {
                try {
                    recommendationService.recordListening(currentUser.getId(), id);
                    logger.info("=== SUCCESS: Recorded recommendation data for user={}, track={} ===",
                            currentUser.getId(), id);
                } catch (Exception e) {
                    logger.error("=== ERROR recording recommendation data for user={}, track={}: {} ===",
                            currentUser.getId(), id, e.getMessage(), e);
                }
            } else {
                logger.warn("=== SKIP: No authenticated user, can't record recommendation ===");
            }
        });
        return ResponseEntity.ok().build();
    }
}