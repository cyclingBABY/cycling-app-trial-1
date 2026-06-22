package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class CwcViewModel(
    application: Application,
    private val repository: CwcRepository
) : AndroidViewModel(application) {

    // --- Navigation & Role States ---
    private val _currentTab = MutableStateFlow("landing") // landing, social, tracking, routes, community, marketplace, admin, profile
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _isAdminRole = MutableStateFlow(false)
    val isAdminRole: StateFlow<Boolean> = _isAdminRole.asStateFlow()

    // --- Master Data Streams ---
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val posts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rides: StateFlow<List<RideEntity>> = repository.allRides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routes: StateFlow<List<RouteEntity>> = repository.allRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clubs: StateFlow<List<ClubEntity>> = repository.allClubs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketplaceItems: StateFlow<List<MarketplaceItemEntity>> = repository.allMarketplaceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Tracking Engine ---
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _rideSeconds = MutableStateFlow(0L)
    val rideSeconds: StateFlow<Long> = _rideSeconds.asStateFlow()

    private val _rideDistance = MutableStateFlow(0.0)
    val rideDistance: StateFlow<Double> = _rideDistance.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    private val _maxSpeed = MutableStateFlow(0.0)
    val maxSpeed: StateFlow<Double> = _maxSpeed.asStateFlow()

    private val _elevationGain = MutableStateFlow(0)
    val elevationGain: StateFlow<Int> = _elevationGain.asStateFlow()

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()

    private val _liveSharingEnabled = MutableStateFlow(true)
    val liveSharingEnabled: StateFlow<Boolean> = _liveSharingEnabled.asStateFlow()

    // --- Advanced GPS, Permissions and Navigation states ---
    private val _isLocationPermissionGranted = MutableStateFlow(false)
    val isLocationPermissionGranted: StateFlow<Boolean> = _isLocationPermissionGranted.asStateFlow()

    private val _batteryOptimizationMode = MutableStateFlow("Active Riding (1s)")
    val batteryOptimizationMode: StateFlow<String> = _batteryOptimizationMode.asStateFlow()

    private val _activeNavigationRoute = MutableStateFlow<RouteEntity?>(null)
    val activeNavigationRoute: StateFlow<RouteEntity?> = _activeNavigationRoute.asStateFlow()

    private val _currentNavigationCue = MutableStateFlow("")
    val currentNavigationCue: StateFlow<String> = _currentNavigationCue.asStateFlow()

    private val _currentNavigationIcon = MutableStateFlow("straight") // straight, left, right, uturn, destination
    val currentNavigationIcon: StateFlow<String> = _currentNavigationIcon.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow(10.0) // 5.0, 10.0, 20.0
    val searchRadiusKm: StateFlow<Double> = _searchRadiusKm.asStateFlow()

    private val _showNearbyCyclistsOnMap = MutableStateFlow(true)
    val showNearbyCyclistsOnMap: StateFlow<Boolean> = _showNearbyCyclistsOnMap.asStateFlow()

    fun requestLocationPermission() {
        _isLocationPermissionGranted.value = true
    }

    fun setBatteryOptimizationMode(mode: String) {
        _batteryOptimizationMode.value = mode
    }

    fun selectNavigationRoute(route: RouteEntity) {
        _activeNavigationRoute.value = route
        _currentTab.value = "tracking" // Route directly to tracker screen
        _currentNavigationCue.value = "Approach starting point: Head north toward route start in 50m"
        _currentNavigationIcon.value = "straight"
    }

    fun clearNavigationRoute() {
        _activeNavigationRoute.value = null
        _currentNavigationCue.value = ""
        _currentNavigationIcon.value = "straight"
    }

    fun setSearchRadius(radius: Double) {
        _searchRadiusKm.value = radius
    }

    fun toggleNearbyCyclistsOnMap() {
        _showNearbyCyclistsOnMap.value = !_showNearbyCyclistsOnMap.value
    }

    // Map Coordinates trace (latitude, longitude)
    private val _gpsCoordinates = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val gpsCoordinates: StateFlow<List<Pair<Double, Double>>> = _gpsCoordinates.asStateFlow()

    // --- Interactive Chat Space ---
    val messages: StateFlow<List<MessageEntity>> = repository.getMessagesByChat("general")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Safety Alerts ---
    private val _sosFired = MutableStateFlow(false)
    val sosFired: StateFlow<Boolean> = _sosFired.asStateFlow()

    private val _accidentDetected = MutableStateFlow(false)
    val accidentDetected: StateFlow<Boolean> = _accidentDetected.asStateFlow()

    // Selected post ID details modal view properties
    private val _activePostComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val activePostComments: StateFlow<List<CommentEntity>> = _activePostComments.asStateFlow()

    private val _focusedPostId = MutableStateFlow<Long?>(null)
    val focusedPostId: StateFlow<Long?> = _focusedPostId.asStateFlow()

    // Timer Job for live riding simulation
    private var trackingJob: Job? = null

    init {
        // Run pre-seeding
        viewModelScope.launch {
            repository.seedDataIfEmpty()
        }
    }

    // Navigation triggers
    fun navigateTo(tab: String) {
        _currentTab.value = tab
    }

    fun toggleAdminRole() {
        _isAdminRole.value = !_isAdminRole.value
        if (_isAdminRole.value) {
            _currentTab.value = "admin"
        } else {
            _currentTab.value = "social"
        }
    }

    // --- Authentication Simulation ---
    fun performLogin(emailOrPhone: String, passwordPlain: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val account = repository.getUserAccount(emailOrPhone.trim().lowercase())
            if (account == null) {
                onResult(false, "No account discovered with this identifier")
            } else if (account.passwordPlain != passwordPlain) {
                onResult(false, "Incorrect password. Please verify your credentials")
            } else {
                val current = userProfile.value ?: UserProfileEntity()
                repository.saveUserProfile(
                    current.copy(
                        isLoggedIn = true,
                        username = account.username,
                        fullName = account.fullName,
                        location = if (account.emailOrPhone == "stuartdonsms@gmail.com") "Kampala, Uganda" else current.location
                    )
                )
                if (account.isAdmin) {
                    _isAdminRole.value = true
                    _currentTab.value = "admin"
                } else {
                    _isAdminRole.value = false
                    _currentTab.value = "social"
                }
                onResult(true, "Authentication successful")
            }
        }
    }

    fun performRegister(fullName: String, emailOrPhone: String, passwordPlain: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedEmail = emailOrPhone.trim().lowercase()
            val existing = repository.getUserAccount(trimmedEmail)
            if (existing != null) {
                onResult(false, "An account is already registered with this identifier")
                return@launch
            }
            if (trimmedEmail.isBlank() || passwordPlain.length < 4) {
                onResult(false, "Valid credentials required (min 4 character password)")
                return@launch
            }

            val cleanEmail = if (trimmedEmail.contains("@")) trimmedEmail.substringBefore("@") else trimmedEmail
            val generatedUsername = cleanEmail.lowercase().replace(" ", "")
            val newAccount = UserAccountEntity(
                emailOrPhone = trimmedEmail,
                passwordPlain = passwordPlain,
                fullName = fullName,
                username = generatedUsername,
                isAdmin = (trimmedEmail == "stuartdonsms@gmail.com")
            )
            repository.registerUserAccount(newAccount)

            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(
                current.copy(
                    isLoggedIn = true,
                    fullName = fullName,
                    username = generatedUsername,
                    totalDistanceRidden = 0.0,
                    achievementsString = "First Ride"
                )
            )

            if (newAccount.isAdmin) {
                _isAdminRole.value = true
                _currentTab.value = "admin"
            } else {
                _isAdminRole.value = false
                _currentTab.value = "social"
                _currentTab.value = "social"
            }
            onResult(true, "Registration successful")
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            repository.saveUserProfile(current.copy(isLoggedIn = false))
            _isAdminRole.value = false
            _currentTab.value = "landing"
        }
    }

    // --- User Profile ---
    fun updateProfile(fullName: String, bio: String, level: String, bike: String, eName: String, ePhone: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                fullName = fullName,
                bio = bio,
                cyclingLevel = level,
                bikeType = bike,
                emergencyContactName = eName,
                emergencyContactPhone = ePhone
            )
            repository.saveUserProfile(updated)
        }
    }

    // --- Social Network ---
    fun triggerCreatePost(
        caption: String,
        category: String,
        imageSeed: String,
        videoUrl: String = "",
        videoDurationSec: Int = 0,
        isPhotoGallery: Boolean = false,
        gallerySeeds: String = "",
        linkedRideId: Long = 0L
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val author = profile?.fullName ?: "Guest Cyclist"
            val location = profile?.location ?: "Kampala, Uganda"
            val newPost = PostEntity(
                authorName = author,
                authorLocation = location,
                authorAvatarSeed = "avatar_stuart",
                category = category,
                caption = caption,
                imageUrl = imageSeed,
                videoUrl = videoUrl,
                videoDurationSec = videoDurationSec,
                isPhotoGallery = isPhotoGallery,
                gallerySeeds = gallerySeeds,
                linkedRideId = linkedRideId,
                likesCount = 0,
                commentsCount = 0,
                isLiked = false,
                isSaved = false
            )
            repository.createPost(newPost)
        }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun savePost(postId: Long) {
        viewModelScope.launch {
            repository.toggleSavePost(postId)
        }
    }

    fun viewPostComments(postId: Long) {
        _focusedPostId.value = postId
        viewModelScope.launch {
            repository.getCommentsForPost(postId).collect {
                _activePostComments.value = it
            }
        }
    }

    fun clearFocusedPost() {
        _focusedPostId.value = null
        _activePostComments.value = emptyList()
    }

    fun postComment(postId: Long, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val author = profile?.fullName ?: "Anonymous"
            val newComment = CommentEntity(
                postId = postId,
                authorName = author,
                authorAvatarSeed = "avatar_stuart",
                text = commentText
            )
            repository.addComment(newComment)
            // Re-fetch comments to update state immediately
            repository.getCommentsForPost(postId).firstOrNull()?.let {
                _activePostComments.value = it
            }
        }
    }

    // Haversine formula for exact distance calculations (Requirement 3 & 4)
    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 * 1000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c // returns meters
    }

    // --- Ride Tracking ---
    fun startRide() {
        if (_isTracking.value) return
        _isTracking.value = true
        _isPaused.value = false
        _rideSeconds.value = 0L
        _rideDistance.value = 0.0
        _currentSpeed.value = 0.0
        _maxSpeed.value = 0.0
        _elevationGain.value = 0
        _caloriesBurned.value = 0
        _sosFired.value = false
        _accidentDetected.value = false

        // Extract active navigation path if available
        val activeRoute = _activeNavigationRoute.value
        val pathSimulation = when (activeRoute?.id) {
            1L -> listOf(
                0.3136 to 32.5811, // Clock Tower Kampala
                0.2882 to 32.5710, // Kibuye roundabout
                0.2450 to 32.5650, // Lweza
                0.2012 to 32.5520, // Kajjansi interchange
                0.1510 to 32.5312, // Kawuku
                0.1110 to 32.5080, // Kisubi
                0.0621 to 32.4820, // Abaita Ababiri
                0.0480 to 32.4625  // Entebbe Municipality
            )
            2L -> listOf(
                0.3340 to 32.5510, // Busega
                0.3520 to 32.5490, // Namungoona
                0.3680 to 32.5650, // Kawaala
                0.3710 to 32.5890, // Kalerwe
                0.3620 to 32.6120, // Ntinda overpass
                0.3350 to 32.6310, // Naalya
                0.3190 to 32.6180  // Spear Motors intersection
            )
            3L -> listOf(
                0.3021 to 32.5695,
                0.2980 to 32.5710,
                0.2941 to 32.5690,
                0.2952 to 32.5645,
                0.3005 to 32.5630,
                0.3035 to 32.5660
            )
            else -> listOf(
                0.3204 to 32.5898, // Kololo Airstrip
                0.3225 to 32.5880,
                0.3245 to 32.5855,
                0.3280 to 32.5812, // heading to Bypass
                0.3320 to 32.5815,
                0.3360 to 32.5830, // Bypass
                0.3395 to 32.5858,
                0.3420 to 32.5891,
                0.3451 to 32.5935, // Kyebando
                0.3410 to 32.5970,
                0.3340 to 32.5991, // Bukoto
                0.3281 to 32.5942, // Kamwokya
                0.3220 to 32.5910  // Back to Kololo
            )
        }

        // Set initial location
        _gpsCoordinates.value = listOf(pathSimulation.first())
        _batteryOptimizationMode.value = "Active GPS (1s)"

        trackingJob = viewModelScope.launch {
            var pathIndex = 0
            var lastCoordinateTime = System.currentTimeMillis()

            while (_isTracking.value) {
                // Dynamic coordinate gathering rate based on state (Requirement 13: Battery Optimization)
                val gpsIntervalMs = if (_isPaused.value) {
                    _batteryOptimizationMode.value = "Stationary (Stopped 10s)"
                    5000L
                } else {
                    _batteryOptimizationMode.value = "Active Riding (Aggressive 1s)"
                    1000L
                }

                delay(gpsIntervalMs)

                if (!_isPaused.value) {
                    _rideSeconds.value += (gpsIntervalMs / 1000)

                    // Gather new coordinate point
                    pathIndex = (pathIndex + 1) % pathSimulation.size
                    val targetBase = pathSimulation[pathIndex]
                    // Add micro-noise variation to simulate active hardware GPS positioning
                    val activeLat = targetBase.first + Random.nextDouble(-0.00018, 0.00018)
                    val activeLng = targetBase.second + Random.nextDouble(-0.00018, 0.00018)
                    val currentPoint = activeLat to activeLng

                    val currentPointsList = _gpsCoordinates.value
                    val lastPoint = currentPointsList.lastOrNull()

                    if (lastPoint != null) {
                        // Math Speed and Distance calculation (Requirement 3 & 4)
                        val metersCovered = calculateHaversineDistance(
                            lat1 = lastPoint.first, lon1 = lastPoint.second,
                            lat2 = currentPoint.first, lon2 = currentPoint.second
                        )

                        // Convert segment distance meters back to Kilometers (Requirement 4)
                        val kmCovered = metersCovered / 1000.0
                        _rideDistance.value = String.format(Locale.US, "%.3f", _rideDistance.value + kmCovered).toDouble()

                        // Turn-by-Turn Speed calculation: Speed = distance covered / time delta (Requirement 3)
                        val timeDeltaSeconds = (System.currentTimeMillis() - lastCoordinateTime) / 1000.0
                        val rawSpeedMps = if (timeDeltaSeconds > 0) (metersCovered / timeDeltaSeconds) else 0.0
                        var speedKmh = rawSpeedMps * 3.6 // speed in km/h

                        // Fine tune: keep speed range within bicycle velocity (18 - 36) for active simulation realism
                        if (speedKmh < 5.0) {
                            speedKmh = Random.nextDouble(18.5, 34.2)
                        } else if (speedKmh > 55.0) {
                            speedKmh = 45.0
                        }

                        _currentSpeed.value = String.format(Locale.US, "%.1f", speedKmh).toDouble()
                        if (speedKmh > _maxSpeed.value) {
                            _maxSpeed.value = String.format(Locale.US, "%.1f", speedKmh).toDouble()
                        }

                        // Burn calories based on effort (Requirement 12)
                        _caloriesBurned.value += if (speedKmh > 26.0) 2 else 1
                    }

                    // Append current location point to history (Requirement 5: Route Recording)
                    _gpsCoordinates.value = currentPointsList + currentPoint
                    lastCoordinateTime = System.currentTimeMillis()

                    // Elevation fluctuations (Requirement 12)
                    if (Random.nextInt(10) > 7) {
                        _elevationGain.value += Random.nextInt(1, 4)
                    }

                    // Dynamically generate turn-by-turn instruction alerts (Requirement 8)
                    if (activeRoute != null) {
                        val progress = (pathIndex.toFloat() / pathSimulation.size.toFloat())
                        _currentNavigationCue.value = when {
                            progress < 0.15f -> "Head onto ${activeRoute.routeName}: Standard asphalt, watch out for riders."
                            progress in 0.15f..0.35f -> "In 200m turn left toward the service lane bypass."
                            progress in 0.35f..0.55f -> "Proceed straight. Moderate elevation incline of +${_elevationGain.value}m upcoming."
                            progress in 0.55f..0.75f -> "In 150m, prepare to turn right. Mind the traffic circle."
                            progress in 0.75f..0.95f -> "Route tracking almost finished. Keep spinning!"
                            else -> "Arriving at destination: Ride Completed with success!"
                        }
                        _currentNavigationIcon.value = when {
                            progress < 0.15f -> "straight"
                            progress in 0.15f..0.35f -> "left"
                            progress in 0.35f..0.55f -> "straight"
                            progress in 0.55f..0.75f -> "right"
                            progress in 0.75f..0.95f -> "straight"
                            else -> "destination"
                        }
                    } else {
                        // Basic local route tracking notifications (Requirement 8)
                        val elapsedMins = _rideSeconds.value / 60
                        _currentNavigationCue.value = when {
                            elapsedMins < 1 -> "GPS Calibrated. Safe active tracking. Pedal away!"
                            elapsedMins % 5L == 0L -> "Telemetry secure, broadcasting live signal to fellow community riders."
                            else -> "Tracking standard cycling workout. Keeping Kampala moving!"
                        }
                        _currentNavigationIcon.value = "straight"
                    }
                }
            }
        }
    }

    fun pauseRide() {
        if (_isTracking.value) {
            _isPaused.value = true
        }
    }

    fun resumeRide() {
        if (_isTracking.value && _isPaused.value) {
            _isPaused.value = false
        }
    }

    fun stopAndSaveRide() {
        if (!_isTracking.value) return
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null

        viewModelScope.launch {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val currentDateText = sdf.format(Date())

            val historyText = _gpsCoordinates.value.joinToString(";") { "${it.first},${it.second}" }

            val rideLog = RideEntity(
                durationSeconds = _rideSeconds.value,
                distanceKm = _rideDistance.value,
                averageSpeedKmh = if (_rideSeconds.value > 0) (_rideDistance.value / (_rideSeconds.value / 3600.0)) else 0.0,
                maxSpeedKmh = _maxSpeed.value,
                caloriesBurned = _caloriesBurned.value,
                elevationGainM = _elevationGain.value,
                dateText = currentDateText,
                routeHistoryText = historyText
            )
            val insertedId = repository.saveRide(rideLog)

            // Update profile stats
            val profile = userProfile.value
            if (profile != null) {
                val currentDist = profile.totalDistanceRidden
                val newDist = currentDist + _rideDistance.value

                // Evaluate badges/milestones
                val badges = profile.achievementsString.split(",").toMutableList()
                if (newDist >= 50.0 && !badges.contains("50 KM")) badges.add("50 KM")
                if (newDist >= 100.0 && !badges.contains("100 KM")) badges.add("100 KM")
                if (newDist >= 500.0 && !badges.contains("500 KM")) badges.add("500 KM")

                repository.saveUserProfile(
                    profile.copy(
                        totalDistanceRidden = String.format(Locale.US, "%.1f", newDist).toDouble(),
                        achievementsString = badges.joinToString(",")
                    )
                )
            }

            // Also post ride summary to social feed!
            val formattedDist = String.format(Locale.US, "%.1f KM", _rideDistance.value)
            triggerCreatePost(
                caption = "Logged a standard ride workout! Finished $formattedDist in ${_rideSeconds.value / 60}m with ${_elevationGain.value}m climbing. Tracked live on CWC Together! 🏅🚵‍♂️",
                category = "Achievement",
                imageSeed = "ride_completed",
                linkedRideId = insertedId
            )

            // Clean state & display rides
            _currentTab.value = "profile"
        }
    }

    fun toggleLiveSharing() {
        _liveSharingEnabled.value = !_liveSharingEnabled.value
    }

    fun deleteRide(rideId: Long) {
        viewModelScope.launch {
            repository.deleteRide(rideId)
        }
    }

    // --- Safety Alerts ---
    fun triggerEmergencySos() {
        _sosFired.value = true
        viewModelScope.launch {
            val messageText = "⚠️ EMERGENCY SOS: Cyclist Stuart Don has triggered a safety alert! Current Location coordinate: 0.3204, 32.5898. Emergency contacts notified!"
            repository.sendMessage(
                MessageEntity(
                    chatId = "general",
                    senderName = "🚨 CWC Safety Bot",
                    senderAvatarSeed = "bot",
                    text = messageText,
                    isLocationShare = true,
                    lat = 0.3204,
                    lng = 32.5898
                )
            )
        }
    }

    fun triggerAccidentSimulation() {
        _accidentDetected.value = true
        _isPaused.value = true
        viewModelScope.launch {
            val messageText = "💥 ACCIDENT ALERT: Sudden impact & prolonged inactivity detected! Cyclist Stuart Don needs urgent safety checks at 0.3245, 32.5855."
            repository.sendMessage(
                MessageEntity(
                    chatId = "general",
                    senderName = "🚨 CWC Collision Watchdog",
                    senderAvatarSeed = "bot",
                    text = messageText,
                    isLocationShare = true,
                    lat = 0.3245,
                    lng = 32.5855
                )
            )
        }
    }

    fun dismissSos() {
        _sosFired.value = false
    }

    fun dismissAccident() {
        _accidentDetected.value = false
    }

    // --- Discover & Route Management ---
    fun createRoute(name: String, description: String, dist: Double, difficulty: String) {
        viewModelScope.launch {
            val newRoute = RouteEntity(
                routeName = name,
                description = description,
                distanceKm = dist,
                difficulty = difficulty,
                elevationProfileString = "1180,1210,1230,1190,1170",
                estimatedTimeMinutes = (dist * 2).toInt(),
                creatorName = userProfile.value?.fullName ?: "Local Legend"
            )
            repository.saveRoute(newRoute)
        }
    }

    fun toggleFavoriteRoute(routeId: Long) {
        viewModelScope.launch {
            repository.toggleFavoriteRoute(routeId)
        }
    }

    // --- Clubs & Community ---
    fun joinOrLeaveClub(clubId: Long) {
        viewModelScope.launch {
            repository.toggleJoinClub(clubId)
        }
    }

    fun publishClubAnnouncement(clubId: Long, msg: String) {
        viewModelScope.launch {
            repository.updateClubAnnouncement(clubId, msg)
        }
    }

    fun createClub(name: String, description: String, location: String) {
        viewModelScope.launch {
            val newClub = ClubEntity(
                name = name,
                description = description,
                location = location,
                membersCount = 1,
                isJoined = true,
                logoSeed = "gravel",
                bannerSeed = "gravel_banner"
            )
            repository.saveClub(newClub)
        }
    }

    // --- Events Management ---
    fun registerEvent(eventId: Long) {
        viewModelScope.launch {
            repository.toggleRegisterEvent(eventId)
        }
    }

    fun createNewEvent(title: String, desc: String, date: String, time: String, loc: String, limit: Int) {
        viewModelScope.launch {
            val newEvent = EventEntity(
                title = title,
                description = desc,
                dateText = date,
                timeText = time,
                location = loc,
                organizer = userProfile.value?.fullName ?: "Uganda Cyclist Union",
                registrationLimit = limit,
                registeredCount = 1,
                isRegistered = true
            )
            repository.saveEvent(newEvent)
        }
    }

    fun cancelActiveEvent(eventId: Long) {
        viewModelScope.launch {
            repository.cancelEvent(eventId)
        }
    }

    // --- Messages Space ---
    fun sendChatMessage(text: String, isLocation: Boolean = false) {
        if (text.isBlank() && !isLocation) return
        viewModelScope.launch {
            val currentProfile = userProfile.value
            val sender = currentProfile?.fullName ?: "CWC Guest"
            val message = MessageEntity(
                chatId = "general",
                senderName = sender,
                senderAvatarSeed = "avatar_stuart",
                text = text,
                isLocationShare = isLocation,
                lat = 0.3204 + Random.nextDouble(-0.01, 0.01),
                lng = 32.5898 + Random.nextDouble(-0.01, 0.01)
            )
            repository.sendMessage(message)
        }
    }

    // --- Marketplace ---
    fun sellItem(title: String, category: String, price: Double, description: String, phone: String) {
        viewModelScope.launch {
            val sellerName = userProfile.value?.fullName ?: "Stuart Don"
            val newItem = MarketplaceItemEntity(
                title = title,
                category = category,
                priceUsd = price,
                description = description,
                sellerName = sellerName,
                phone = phone,
                imageSeed = "item_generic_bike",
                isApproved = false // Managed in Admin Portal!
            )
            repository.saveMarketplaceItem(newItem)
        }
    }

    fun deleteItemAdmin(item: MarketplaceItemEntity) {
        viewModelScope.launch {
            repository.deleteMarketplaceItem(item)
        }
    }

    fun approveItemAdmin(itemId: Long) {
        viewModelScope.launch {
            repository.approveMarketplaceItem(itemId)
        }
    }

    // --- Admin Platform Management ---
    fun deleteSocialPostAdmin(post: PostEntity) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    fun suspendCyclistUser(fullName: String) {
        // Mock admin moderation alert
        viewModelScope.launch {
            repository.sendMessage(
                MessageEntity(
                    chatId = "general",
                    senderName = "🛡️ CWC System Moderator",
                    senderAvatarSeed = "mod",
                    text = "⚠️ USER WARNING: Cyclist user '$fullName' has been given an administrative warning regarding community standards guidelines."
                )
            )
        }
    }
}

// Factory constructor setup
class CwcViewModelFactory(
    private val application: Application,
    private val repository: CwcRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CwcViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CwcViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
