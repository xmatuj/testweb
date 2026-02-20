// Global player variables
let currentAudio = null;
let isPlaying = false;
let currentTrackId = null;

// Play track function
function playTrack(trackId) {
    if (currentAudio) {
        currentAudio.pause();
    }

    currentAudio = new Audio('/tracks/stream/' + trackId);
    currentTrackId = trackId;

    currentAudio.addEventListener('canplay', function() {
        currentAudio.play()
            .then(() => {
                isPlaying = true;
                updatePlayButtons(trackId, true);
                recordPlay(trackId);
            })
            .catch(error => console.error('Playback failed:', error));
    });

    currentAudio.addEventListener('ended', function() {
        isPlaying = false;
        updatePlayButtons(trackId, false);
    });
}

// Record play for statistics
function recordPlay(trackId) {
    fetch('/tracks/record/' + trackId, { method: 'POST' })
        .catch(error => console.error('Failed to record play:', error));
}

// Update play buttons UI
function updatePlayButtons(trackId, playing) {
    document.querySelectorAll('.play-button').forEach(btn => {
        const icon = btn.querySelector('i');
        if (btn.getAttribute('data-track-id') == trackId) {
            if (playing) {
                icon.className = 'fas fa-pause';
            } else {
                icon.className = 'fas fa-play';
            }
        }
    });
}

// Toggle play/pause
function togglePlay() {
    if (!currentAudio) return;

    if (isPlaying) {
        currentAudio.pause();
        isPlaying = false;
    } else {
        currentAudio.play();
        isPlaying = true;
    }
    updatePlayButtons(currentTrackId, isPlaying);
}

// Add to playlist
function addToPlaylist(trackId) {
    if (!isAuthenticated) {
        window.location.href = '/account/login';
        return;
    }

    // Show playlist selection modal
    fetch('/playlists/add-to-playlist-modal?trackId=' + trackId)
        .then(response => response.text())
        .then(html => {
            const modalContainer = document.createElement('div');
            modalContainer.innerHTML = html;
            document.body.appendChild(modalContainer.firstElementChild);

            const modal = new bootstrap.Modal(document.getElementById('addToPlaylistModal'));
            modal.show();
        })
        .catch(error => console.error('Failed to load modal:', error));
}

// Like/unlike track
function toggleLike(trackId, button) {
    const icon = button.querySelector('i');
    if (icon.classList.contains('far')) {
        icon.classList.remove('far');
        icon.classList.add('fas');
        icon.style.color = 'var(--accent)';
    } else {
        icon.classList.remove('fas');
        icon.classList.add('far');
        icon.style.color = '';
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Setup play buttons
    document.querySelectorAll('.play-button').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const trackId = this.getAttribute('data-track-id');
            const trackCard = this.closest('.track-card');

            if (trackCard) {
                const trackTitle = trackCard.querySelector('.track-title').textContent;
                const trackArtist = trackCard.querySelector('.track-artist').textContent;
                playTrack(trackId);
            }
        });
    });

    // Setup like buttons
    document.querySelectorAll('.like-button').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleLike(this.getAttribute('data-track-id'), this);
        });
    });

    // Setup add to playlist buttons
    document.querySelectorAll('.add-to-playlist-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const trackId = this.getAttribute('data-track-id');
            addToPlaylist(trackId);
        });
    });
});

// Global variables for auth state
let isAuthenticated = false;