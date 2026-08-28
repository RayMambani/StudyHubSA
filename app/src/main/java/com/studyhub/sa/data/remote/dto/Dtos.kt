package com.studyhub.sa.data.remote.dto

data class RegisterRequest(
    val fullName: String,
    val studentNumber: String?,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val fullName: String,
    val studentNumber: String?,
    val email: String,
    val authProvider: String,
    val profileImage: String?,
    val language: String,
    val privacy: String? = null,
    val uploadsCount: Int? = null,
    val groupsCount: Int? = null,
    val streak: Int? = null,
    val points: Int? = null,
    val badges: List<String>? = null
)

data class SettingsUpdateRequest(
    val language: String? = null,
    val fcmToken: String? = null,
    val privacy: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String?,
    val newPassword: String
)

data class UpdateProfileRequest(
    val fullName: String? = null,
    val studentNumber: String? = null
)

data class EventDto(
    val id: String,
    val title: String,
    val event_date: String,
    val event_time: String?,
    val type: String
)

data class CreateEventRequest(
    val title: String,
    val eventDate: String,
    val eventTime: String? = null,
    val type: String = "personal"
)

data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val is_read: Int,
    val created_at: String
)

data class ResourceDto(
    val id: String,
    val title: String,
    val description: String?,
    val module: String?,
    val file_url: String?,
    val file_type: String?,
    val uploader_id: String,
    val upload_date: String
)

data class StudyGroupDto(
    val id: String,
    val name: String,
    val module: String?,
    val description: String?,
    val created_by: String,
    val member_count: Int
)

data class MessageDto(
    val id: String,
    val group_id: String,
    val sender_id: String,
    val content: String,
    val sent_at: String
)

data class SendMessageRequest(val content: String)
data class CreateGroupRequest(val name: String, val module: String?, val description: String?)

data class NoteSyncItem(
    val id: String,
    val title: String,
    val content: String?,
    val createdAt: String,
    val updatedAt: String
)

data class NoteSyncRequest(val notes: List<NoteSyncItem>)
data class NoteSyncResponse(val syncedIds: List<String>)

data class CommentDto(
    val id: String,
    val resource_id: String,
    val author_name: String,
    val content: String,
    val created_at: String
)

data class CreateCommentRequest(val content: String)

data class SummarizeRequest(val text: String)
data class SummarizeResponse(val summary: String)
data class AskRequest(val question: String)
data class AskResponse(val answer: String)
data class RecommendationsResponse(val recommendations: List<String>)
