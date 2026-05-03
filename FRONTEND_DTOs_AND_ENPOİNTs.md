```
data class PostExamDTO(
    val fullName : String,
    val no : String,
)
```
```
data class ResolvedExamDTO(
    val fullName: String,
    val no: String,
    val score: Double,
    val examImageUrl: String?
)
```

```
```
```
data class ResolvedGroupDTO(
    val id: Int,
    val name: String,
    val examCount: Int,
    val answerKeyImageUrl: String?,
    val exams: List<ResolvedExamDTO>
)
```
```
data class UserClientDTO(
val fullName: String,
val accessToken: String
)
```
```
data class UserSignInDTO(
val usernameOrEmail : String,
val password : String
)
```
```
data class UserSignUpDTO(
val fullName : String,
val email : String,
val username : String,
val password : String
)
```
```
interface ApiUserService {

    @POST("auth/signUp")
    suspend fun signUp(@Body userSignUpDto: UserSignUpDTO) : UserClientDTO

    @POST("auth/signIn")
    suspend fun signIn(@Body userSignInDto: UserSignInDTO) : UserClientDTO

    @GET("auth/user")
    suspend fun getUser(@Header("Authorization") accessToken : String) : UserClientDTO
}
```
```
interface ApiGroupService {

    @GET("classes")
    suspend fun getGroups(@Header("Authorization") accessToken: String) : List<ResolvedGroupDTO>

    @Multipart
    @POST("classes")
    suspend fun addAGroup(
        @Header("Authorization") accessToken: String,
        @Part("name") name: RequestBody,
        @Part answerKeyPhotos: List<MultipartBody.Part>
    ) : List<ResolvedGroupDTO>

    @Multipart
    @PUT("classes/{groupId}")
    suspend fun addExams(
        @Header("Authorization") accessToken: String,
        @Path("groupId") groupId: Int,
        @Part("postExamDTO") postExamDTO: PostExamDTO,
        @Part examPhotos: List<MultipartBody.Part>
    ) : ResolvedExamDTO

}
```



- `examImageUrl` auth korumali image URL'sidir
- `answerKeyImageUrl` group'a ait cevap anahtarinin auth korumali image URL'sidir
- frontend bu URL'leri Coil/Glide/Picasso benzeri image loader ile dogrudan kullanabilir