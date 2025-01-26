
# Auth Services

Authentication Services with Spring Boot for Backend and ReactJS for Frontend

**Branch** `main`: JWT with nimbus-jose-jwt (oauth2-resource-server) 

**Branch** `jwt-with-iojsonwebtoken`: JWT with iojsonwebtoken - manual config. 

**Branch** `session-auth`: JWT with Spring Session and Redis. 


# Tính năng

- Đăng ký, Đăng nhập
- Xác thực bằng email và mật khẩu.
- Kiểm soát quyền truy cập theo vai trò với Vai trò và Quyền hạn.
- Xác thực JWT.
- Blacklist token với Redis
- Xác thực bằng Session.
- Lưu trữ session trong Redis.
- Chuẩn hóa ApiResponse và handle lỗi bằng GlobalExceptionHandler
- Unit Test và Integration Test
- Kiểm thử sử dụng TestContainer cho môi trường cô lập.
- SonarQube để duy trì chất lượng mã nguồn.
- Deploy SonarQube lên EC2 để CICD với Github Actions.

# JWT
### Khởi tạo token
```java
public String generateToken(UserEntity user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("cungthinh")
                .claim("email", user.getEmail())
                .claim("scope", buildScope(user))
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expired_duration, ChronoUnit.SECONDS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Khởi tạo token bị lỗi");
        }
    }
```
### Xác thực token
```java
public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime;
        if (isRefresh) {
            expirationTime = new Date(signedJWT
                    .getJWTClaimsSet()
                    .getIssueTime()
                    .toInstant()
                    .plus(refreshable_duration, ChronoUnit.SECONDS)
                    .toEpochMilli());
        } else {
            expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        }

        if (!signedJWT.verify(verifier)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (expirationTime.before(new Date())) {
            //            log.info("Token hết hạn");
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }

        if (jwtBlackListService.isTokenBlacklisted(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }
```

### Integration Test
```java
...
public class UserControllerIntegrationTest {
    ...

    @BeforeEach
    void initData() {
        UserEntity testUser = UserEntity.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        savedUser = userResipotory.saveAndFlush(testUser);
    }
...
    @Test
    @WithMockUser(
            username = "test-uuid-123",
            roles = {"USER"})
    void getMyInfo_validRequest_success() throws Exception {
        // Now use @WithMockUser with the generated ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me")
                        .with(user(savedUser.getId()).roles("USER")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
```

# Session
```java
private final RedisIndexedSessionRepository redisIndexedSessionRepository;

    public SecurityConfig(RedisIndexedSessionRepository redisIndexedSessionRepository) {
        this.redisIndexedSessionRepository = redisIndexedSessionRepository;
    }
```

```java
.sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(IF_REQUIRED) //
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession) //
                        .maximumSessions(1) //
                        .sessionRegistry(sessionRegistry()));
```

### Sử dụng SpringSessionBackedSessionRegistry để lưu trữ Session
```java
@Bean
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(this.redisIndexedSessionRepository);
    }

```
### Sử dụng Test Container
```java
@Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("authservices")
            .withUsername("postgres")
            .withPassword("dontwastetime");

    @Container
    private static final RedisContainer redis =
            new RedisContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        // primary
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
```

### Test session có bị invalidate không
```java
@Test
    void loginOnSecondLoginThenFirstSessionTerminated() throws Exception {
        // Set max session to 1
        MvcResult mvcResult = this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String sessionCookie = mvcResult.getResponse().getCookie("JSESSIONID").getValue();

        this.mvc
                .perform(get("/api/v1/auth/session-info").cookie(new Cookie("JSESSIONID", sessionCookie)))
                .andExpect(status().isOk());

        log.info("Second login");
        this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk());

        this.mvc
                .perform(get("/api/v1/auth/session-info").cookie(new Cookie("JSESSIONID", sessionCookie)))
                .andExpect(status().isForbidden()); // Session terminated
    }
```

# CICD với Github Actions
``` yaml
name: CI/CD workflow for Maven Build and Sonar Code scan
on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'  # Chỉ chạy khi có thay đổi trong thư mục backend
  workflow_dispatch:

jobs:

  build:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:latest
        ports:
          - 5432:5432
        env:
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: dontwastetime
          POSTGRES_DB: authservices
        options: >-
          --health-cmd="pg_isready -U postgres"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
          --health-start-period=30s
    steps:
    - name: Checkout code
      uses: actions/checkout@v2
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        distribution: 'adopt'
        java-version: '21'
    - name: Build with Maven
      run: mvn clean install -X -f backend/authservices/pom.xml
    - name: SonarQube Scan
      uses: sonarsource/sonarqube-scan-action@master
      with:
        projectBaseDir: backend/authservices 
        args: >
          -Dsonar.organization=my-org
          -Dsonar.projectKey=my-Java-web-app
          -Dsonar.java.binaries=target/classes
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

### Deploy SonarQube lên EC2
``` yaml
version: '3'

services:
  sonarqube:
    image: sonarqube:community
    container_name: sonarqube
    ports:
      - "9000:9000"
    environment:
      - sonar.jdbc.username=sonar
      - sonar.jdbc.password=sonar
      - sonar.jdbc.url=jdbc:postgresql://db:5432/sonar
    depends_on:
      - db
    restart: always

  db:
    image: postgres:13
    container_name: postgres
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
      POSTGRES_DB: sonar
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: always

volumes:
  postgres_data:
```



