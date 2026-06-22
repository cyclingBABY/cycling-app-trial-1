package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Single active user profile
    val isLoggedIn: Boolean = false,
    val username: String = "SturRide",
    val fullName: String = "Stuart Don",
    val bio: String = "Avid cyclist & route explorer. Loving the gravel tracks of Kampala! 🚴‍♂️",
    val location: String = "Kampala, Uganda",
    val cyclingLevel: String = "Advanced Rider", // Beginner, Intermediate, Advanced, Professional
    val bikeType: String = "Gravel Bike", // Road, MTB, Gravel, Hybrid
    val emergencyContactName: String = "Jane Don",
    val emergencyContactPhone: String = "+256 701 234567",
    val totalDistanceRidden: Double = 1240.5,
    val followerCount: Int = 342,
    val followingCount: Int = 189,
    val profilePhotoUri: String = "",
    val coverPhotoUri: String = "",
    val achievementsString: String = "First Ride,50 KM,100 KM,Club Champion" // Comma-separated Achievements/Badges
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorLocation: String,
    val authorAvatarSeed: String, // visual avatar reference
    val category: String = "General", // General, Route, Achievement, Club
    val caption: String,
    val imageUrl: String = "", // Seed or hex resource for drawable
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val videoUrl: String = "",
    val videoDurationSec: Int = 0,
    val isPhotoGallery: Boolean = false,
    val gallerySeeds: String = "",
    val linkedRideId: Long = 0L
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarSeed: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationSeconds: Long,
    val distanceKm: Double,
    val averageSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val caloriesBurned: Int,
    val elevationGainM: Int,
    val dateText: String,
    val routeHistoryText: String, // Encoded coordinate string "lat,lng;lat,lng;lat,lng"
    val isSavedToRoutes: Boolean = false
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeName: String,
    val description: String,
    val distanceKm: Double,
    val difficulty: String, // Easy, Moderate, Hard, Demanding
    val elevationProfileString: String, // String representation of elevation points
    val estimatedTimeMinutes: Int,
    val creatorName: String,
    val rating: Double = 4.5,
    val isFavorite: Boolean = false
)

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val location: String = "Uganda",
    val membersCount: Int = 1,
    val isJoined: Boolean = false,
    val isApproved: Boolean = true,
    val recentAnnouncement: String = "",
    val logoSeed: String = "",
    val bannerSeed: String = ""
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val dateText: String,
    val timeText: String,
    val location: String,
    val organizer: String,
    val registrationLimit: Int = 50,
    val registeredCount: Int = 12,
    val isRegistered: Boolean = false,
    val isCancelled: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String, // "general" or "userId"
    val senderName: String,
    val senderAvatarSeed: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val isLocationShare: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Entity(tableName = "marketplace_items")
data class MarketplaceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Road Bikes, Mountain Bikes, Helmets, Cycling Gear, Accessories, Spare Parts
    val priceUsd: Double,
    val description: String,
    val sellerName: String,
    val location: String = "Kampala",
    val phone: String = "+256700000000",
    val imageSeed: String = "",
    val isApproved: Boolean = true
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val emailOrPhone: String,
    val passwordPlain: String,
    val fullName: String,
    val username: String,
    val isAdmin: Boolean = false
)
