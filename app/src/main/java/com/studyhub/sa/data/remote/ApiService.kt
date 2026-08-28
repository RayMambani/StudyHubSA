package com.studyhub.sa.data.remote

import com.studyhub.sa.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleSignIn(@Body body: GoogleAuthRequest): Response<AuthResponse>

    @GET("api/users/me")
    suspend fun getProfile(): Response<UserDto>

    @PATCH("api/users/settings")
    suspend fun updateSettings(@Body body: SettingsUpdateRequest): Response<Map<String, String>>

    @PATCH("api/users/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<Map<String, String>>

    @GET("api/resources")
    suspend fun getResources(
        @Query("module") module: String? = null,
        @Query("search") search: String? = null
    ): Response<List<ResourceDto>>

    @Multipart
    @POST("api/resources")
    suspend fun uploadResource(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("module") module: RequestBody
    ): Response<Map<String, String>>

    @GET("api/groups")
    suspend fun getGroups(): Response<List<StudyGroupDto>>

    @POST("api/groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): Response<Map<String, String>>

    @POST("api/groups/{id}/join")
    suspend fun joinGroup(@Path("id") groupId: String): Response<Map<String, Boolean>>

    @GET("api/groups/{id}/messages")
    suspend fun getMessages(@Path("id") groupId: String): Response<List<MessageDto>>

    @POST("api/groups/{id}/messages")
    suspend fun sendMessage(@Path("id") groupId: String, @Body body: SendMessageRequest): Response<Map<String, String>>

    @GET("api/notes")
    suspend fun getNotes(): Response<List<NoteSyncItem>>

    @POST("api/notes/sync")
    suspend fun syncNotes(@Body body: NoteSyncRequest): Response<NoteSyncResponse>

    @GET("api/events")
    suspend fun getEvents(): Response<List<EventDto>>

    @POST("api/events")
    suspend fun createEvent(@Body body: CreateEventRequest): Response<Map<String, String>>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<Map<String, Boolean>>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") notificationId: String): Response<Map<String, Boolean>>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: String): Response<Map<String, Boolean>>

    @POST("api/users/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Map<String, Boolean>>

    @Multipart
    @POST("api/users/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @GET("api/resources/{id}/comments")
    suspend fun getComments(@Path("id") resourceId: String): Response<List<CommentDto>>

    @POST("api/resources/{id}/comments")
    suspend fun postComment(@Path("id") resourceId: String, @Body body: CreateCommentRequest): Response<Map<String, String>>

    @POST("api/ai/summarize")
    suspend fun summarize(@Body body: SummarizeRequest): Response<SummarizeResponse>

    @POST("api/ai/ask")
    suspend fun askQuestion(@Body body: AskRequest): Response<AskResponse>

    @GET("api/ai/recommendations")
    suspend fun getRecommendations(): Response<RecommendationsResponse>
}
