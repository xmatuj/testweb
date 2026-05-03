package com.musicstreaming.service;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class AudioMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(AudioMetadataService.class);

    public int getDurationInSeconds(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                logger.warn("Audio file not found: {}", filePath);
                return 0;
            }

            AudioFile audio = AudioFileIO.read(audioFile);
            AudioHeader header = audio.getAudioHeader();
            int duration = header.getTrackLength(); // Длительность в секундах

            logger.info("Audio duration for {}: {} seconds ({}:{})",
                    audioFile.getName(), duration, duration / 60, duration % 60);

            return duration;
        } catch (CannotReadException | IOException | TagException |
                 ReadOnlyFileException | InvalidAudioFrameException e) {
            logger.warn("Could not read audio metadata for file: {}. Using default duration.", filePath, e);
            return 0;
        }
    }

    public String getFormattedDuration(String filePath) {
        int seconds = getDurationInSeconds(filePath);
        if (seconds <= 0) {
            return "0:00";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}