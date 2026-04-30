package com.musicstreaming.service;

import com.musicstreaming.model.*;
import com.musicstreaming.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private static final int MIN_LISTENINGS_FOR_PERSONALIZED = 3;
    private static final int POPULAR_TRACKS_DAYS = 14;

    private final RecommendationRepository recommendationRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Autowired
    public RecommendationService(RecommendationRepository recommendationRepository,
                                 TrackRepository trackRepository,
                                 UserRepository userRepository) {
        this.recommendationRepository = recommendationRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordListening(Integer userId, Integer trackId) {
        logger.info("=== recordListening START: userId={}, trackId={} ===", userId, trackId);

        User user = userRepository.findById(userId).orElse(null);
        Track track = trackRepository.findById(trackId).orElse(null);

        if (user == null) {
            logger.error("=== User not found: {} ===", userId);
            return;
        }
        if (track == null) {
            logger.error("=== Track not found: {} ===", trackId);
            return;
        }

        // Проверяем, не было ли такого же прослушивания в последние 5 минут
        List<Recommendation> recentListenings = recommendationRepository
                .findByUserIdAndDateAfter(userId, LocalDateTime.now().minusMinutes(5));

        boolean recentDuplicate = recentListenings.stream()
                .anyMatch(r -> r.getTrack().getId().equals(trackId));

        if (recentDuplicate) {
            logger.info("=== Skipping duplicate listening record for user={}, track={} ===", userId, trackId);
            return;
        }

        Recommendation recommendation = new Recommendation(user, track);
        recommendationRepository.save(recommendation);

        // Проверяем, что запись сохранилась
        long totalListenings = recommendationRepository.countByUserId(userId);
        logger.info("=== SUCCESS: Recorded listening - user={}, track={}, title={}. Total listenings for user: {} ===",
                userId, trackId, track.getTitle(), totalListenings);
    }

    @Transactional(readOnly = true)
    public List<Track> getRecommendationsForHome(Integer userId, int limit) {
        logger.info("=== getRecommendationsForHome: userId={}, limit={} ===", userId, limit);

        if (userId == null) {
            logger.info("No user, returning popular tracks");
            return getPopularTracksForPeriod(limit);
        }

        long listenCount = recommendationRepository.countByUserId(userId);
        logger.info("User {} has {} listenings (need {} for personalized)", userId, listenCount, MIN_LISTENINGS_FOR_PERSONALIZED);

        if (listenCount < MIN_LISTENINGS_FOR_PERSONALIZED) {
            logger.info("Not enough listenings for user {}, showing popular tracks", userId);
            return getPopularTracksForPeriod(limit);
        }

        logger.info("Generating personalized recommendations for user {}", userId);
        List<Track> recommendations = getPersonalizedRecommendations(userId, limit);
        logger.info("=== Generated {} personalized recommendations for user {} ===", recommendations.size(), userId);

        return recommendations;
    }

    @Transactional(readOnly = true)
    public List<Track> getRecommendationsForSearch(Integer userId, int limit) {
        return getRecommendationsForHome(userId, limit);
    }

    @Transactional(readOnly = true)
    public List<Track> getPersonalizedRecommendations(Integer userId, int limit) {
        logger.info("=== getPersonalizedRecommendations START: userId={}, limit={} ===", userId, limit);

        // Проверяем количество прослушиваний
        long totalListens = recommendationRepository.countByUserId(userId);
        logger.info("Total listenings for user {}: {}", userId, totalListens);

        if (totalListens < MIN_LISTENINGS_FOR_PERSONALIZED) {
            logger.info("Not enough data for personalized, returning popular");
            return getPopularTracksForPeriod(limit);
        }

        List<Track> recommendations = new ArrayList<>();
        Set<Integer> recommendedTrackIds = new HashSet<>();

        // 1. Рекомендации на основе жанров (40% от лимита)
        int genreLimit = Math.max(1, limit * 40 / 100);
        logger.info("Getting genre-based recommendations (limit={})", genreLimit);
        List<Track> genreBasedRecs = getGenreBasedRecommendations(userId, genreLimit * 3);
        logger.info("Got {} genre-based tracks", genreBasedRecs.size());
        addUniqueTracks(recommendations, genreBasedRecs, recommendedTrackIds, genreLimit);

        // 2. Рекомендации на основе исполнителей (30% от лимита)
        if (recommendations.size() < limit) {
            int artistLimit = Math.max(1, limit * 30 / 100);
            logger.info("Getting artist-based recommendations (limit={})", artistLimit);
            List<Track> artistBasedRecs = getArtistBasedRecommendations(userId, artistLimit * 3);
            logger.info("Got {} artist-based tracks", artistBasedRecs.size());
            addUniqueTracks(recommendations, artistBasedRecs, recommendedTrackIds, artistLimit);
        }

        // 3. Рекомендации из тех же альбомов (15% от лимита)
        if (recommendations.size() < limit) {
            int albumLimit = Math.max(1, limit * 15 / 100);
            logger.info("Getting album-based recommendations (limit={})", albumLimit);
            List<Track> albumBasedRecs = getAlbumBasedRecommendations(userId);
            logger.info("Got {} album-based tracks", albumBasedRecs.size());
            addUniqueTracks(recommendations, albumBasedRecs, recommendedTrackIds, albumLimit);
        }

        // 4. Дополняем популярными треками, если не хватает
        if (recommendations.size() < limit) {
            logger.info("Not enough recommendations ({}), filling with popular tracks", recommendations.size());
            List<Track> popularTracks = getPopularTracksForPeriod(limit * 2);
            List<Track> listenedTracks = recommendationRepository.findDistinctTracksByUserId(userId);
            Set<Integer> listenedTrackIds = listenedTracks.stream()
                    .map(Track::getId)
                    .collect(Collectors.toSet());

            for (Track track : popularTracks) {
                if (recommendations.size() >= limit) break;
                if (!listenedTrackIds.contains(track.getId()) &&
                        !recommendedTrackIds.contains(track.getId())) {
                    recommendations.add(track);
                    recommendedTrackIds.add(track.getId());
                    logger.debug("Added popular track as filler: {}", track.getTitle());
                }
            }
        }

        logger.info("=== Final recommendations count for user {}: {} ===", userId, recommendations.size());
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Track> getPopularTracksForPeriod(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(POPULAR_TRACKS_DAYS);
        logger.info("Getting popular tracks since {} (limit={})", since, limit);

        try {
            List<Object[]> popularTracksData = recommendationRepository
                    .findPopularTracksSince(since, PageRequest.of(0, limit));

            if (!popularTracksData.isEmpty()) {
                List<Track> tracks = popularTracksData.stream()
                        .map(row -> (Track) row[0])
                        .collect(Collectors.toList());
                logger.info("Found {} popular tracks in last {} days", tracks.size(), POPULAR_TRACKS_DAYS);
                return tracks;
            }
        } catch (Exception e) {
            logger.warn("Error getting popular tracks from recommendations: {}", e.getMessage());
        }

        // Fallback: если нет данных в recommendations, используем TrackStatistics
        logger.info("Falling back to TrackStatistics for popular tracks");
        try {
            List<Track> tracks = trackRepository.findByIsModeratedTrueOrderByIdDesc()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            logger.info("Fallback: returning {} recent tracks", tracks.size());
            return tracks;
        } catch (Exception e) {
            logger.error("Error getting fallback tracks", e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public boolean hasEnoughDataForPersonalized(Integer userId) {
        if (userId == null) {
            logger.debug("hasEnoughDataForPersonalized: userId is null");
            return false;
        }
        long count = recommendationRepository.countByUserId(userId);
        boolean hasEnough = count >= MIN_LISTENINGS_FOR_PERSONALIZED;
        logger.debug("hasEnoughDataForPersonalized for user {}: {} (count={}, needed={})",
                userId, hasEnough, count, MIN_LISTENINGS_FOR_PERSONALIZED);
        return hasEnough;
    }

    private List<Track> getGenreBasedRecommendations(Integer userId, int limit) {
        try {
            List<Object[]> genrePreferences = recommendationRepository.findGenrePreferencesByUserId(userId);
            logger.info("Genre preferences for user {}: {} genres found", userId, genrePreferences.size());

            if (genrePreferences.isEmpty()) {
                logger.info("No genre preferences found for user {}", userId);
                return Collections.emptyList();
            }

            // Логируем топ жанров
            for (int i = 0; i < Math.min(3, genrePreferences.size()); i++) {
                Genre genre = (Genre) genrePreferences.get(i)[0];
                Long count = (Long) genrePreferences.get(i)[1];
                logger.info("  Top genre #{}: {} ({} listenings)", i + 1, genre.getName(), count);
            }

            List<Integer> topGenreIds = genrePreferences.stream()
                    .limit(3)
                    .map(row -> ((Genre) row[0]).getId())
                    .collect(Collectors.toList());

            logger.info("Top genre IDs: {}", topGenreIds);

            List<Track> tracks = recommendationRepository.findTracksByGenres(topGenreIds, userId, PageRequest.of(0, limit));
            logger.info("Found {} tracks for top genres", tracks.size());
            return tracks;
        } catch (Exception e) {
            logger.error("Error getting genre-based recommendations for user {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Track> getArtistBasedRecommendations(Integer userId, int limit) {
        try {
            List<Object[]> artistPreferences = recommendationRepository.findArtistPreferencesByUserId(userId);
            logger.info("Artist preferences for user {}: {} artists found", userId, artistPreferences.size());

            if (artistPreferences.isEmpty()) {
                logger.info("No artist preferences found for user {}", userId);
                return Collections.emptyList();
            }

            // Логируем топ исполнителей
            for (int i = 0; i < Math.min(3, artistPreferences.size()); i++) {
                Artist artist = (Artist) artistPreferences.get(i)[0];
                Long count = (Long) artistPreferences.get(i)[1];
                logger.info("  Top artist #{}: {} ({} listenings)", i + 1, artist.getName(), count);
            }

            List<Integer> topArtistIds = artistPreferences.stream()
                    .limit(3)
                    .map(row -> ((Artist) row[0]).getId())
                    .collect(Collectors.toList());

            logger.info("Top artist IDs: {}", topArtistIds);

            List<Track> tracks = recommendationRepository.findTracksByArtists(topArtistIds, userId, PageRequest.of(0, limit));
            logger.info("Found {} tracks for top artists", tracks.size());
            return tracks;
        } catch (Exception e) {
            logger.error("Error getting artist-based recommendations for user {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Track> getAlbumBasedRecommendations(Integer userId) {
        try {
            List<Track> tracks = recommendationRepository.findTracksFromSameAlbums(userId);
            logger.info("Found {} tracks from same albums for user {}", tracks.size(), userId);
            return tracks;
        } catch (Exception e) {
            logger.error("Error getting album-based recommendations for user {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void addUniqueTracks(List<Track> target, List<Track> source,
                                 Set<Integer> usedIds, int maxToAdd) {
        int added = 0;
        for (Track track : source) {
            if (added >= maxToAdd) break;
            if (!usedIds.contains(track.getId())) {
                target.add(track);
                usedIds.add(track.getId());
                added++;
                logger.debug("Added track to recommendations: {} (id={})", track.getTitle(), track.getId());
            }
        }
        logger.debug("Added {} unique tracks out of {} available", added, source.size());
    }
}