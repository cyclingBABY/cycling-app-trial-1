package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class CwcRepository(private val appDao: AppDao) {

    // Streams
    val userProfile: Flow<UserProfileEntity?> = appDao.getUserProfile()
    val allPosts: Flow<List<PostEntity>> = appDao.getAllPosts()
    val allRides: Flow<List<RideEntity>> = appDao.getAllRides()
    val allRoutes: Flow<List<RouteEntity>> = appDao.getAllRoutes()
    val allClubs: Flow<List<ClubEntity>> = appDao.getAllClubs()
    val allEvents: Flow<List<EventEntity>> = appDao.getAllEvents()
    val allMarketplaceItems: Flow<List<MarketplaceItemEntity>> = appDao.getAllMarketplaceItems()

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = appDao.getCommentsForPost(postId)
    fun getMessagesByChat(chatId: String): Flow<List<MessageEntity>> = appDao.getMessagesByChat(chatId)

    // Operations
    suspend fun getUserAccount(emailOrPhone: String): UserAccountEntity? {
        return appDao.getUserAccount(emailOrPhone)
    }

    suspend fun registerUserAccount(account: UserAccountEntity) {
        appDao.insertUserAccount(account)
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        appDao.insertUserProfile(profile)
    }

    suspend fun createPost(post: PostEntity) {
        appDao.insertPost(post)
    }

    suspend fun deletePost(post: PostEntity) {
        appDao.deletePost(post)
    }

    suspend fun toggleLikePost(postId: Long) {
        // Simple client-side toggle
        val posts = appDao.getAllPosts().firstOrNull() ?: return
        val matched = posts.find { it.id == postId } ?: return
        val updated = matched.copy(
            isLiked = !matched.isLiked,
            likesCount = matched.likesCount + (if (matched.isLiked) -1 else 1)
        )
        appDao.insertPost(updated)
    }

    suspend fun toggleSavePost(postId: Long) {
        val posts = appDao.getAllPosts().firstOrNull() ?: return
        val matched = posts.find { it.id == postId } ?: return
        val updated = matched.copy(isSaved = !matched.isSaved)
        appDao.insertPost(updated)
    }

    suspend fun addComment(comment: CommentEntity) {
        appDao.insertComment(comment)
        // Increment comment counter on post
        val posts = appDao.getAllPosts().firstOrNull() ?: return
        val matched = posts.find { it.id == comment.postId } ?: return
        val updated = matched.copy(commentsCount = matched.commentsCount + 1)
        appDao.insertPost(updated)
    }

    suspend fun saveRide(ride: RideEntity): Long {
        return appDao.insertRide(ride)
    }

    suspend fun deleteRide(id: Long) {
        appDao.deleteRide(id)
    }

    suspend fun saveRoute(route: RouteEntity) {
        appDao.insertRoute(route)
    }

    suspend fun toggleFavoriteRoute(routeId: Long) {
        val routes = appDao.getAllRoutes().firstOrNull() ?: return
        val matched = routes.find { it.id == routeId } ?: return
        appDao.insertRoute(matched.copy(isFavorite = !matched.isFavorite))
    }

    suspend fun saveClub(club: ClubEntity) {
        appDao.insertClub(club)
    }

    suspend fun deleteClub(club: ClubEntity) {
        appDao.deleteClub(club)
    }

    suspend fun toggleJoinClub(clubId: Long) {
        val clubs = appDao.getAllClubs().firstOrNull() ?: return
        val matched = clubs.find { it.id == clubId } ?: return
        val updated = matched.copy(
            isJoined = !matched.isJoined,
            membersCount = matched.membersCount + (if (matched.isJoined) -1 else 1)
        )
        appDao.insertClub(updated)
    }

    suspend fun updateClubAnnouncement(clubId: Long, announcement: String) {
        val clubs = appDao.getAllClubs().firstOrNull() ?: return
        val matched = clubs.find { it.id == clubId } ?: return
        appDao.insertClub(matched.copy(recentAnnouncement = announcement))
    }

    suspend fun saveEvent(event: EventEntity) {
        appDao.insertEvent(event)
    }

    suspend fun toggleRegisterEvent(eventId: Long) {
        val events = appDao.getAllEvents().firstOrNull() ?: return
        val matched = events.find { it.id == eventId } ?: return
        val updated = matched.copy(
            isRegistered = !matched.isRegistered,
            registeredCount = matched.registeredCount + (if (matched.isRegistered) -1 else 1)
        )
        appDao.insertEvent(updated)
    }

    suspend fun cancelEvent(eventId: Long) {
        val events = appDao.getAllEvents().firstOrNull() ?: return
        val matched = events.find { it.id == eventId } ?: return
        appDao.insertEvent(matched.copy(isCancelled = true))
    }

    suspend fun sendMessage(message: MessageEntity) {
        appDao.insertMessage(message)
    }

    suspend fun saveMarketplaceItem(item: MarketplaceItemEntity) {
        appDao.insertMarketplaceItem(item)
    }

    suspend fun deleteMarketplaceItem(item: MarketplaceItemEntity) {
        appDao.deleteMarketplaceItem(item)
    }

    suspend fun approveMarketplaceItem(itemId: Long) {
        val items = appDao.getAllMarketplaceItems().firstOrNull() ?: return
        val matched = items.find { it.id == itemId } ?: return
        appDao.insertMarketplaceItem(matched.copy(isApproved = true))
    }

    // --- Seeding ---
    suspend fun seedDataIfEmpty() {
        // 1. Profile
        val currentProfile = appDao.getUserProfile().firstOrNull()
        if (currentProfile == null) {
            val defaultProfile = UserProfileEntity(
                isLoggedIn = false,
                username = "StuartRide256",
                fullName = "Stuart Don",
                bio = "Ugandan gravel cyclist & local route curator. Pedal together, stay together! 🚲✨",
                location = "Kampala, Uganda",
                cyclingLevel = "Advanced Rider",
                bikeType = "Gravel Bike",
                emergencyContactName = "Jane Don",
                emergencyContactPhone = "+256 772 987654",
                totalDistanceRidden = 345.5,
                followerCount = 451,
                followingCount = 282,
                profilePhotoUri = "avatar_stuart",
                coverPhotoUri = "banner_uganda",
                achievementsString = "First Ride,50 KM,100 KM,Club Champion"
            )
            appDao.insertUserProfile(defaultProfile)
        }

        // 1b. Admin Account Seeding
        val adminAccount = appDao.getUserAccount("stuartdonsms@gmail.com")
        if (adminAccount == null) {
            appDao.insertUserAccount(
                UserAccountEntity(
                    emailOrPhone = "stuartdonsms@gmail.com",
                    passwordPlain = "code5_12345@1",
                    fullName = "Stuart Don",
                    username = "stuartdonsms",
                    isAdmin = true
                )
            )
        }

        // 2. Clubs
        val currentClubs = appDao.getAllClubs().firstOrNull()
        if (currentClubs.isNullOrEmpty()) {
            val defaultClubs = listOf(
                ClubEntity(
                    id = 1,
                    name = "Kampala Gravel Riders",
                    description = "Gravel, dirt, mud and speed across Buganda routes. Weekly explorations around Wakiso & Kampala hills.",
                    location = "Kampala, Central",
                    membersCount = 124,
                    isJoined = true,
                    recentAnnouncement = "Next wet-soil tracking starts this Saturday from Akamwesi Shopping Mall, Gayaza Road! Join us!",
                    logoSeed = "gravel",
                    bannerSeed = "gravel_banner"
                ),
                ClubEntity(
                    id = 2,
                    name = "Nile Source Wheelers",
                    description = "We ride along the banks of the Mighty Nile. Road & light adventure cyclists based in Jinja.",
                    location = "Jinja, Eastern",
                    membersCount = 85,
                    isJoined = false,
                    recentAnnouncement = "Jinja Sunset Cruise rescheduled to Thursday evening. Lights required!",
                    logoSeed = "nile",
                    bannerSeed = "nile_banner"
                ),
                ClubEntity(
                    id = 3,
                    name = "Lugogo Speedsters CC",
                    description = "High-performance road racing, criterium training under strict pace. Uganda's premier amateur cycling division.",
                    location = "Kampala, Central",
                    membersCount = 210,
                    isJoined = false,
                    recentAnnouncement = "Time trial training checks this Sunday morning at 6:00 AM sharp on Kampala Bypass.",
                    logoSeed = "speed",
                    bannerSeed = "speed_banner"
                )
            )
            for (club in defaultClubs) {
                appDao.insertClub(club)
            }
        }

        // 3. Events
        val currentEvents = appDao.getAllEvents().firstOrNull()
        if (currentEvents.isNullOrEmpty()) {
            val defaultEvents = listOf(
                EventEntity(
                    id = 1,
                    title = "Kampala Car-Free Day City Tour",
                    description = "Explore a stress-free Kampala during a car-free Sunday. Join 300+ cyclists to demonstrate green urban transport. Free water points provided by standard sponsors.",
                    dateText = "July 05, 2026",
                    timeText = "07:30 AM",
                    location = "Start at Kololo Airstrip",
                    organizer = "Kampala Gravel Riders",
                    registrationLimit = 500,
                    registeredCount = 145,
                    isRegistered = true
                ),
                EventEntity(
                    id = 2,
                    title = "Source of the Nile Gravel Challenge",
                    description = "A rugged, 80km epic adventure along single-tracks near Jinja, traversing local communities and Sugarcane plantations. Extremely high elevation profiles.",
                    dateText = "July 18, 2026",
                    timeText = "06:00 AM",
                    location = "Jinja Town Hall",
                    organizer = "Nile Source Wheelers",
                    registrationLimit = 150,
                    registeredCount = 74,
                    isRegistered = false
                ),
                EventEntity(
                    id = 3,
                    title = "Entebbe Expressway Sunrise Sprint",
                    description = "Fast paceline cardio sprint under fully secured, early-morning toll road segments. Experience gorgeous scenic views of Lake Victoria under sunrise glow.",
                    dateText = "August 02, 2026",
                    timeText = "05:45 AM",
                    location = "Shell Kabalagala meeting station",
                    organizer = "Lugogo Speedsters CC",
                    registrationLimit = 80,
                    registeredCount = 31,
                    isRegistered = false
                )
            )
            for (event in defaultEvents) {
                appDao.insertEvent(event)
            }
        }

        // 4. Routes
        val currentRoutes = appDao.getAllRoutes().firstOrNull()
        if (currentRoutes.isNullOrEmpty()) {
            val defaultRoutes = listOf(
                RouteEntity(
                    id = 1,
                    routeName = "Kampala-Entebbe Express Challenge",
                    description = "Smooth asphalt, gorgeous Lake Victoria breeze, structured climbing curves. Kampala to Entebbe.",
                    distanceKm = 36.2,
                    difficulty = "Moderate",
                    elevationProfileString = "1200,1150,1180,1230,1210,1170,1140",
                    estimatedTimeMinutes = 75,
                    creatorName = "StuartRide256",
                    rating = 4.8,
                    isFavorite = true
                ),
                RouteEntity(
                    id = 2,
                    routeName = "Kampala Bypass Criterium Ring",
                    description = "Double lane highway bypass route with safe industrial shoulder. Highly demanding rolling hills profile.",
                    distanceKm = 21.0,
                    difficulty = "Hard",
                    elevationProfileString = "1150,1210,1180,1290,1250,1190,1130",
                    estimatedTimeMinutes = 55,
                    creatorName = "Lugogo Speedsters CC",
                    rating = 4.6,
                    isFavorite = false
                ),
                RouteEntity(
                    id = 3,
                    routeName = "Lubiri Palace Ring Road",
                    description = "Historic circular loop around Kabaka's Palace. Very popular for flat speed tests and beginners' leisure social groups.",
                    distanceKm = 4.5,
                    difficulty = "Easy",
                    elevationProfileString = "1180,1182,1180,1181,1180,1183,1180",
                    estimatedTimeMinutes = 10,
                    creatorName = "Kampala Cycling Club",
                    rating = 4.4,
                    isFavorite = false
                )
            )
            for (route in defaultRoutes) {
                appDao.insertRoute(route)
            }
        }

        // 5. Social Feed Posts
        val currentPosts = appDao.getAllPosts().firstOrNull()
        if (currentPosts.isNullOrEmpty()) {
            val defaultPosts = listOf(
                PostEntity(
                    id = 1,
                    authorName = "Arthur Mukasa",
                    authorLocation = "Jinja, Uganda",
                    authorAvatarSeed = "mukasa",
                    category = "General",
                    caption = "What an unforgettable ride morning along the shores of the Nile! The sun was breaking over the horizon with extreme mist. Together we can explore the best of Pearl of Africa! 🌅🚴‍♂️ #CWC #UgandaCyclists",
                    imageUrl = "ride_nile",
                    timestamp = System.currentTimeMillis() - 3600000 * 2, // 2 hrs ago
                    likesCount = 42,
                    commentsCount = 3,
                    isLiked = true,
                    isSaved = false
                ),
                PostEntity(
                    id = 2,
                    authorName = "Sarah Namubiru",
                    authorLocation = "Kampala, Uganda",
                    authorAvatarSeed = "namubiru",
                    category = "Achievement",
                    caption = "Just smashed my personal distance milestone! 100 KM Kampala-Entebbe-Kampala return loop complete. Legs are completely cooked, but the energy within the group is what pushed everyone through! Super grateful for CWC live location tracking, kept our back markers perfectly aligned. 🏅🎉💪",
                    imageUrl = "badge_100",
                    timestamp = System.currentTimeMillis() - 3600000 * 5, // 5 hrs ago
                    likesCount = 89,
                    commentsCount = 12,
                    isLiked = false,
                    isSaved = true
                ),
                PostEntity(
                    id = 3,
                    authorName = "Dennis Ssekitoleko",
                    authorLocation = "Wakiso, Uganda",
                    authorAvatarSeed = "dennis",
                    category = "Route",
                    caption = "Found a hidden mud single-track trail near Kigo hills. Perfect for intermediate mountain bike or gravel riders. Added standard GPS nodes to the database routes. Check out 'Kigo Gravel Hideout' flow! Let me know if you want to try it this Friday evening.",
                    imageUrl = "gravel_track",
                    timestamp = System.currentTimeMillis() - 3600000 * 24, // 1 day ago
                    likesCount = 28,
                    commentsCount = 2,
                    isLiked = false,
                    isSaved = false
                )
            )
            for (post in defaultPosts) {
                appDao.insertPost(post)
            }

            // Seed some comments
            appDao.insertComment(CommentEntity(1, 1, "Stuart Don", "avatar_stuart", "Spectacular view Arthur! Let's do a group cruise next Sunday.", System.currentTimeMillis() - 3600000))
            appDao.insertComment(CommentEntity(2, 1, "Sarah Namubiru", "namubiru", "Stunning sunrise! Kampala Gravel Riders need to schedule a ride out to Jinja soon.", System.currentTimeMillis() - 1800000))
            appDao.insertComment(CommentEntity(3, 2, "Dennis Ssekitoleko", "dennis", "Absolute machine, Sarah! 100K on those Entebbe grades is no joke! 🔥", System.currentTimeMillis() - 3600000 * 4))
        }

        // 6. Messages
        val currentMessages = appDao.getMessagesByChat("general").firstOrNull()
        if (currentMessages.isNullOrEmpty()) {
            val defaultMsgs = listOf(
                MessageEntity(
                    id = 1,
                    chatId = "general",
                    senderName = "Arthur Mukasa",
                    senderAvatarSeed = "mukasa",
                    text = "Hello team CWC! Is anyone up for a short gravel spin around Kololo hill this evening?",
                    timestamp = System.currentTimeMillis() - 7200000
                ),
                MessageEntity(
                    id = 2,
                    chatId = "general",
                    senderName = "Dennis Ssekitoleko",
                    senderAvatarSeed = "dennis",
                    text = "Double yes! I can meet you by the airstrip gate around 5:30 PM. I'll share my live tracking node on the community map.",
                    timestamp = System.currentTimeMillis() - 6000000
                ),
                MessageEntity(
                    id = 3,
                    chatId = "general",
                    senderName = "Stuart Don",
                    senderAvatarSeed = "avatar_stuart",
                    text = "Count me in guys! I am bringing the gravel rigs. Look out for local Kampala traffic.",
                    timestamp = System.currentTimeMillis() - 5400000
                )
            )
            for (msg in defaultMsgs) {
                appDao.insertMessage(msg)
            }
        }

        // 7. Marketplace Items
        val currentMarket = appDao.getAllMarketplaceItems().firstOrNull()
        if (currentMarket.isNullOrEmpty()) {
            val defaultItems = listOf(
                MarketplaceItemEntity(
                    id = 1,
                    title = "Giant Revolt Advanced 2 (2024)",
                    category = "Mountain Bikes",
                    priceUsd = 1450.0,
                    description = "Carbon gravel powerhouse with Shimano GRX 2x11 speed. Medium size. Only ridden 300km, upgraded to wireless. Barely a single scratch! Perfect for Ugandan backroads.",
                    sellerName = "Arthur Mukasa",
                    location = "Jinja",
                    phone = "+256 752 111222",
                    imageSeed = "item_bike_giant",
                    isApproved = true
                ),
                MarketplaceItemEntity(
                    id = 2,
                    title = "Lazer Genesis MIPS Lightweight Helmet",
                    category = "Helmets",
                    priceUsd = 120.0,
                    description = "Professional road cycling helmet. extremely well ventilated. Medium size, Neon Green color matching CWC. Brand new with box tags.",
                    sellerName = "Sarah Namubiru",
                    location = "Kampala",
                    phone = "+256 701 234567",
                    imageSeed = "item_helmet",
                    isApproved = true
                ),
                MarketplaceItemEntity(
                    id = 3,
                    title = "Specialized Torch 1.0 Road Shoes",
                    category = "Cycling Gear",
                    priceUsd = 65.0,
                    description = "Size 43 road cycling shoes with BOA dials. Used only twice, selling because size is slightly small for me. Cleats included.",
                    sellerName = "Dennis Ssekitoleko",
                    location = "Wakiso",
                    phone = "+256 788 333444",
                    imageSeed = "item_shoes",
                    isApproved = true
                )
            )
            for (item in defaultItems) {
                appDao.insertMarketplaceItem(item)
            }
        }

        // 8. Rides (Historical tracking data)
        val currentRides = appDao.getAllRides().firstOrNull()
        if (currentRides.isNullOrEmpty()) {
            val defaultRides = listOf(
                RideEntity(
                    id = 1,
                    durationSeconds = 3400,
                    distanceKm = 24.5,
                    averageSpeedKmh = 25.9,
                    maxSpeedKmh = 48.2,
                    caloriesBurned = 620,
                    elevationGainM = 310,
                    dateText = "June 18, 2026",
                    routeHistoryText = "0.302,32.582;0.308,32.585;0.315,32.592;0.320,32.596;0.310,32.584"
                ),
                RideEntity(
                    id = 2,
                    durationSeconds = 7120,
                    distanceKm = 52.1,
                    averageSpeedKmh = 26.3,
                    maxSpeedKmh = 54.0,
                    caloriesBurned = 1340,
                    elevationGainM = 540,
                    dateText = "June 14, 2026",
                    routeHistoryText = "0.332,32.612;0.358,32.655;0.365,32.682;0.350,32.645;0.332,32.612"
                )
            )
            for (ride in defaultRides) {
                appDao.insertRide(ride)
            }
        }
    }
}
