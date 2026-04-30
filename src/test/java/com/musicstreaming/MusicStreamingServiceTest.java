package com.musicstreaming;

import com.musicstreaming.model.*;
import com.musicstreaming.dto.*;
import com.musicstreaming.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MusicStreamingServiceTest {

    // ==================== Test Setup ====================

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;
    private Artist testArtist;
    private Genre testGenre;
    private Album testAlbum;
    private Track testTrack;
    private Playlist testPlaylist;
    private Subscription testSubscription;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpSession mockSession;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // Initialize test data
        testUser = new User("testuser", "test@example.com",
                passwordEncoder.encode("TestPass123!"));
        testUser.setId(1);
        testUser.setRole(User.UserRole.User);

        testArtist = new Artist("Test Artist", "Test artist description");
        testArtist.setId(1);

        testGenre = new Genre("Rock");
        testGenre.setId(1);

        testAlbum = new Album("Test Album", testArtist);
        testAlbum.setId(1);

        testTrack = new Track("Test Track", "test.mp3", 240, testGenre, testArtist);
        testTrack.setId(1);
        testTrack.setAlbum(testAlbum);
        testTrack.setModerated(true);

        testPlaylist = new Playlist("My Playlist", testUser);
        testPlaylist.setId(1);
        testPlaylist.setVisibility(Playlist.PlaylistVisibility.Public);

        testSubscription = new Subscription(testUser);
        testSubscription.setId(1);
        testSubscription.setActivated(true);
        testSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
        testSubscription.setAmount(new BigDecimal("299.00"));
    }

    // ==================== User Model Tests ====================

    @Test
    @Order(1)
    @DisplayName("TC001 - User Creation: Should create user with correct properties")
    void testUserCreation() {
        User user = new User("john_doe", "john@example.com", "hashedPassword");
        user.setId(100);

        assertEquals(100, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals(User.UserRole.User, user.getRole());
        assertNotNull(user.getDateOfCreated());
    }

    @Test
    @Order(2)
    @DisplayName("TC002 - User Roles: Should correctly identify user roles")
    void testUserRoles() {
        // Admin user
        User adminUser = new User("admin", "admin@test.com", "hash");
        adminUser.setRole(User.UserRole.Admin);
        assertTrue(adminUser.isAdmin());
        assertFalse(adminUser.isMusician()); // Admin is not Musician (unless specifically set)
        assertTrue(adminUser.isSubscriber()); // Admin has all privileges
        assertTrue(adminUser.canUploadTracks()); // Admin can upload tracks

        // Musician user
        User musicianUser = new User("musician", "musician@test.com", "hash");
        musicianUser.setRole(User.UserRole.Musician);
        assertTrue(musicianUser.isMusician());
        assertFalse(musicianUser.isAdmin());
        assertFalse(musicianUser.isSubscriber()); // Not subscriber by default
        assertTrue(musicianUser.canUploadTracks()); // Musicians can upload

        // Subscriber user
        User subscriberUser = new User("subscriber", "sub@test.com", "hash");
        subscriberUser.setRole(User.UserRole.Subscriber);
        assertTrue(subscriberUser.isSubscriber());
        assertFalse(subscriberUser.isAdmin());
        assertFalse(subscriberUser.isMusician());
        assertFalse(subscriberUser.canUploadTracks()); // Subscribers cannot upload by default

        // Regular user
        User regularUser = new User("regular", "reg@test.com", "hash");
        regularUser.setRole(User.UserRole.User);
        assertFalse(regularUser.isAdmin());
        assertFalse(regularUser.isMusician());
        assertFalse(regularUser.isSubscriber());
        assertFalse(regularUser.canUploadTracks());
    }

    @Test
    @Order(3)
    @DisplayName("TC003 - User Authentication: Should authenticate with correct credentials")
    void testUserAuthentication() {
        String rawPassword = "MySecurePass123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User("authuser", "auth@example.com", encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
        assertFalse(passwordEncoder.matches("WrongPassword", user.getPasswordHash()));
    }

    // ==================== Artist Model Tests ====================

    @Test
    @Order(4)
    @DisplayName("TC004 - Artist Creation: Should create artist with albums and tracks")
    void testArtistCreation() {
        Artist artist = new Artist("The Beatles", "Legendary band from Liverpool");
        artist.setId(10);
        artist.setPhotoPath("/images/artists/beatles.jpg");

        assertEquals(10, artist.getId());
        assertEquals("The Beatles", artist.getName());
        assertEquals("Legendary band from Liverpool", artist.getDescription());
        assertEquals("/images/artists/beatles.jpg", artist.getPhotoPath());
        assertNotNull(artist.getAlbums());
        assertNotNull(artist.getTracks());
        assertTrue(artist.getAlbums().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("TC005 - Artist DTO: Should create DTO with correct counts")
    void testArtistDTO() {
        Artist artist = new Artist("Queen", "British rock band");
        artist.setId(5);

        Album album1 = new Album("A Night at the Opera", artist);
        Album album2 = new Album("News of the World", artist);

        Track track1 = new Track("Bohemian Rhapsody", "song.mp3", 354, null, artist);
        Track track2 = new Track("We Will Rock You", "song2.mp3", 122, null, artist);

        artist.getAlbums().add(album1);
        artist.getAlbums().add(album2);
        artist.getTracks().add(track1);
        artist.getTracks().add(track2);

        ArtistDTO dto = new ArtistDTO(
                artist.getId(),
                artist.getName(),
                artist.getDescription(),
                artist.getAlbums().size(),
                artist.getTracks().size(),
                artist.getPhotoPath()
        );

        assertEquals(5, dto.getId());
        assertEquals("Queen", dto.getName());
        assertEquals(2, dto.getAlbumCount());
        assertEquals(2, dto.getTrackCount());
    }

    // ==================== Track Model Tests ====================

    @Test
    @Order(6)
    @DisplayName("TC006 - Track Creation: Should create track with correct properties")
    void testTrackCreation() {
        Track track = new Track();
        track.setId(100);
        track.setTitle("Test Song");
        track.setFilePath("/music/test.mp3");
        track.setDuration(245);
        track.setGenre(testGenre);
        track.setArtist(testArtist);
        track.setModerated(true);
        track.setUploadedByUser(testUser);

        assertEquals(100, track.getId());
        assertEquals("Test Song", track.getTitle());
        assertEquals(245, track.getDuration());
        assertTrue(track.isModerated());
        assertNotNull(track.getUploadedByUser());
    }

    @Test
    @Order(7)
    @DisplayName("TC007 - Track Duration Formatting: Should format duration correctly")
    void testTrackDurationFormatting() {
        Track track1 = new Track("Song 1", "file1.mp3", 180, null, null);
        assertEquals("3:00", track1.getFormattedDuration());

        Track track2 = new Track("Song 2", "file2.mp3", 245, null, null);
        assertEquals("4:05", track2.getFormattedDuration());

        Track track3 = new Track("Song 3", "file3.mp3", 0, null, null);
        assertEquals("3:45", track3.getFormattedDuration());

        Track track4 = new Track("Song 4", "file4.mp3", null, null, null);
        assertEquals("3:45", track4.getFormattedDuration());
    }

    @Test
    @Order(8)
    @DisplayName("TC008 - Track Cover Image: Should return correct cover image")
    void testTrackCoverImage() {
        // Track with album cover
        Album albumWithCover = new Album("Test", testArtist);
        albumWithCover.setCoverPath("/images/albums/cover.jpg");
        Track track1 = new Track("Song", "file.mp3", 180, testGenre, testArtist);
        track1.setAlbum(albumWithCover);
        assertEquals("/images/albums/cover.jpg", track1.getCoverImage());

        // Track with artist photo but no album cover
        testArtist.setPhotoPath("/images/artists/photo.jpg");
        Track track2 = new Track("Song", "file.mp3", 180, testGenre, testArtist);
        assertEquals("/images/artists/photo.jpg", track2.getCoverImage());

        // Track without any cover
        Artist noPhotoArtist = new Artist("No Photo", "desc");
        Track track3 = new Track("Song", "file.mp3", 180, null, noPhotoArtist);
        assertEquals("/images/default-track-cover.jpg", track3.getCoverImage());
    }

    // ==================== Playlist Model Tests ====================

    @Test
    @Order(9)
    @DisplayName("TC009 - Playlist Creation: Should create playlist with correct properties")
    void testPlaylistCreation() {
        Playlist playlist = new Playlist("Summer Hits", testUser);
        playlist.setId(42);
        playlist.setDescription("Best summer songs");
        playlist.setVisibility(Playlist.PlaylistVisibility.Public);

        assertEquals(42, playlist.getId());
        assertEquals("Summer Hits", playlist.getTitle());
        assertEquals(testUser, playlist.getUser());
        assertTrue(playlist.isPublic());
        assertEquals(0, playlist.getTrackCount());
        assertNotNull(playlist.getCreatedDate());
    }

    @Test
    @Order(10)
    @DisplayName("TC010 - Playlist DTO: Should calculate correct duration and track count")
    void testPlaylistDTO() {
        Playlist playlist = new Playlist("My Mix", testUser);
        playlist.setId(99);
        playlist.setDescription("Mixed playlist");
        playlist.setVisibility(Playlist.PlaylistVisibility.Public);

        List<PlaylistTrack> tracks = new ArrayList<>();

        // Add 3 tracks with different durations
        Track track1 = new Track("Track 1", "f1.mp3", 180, null, null);
        track1.setId(1);
        Track track2 = new Track("Track 2", "f2.mp3", 240, null, null);
        track2.setId(2);
        Track track3 = new Track("Track 3", "f3.mp3", 300, null, null);
        track3.setId(3);

        PlaylistTrack pt1 = new PlaylistTrack(playlist, track1);
        PlaylistTrack pt2 = new PlaylistTrack(playlist, track2);
        PlaylistTrack pt3 = new PlaylistTrack(playlist, track3);

        tracks.add(pt1);
        tracks.add(pt2);
        tracks.add(pt3);

        PlaylistDTO dto = new PlaylistDTO(playlist, tracks);

        assertEquals(99, dto.getId());
        assertEquals("My Mix", dto.getTitle());
        assertEquals("Mixed playlist", dto.getDescription());
        assertEquals(3, dto.getTrackCount());
        assertEquals(720, dto.getTotalDuration()); // 180 + 240 + 300 = 720
        assertEquals("12 мин", dto.getFormattedTotalDuration());
    }

    // ==================== Subscription Model Tests ====================

    @Test
    @Order(11)
    @DisplayName("TC011 - Subscription Creation: Should create subscription with correct dates")
    void testSubscriptionCreation() {
        LocalDateTime start = LocalDateTime.now();
        Subscription sub = new Subscription(testUser);
        sub.setId(10);
        sub.setStartDate(start);
        sub.setEndDate(start.plusMonths(1));
        sub.setActivated(true);
        sub.setAmount(new BigDecimal("299.00"));

        assertEquals(10, sub.getId());
        assertEquals(testUser, sub.getUser());
        assertTrue(sub.isActivated());
        assertEquals(start.plusMonths(1), sub.getEndDate());
        assertEquals(new BigDecimal("299.00"), sub.getAmount());
    }

    @Test
    @Order(12)
    @DisplayName("TC012 - Subscription Status: Should correctly identify active subscriptions")
    void testSubscriptionStatus() {
        Subscription activeSub = new Subscription(testUser);
        activeSub.setActivated(true);
        activeSub.setEndDate(LocalDateTime.now().plusDays(10));
        assertTrue(activeSub.isActivated());

        Subscription expiredSub = new Subscription(testUser);
        expiredSub.setActivated(true);
        expiredSub.setEndDate(LocalDateTime.now().minusDays(1));
        assertTrue(expiredSub.getEndDate().isBefore(LocalDateTime.now()));

        Subscription inactiveSub = new Subscription(testUser);
        inactiveSub.setActivated(false);
        inactiveSub.setEndDate(LocalDateTime.now().plusDays(10));
        assertFalse(inactiveSub.isActivated());
    }

    // ==================== DTO Tests ====================

    @Test
    @Order(13)
    @DisplayName("TC013 - Admin User DTO: Should create DTO with correct properties")
    void testAdminUserDTO() {
        User user = new User("adminuser", "admin@test.com", "hash");
        user.setId(50);
        user.setRole(User.UserRole.Admin);

        AdminUserDTO dto = new AdminUserDTO(user, 15);

        assertEquals(50, dto.getId());
        assertEquals("adminuser", dto.getUsername());
        assertEquals("admin@test.com", dto.getEmail());
        assertEquals(User.UserRole.Admin, dto.getRole());
        assertEquals(15, dto.getPlaylistCount());
        assertTrue(dto.isAdmin());
        assertFalse(dto.isMusician());
    }

    @Test
    @Order(14)
    @DisplayName("TC014 - Home User DTO: Should create DTO with subscription status")
    void testHomeUserDTO() {
        User user = new User("homeuser", "home@test.com", "hash");
        user.setId(25);
        user.setRole(User.UserRole.Subscriber);

        HomeUserDTO dtoWithSub = new HomeUserDTO(user, true);
        assertTrue(dtoWithSub.isHasActiveSubscription());
        assertTrue(dtoWithSub.isSubscriber());
        assertFalse(dtoWithSub.isAdmin());

        HomeUserDTO dtoWithoutSub = new HomeUserDTO(user, false);
        assertFalse(dtoWithoutSub.isHasActiveSubscription());
        assertTrue(dtoWithoutSub.isSubscriber()); // Still subscriber by role
    }

    @Test
    @Order(15)
    @DisplayName("TC015 - Track DTO: Should create DTO with all track properties")
    void testTrackDTO() {
        Track track = new Track("Test Track", "test.mp3", 200, testGenre, testArtist);
        track.setId(75);
        track.setAlbum(testAlbum);
        track.setModerated(true);
        track.setUploadedByUser(testUser);

        TrackDTO dto = new TrackDTO(track);

        assertEquals(75, dto.getId());
        assertEquals("Test Track", dto.getTitle());
        assertEquals("test.mp3", dto.getFilePath());
        assertEquals(200, dto.getDuration());
        assertEquals("3:20", dto.getFormattedDuration());
        assertEquals(testGenre, dto.getGenre());
        assertEquals(testAlbum, dto.getAlbum());
        assertEquals(testArtist, dto.getArtist());
        assertTrue(dto.isModerated());
        assertEquals("testuser", dto.getUploadedByUsername());
    }

    @Test
    @Order(16)
    @DisplayName("TC016 - User Profile DTO: Should detect active subscription")
    void testUserProfileDTO() {
        User user = new User("profileuser", "profile@test.com", "hash");
        user.setId(30);
        user.setRole(User.UserRole.User);

        // Create active subscription
        Subscription activeSub = new Subscription(user);
        activeSub.setActivated(true);
        activeSub.setEndDate(LocalDateTime.now().plusDays(5));

        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(activeSub);

        UserProfileDTO dto = new UserProfileDTO(user, new ArrayList<>(), subscriptions);

        assertTrue(dto.hasActiveSubscription());
        assertTrue(dto.isSubscriber()); // Subscriber because of active subscription
        assertFalse(dto.isAdmin());
        assertEquals("profileuser", dto.getUsername());
    }

    // ==================== Genre and Album Tests ====================

    @Test
    @Order(17)
    @DisplayName("TC017 - Genre Creation: Should create genre with correct properties")
    void testGenreCreation() {
        Genre genre = new Genre("Electronic");
        genre.setId(15);

        assertEquals(15, genre.getId());
        assertEquals("Electronic", genre.getName());
        assertNotNull(genre.getTracks());
        assertTrue(genre.getTracks().isEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("TC018 - Album Creation: Should create album with artist and tracks")
    void testAlbumCreation() {
        Album album = new Album("Dark Side of the Moon", testArtist);
        album.setId(88);
        album.setReleaseDate(java.time.LocalDate.of(1973, 3, 1));
        album.setCoverPath("/images/albums/dsotm.jpg");

        assertEquals(88, album.getId());
        assertEquals("Dark Side of the Moon", album.getTitle());
        assertEquals(testArtist, album.getArtist());
        assertEquals(java.time.LocalDate.of(1973, 3, 1), album.getReleaseDate());
        assertNotNull(album.getTracks());
    }

    @Test
    @Order(19)
    @DisplayName("TC019 - Genre DTO: Should create DTO with track count")
    void testGenreDTO() {
        Genre genre = new Genre("Pop");
        genre.setId(33);

        // Add some tracks
        genre.getTracks().add(new Track("Song1", "s1.mp3", 180, genre, testArtist));
        genre.getTracks().add(new Track("Song2", "s2.mp3", 200, genre, testArtist));
        genre.getTracks().add(new Track("Song3", "s3.mp3", 220, genre, testArtist));

        GenreDTO dto = new GenreDTO(genre.getId(), genre.getName(), (long) genre.getTracks().size());

        assertEquals(33, dto.getId());
        assertEquals("Pop", dto.getName());
        assertEquals(3L, dto.getTrackCount());
    }

    // ==================== Auth Service Tests ====================

    @Test
    @Order(20)
    @DisplayName("TC020 - Auth Service Login: Should store user in session")
    void testAuthServiceLogin() {
        AuthService authService = new AuthService();

        when(mockRequest.getSession(true)).thenReturn(mockSession);

        authService.login(mockRequest, testUser);

        verify(mockRequest, times(1)).getSession(true);
        verify(mockSession, times(1)).setAttribute(eq("loggedInUser"), eq(testUser));
        verify(mockSession, times(1)).setMaxInactiveInterval(1800);
    }

    @Test
    @Order(21)
    @DisplayName("TC021 - Auth Service Logout: Should invalidate session")
    void testAuthServiceLogout() {
        AuthService authService = new AuthService();

        when(mockRequest.getSession(false)).thenReturn(mockSession);

        authService.logout(mockRequest);

        verify(mockSession, times(1)).removeAttribute("loggedInUser");
        verify(mockSession, times(1)).invalidate();
    }

    @Test
    @Order(22)
    @DisplayName("TC022 - Auth Service Role Check: Should correctly identify admin")
    void testAuthServiceRoleCheck() {
        AuthService authService = new AuthService();

        when(mockRequest.getSession(false)).thenReturn(mockSession);

        User adminUser = new User("admin", "admin@test.com", "hash");
        adminUser.setRole(User.UserRole.Admin);

        when(mockSession.getAttribute("loggedInUser")).thenReturn(adminUser);

        assertTrue(authService.isAdmin(mockRequest));
        assertTrue(authService.isMusician(mockRequest));
        assertTrue(authService.isAuthenticated(mockRequest));
    }

    // ==================== Model Relationship Tests ====================

    @Test
    @Order(23)
    @DisplayName("TC023 - Model Relationships: Should properly link related entities")
    void testModelRelationships() {
        // Create chain of relationships
        Track track = new Track("Test Track", "audio.mp3", 300, testGenre, testArtist);
        track.setAlbum(testAlbum);
        track.setUploadedByUser(testUser);

        testAlbum.getTracks().add(track);
        testArtist.getTracks().add(track);
        testArtist.getAlbums().add(testAlbum);
        testGenre.getTracks().add(track);

        // Verify relationships
        assertTrue(testAlbum.getTracks().contains(track));
        assertTrue(testArtist.getTracks().contains(track));
        assertTrue(testGenre.getTracks().contains(track));
        assertEquals(testArtist, track.getArtist());
        assertEquals(testAlbum, track.getAlbum());
        assertEquals(testUser, track.getUploadedByUser());
    }

    // ==================== Subscription User DTO Tests ====================

    @Test
    @Order(24)
    @DisplayName("TC024 - Subscription User DTO: Should correctly check roles")
    void testSubscriptionUserDTO() {
        // Regular user with active subscription
        User user = new User("subuser", "sub@test.com", "hash");
        user.setId(60);
        user.setRole(User.UserRole.User);

        SubscriptionUserDTO dtoWithSub = new SubscriptionUserDTO(user, true);
        assertTrue(dtoWithSub.hasActiveSubscription());
        assertTrue(dtoWithSub.isSubscriber()); // Due to active subscription

        // Regular user without subscription
        SubscriptionUserDTO dtoWithoutSub = new SubscriptionUserDTO(user, false);
        assertFalse(dtoWithoutSub.hasActiveSubscription());
        assertFalse(dtoWithoutSub.isSubscriber()); // No active subscription, User role
        assertFalse(dtoWithoutSub.isAdmin());
        assertFalse(dtoWithoutSub.isMusician());

        // Admin user
        User adminUser = new User("admin", "admin@test.com", "hash");
        adminUser.setRole(User.UserRole.Admin);

        SubscriptionUserDTO dtoAdmin = new SubscriptionUserDTO(adminUser, false);
        assertTrue(dtoAdmin.isAdmin());
        assertTrue(dtoAdmin.isMusician()); // Admin is also considered musician
        assertTrue(dtoAdmin.isSubscriber()); // Admin has all privileges
    }

    @Test
    @Order(25)
    @DisplayName("TC025 - Playlist Track ID Class: Should properly implement equals/hashCode")
    void testPlaylistTrackIdClass() {
        PlaylistTrack.PlaylistTrackId id1 = new PlaylistTrack.PlaylistTrackId(1, 100);
        PlaylistTrack.PlaylistTrackId id2 = new PlaylistTrack.PlaylistTrackId(1, 100);
        PlaylistTrack.PlaylistTrackId id3 = new PlaylistTrack.PlaylistTrackId(2, 100);

        // Test equals
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);

        // Test hashCode
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    @Order(26)
    @DisplayName("TC026 - Moderation Model: Should create moderation with correct status")
    void testModerationModel() {
        User moderator = new User("moderator", "mod@test.com", "hash");
        moderator.setId(99);
        moderator.setRole(User.UserRole.Admin);

        Track track = new Track("New Track", "new.mp3", 200, testGenre, testArtist);
        track.setId(50);

        Moderation moderation = new Moderation();
        moderation.setId(10);
        moderation.setTrack(track);
        moderation.setModerator(moderator);
        moderation.setStatus(Moderation.ModerationStatus.Pending);
        moderation.setComment("Under review");

        assertEquals(10, moderation.getId());
        assertEquals(track, moderation.getTrack());
        assertEquals(moderator, moderation.getModerator());
        assertEquals(Moderation.ModerationStatus.Pending, moderation.getStatus());
        assertEquals("Under review", moderation.getComment());
        assertNotNull(moderation.getModerationDate());

        // Change status
        moderation.setStatus(Moderation.ModerationStatus.Approved);
        assertEquals(Moderation.ModerationStatus.Approved, moderation.getStatus());

        moderation.setStatus(Moderation.ModerationStatus.Rejected);
        assertEquals(Moderation.ModerationStatus.Rejected, moderation.getStatus());
    }
}