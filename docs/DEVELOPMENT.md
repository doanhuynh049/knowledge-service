# Development Guide

## Table of Contents

1. [Development Environment Setup](#development-environment-setup)
2. [Project Structure](#project-structure)
3. [Coding Standards](#coding-standards)
4. [Testing Strategy](#testing-strategy)
5. [Debugging Guide](#debugging-guide)
6. [Contributing Guidelines](#contributing-guidelines)
7. [Release Process](#release-process)

## Development Environment Setup

### Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.6+**
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**
- **Docker** (optional, for containerized development)

### IDE Setup

#### IntelliJ IDEA

1. **Import Project**: File → Open → Select `pom.xml`
2. **Configure JDK**: File → Project Structure → Project SDK → Java 17
3. **Enable Annotation Processing**: Build → Compiler → Annotation Processors → Enable
4. **Install Plugins**:
   - Lombok Plugin
   - Spring Boot Assistant
   - SonarLint

#### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Configure Settings**:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.compile.nullAnalysis.mode": "automatic",
     "spring-boot.ls.checkspringbootversion": false
   }
   ```

### Local Development

```bash
# Clone repository
git clone <repository-url>
cd knowledge-service

# Setup development profile
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties

# Edit configuration
vim src/main/resources/application-dev.properties

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run with hot reload
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev -Dspring.devtools.restart.enabled=true"
```

### Development Configuration

Create `application-dev.properties`:

```properties
# Development Database (H2)
spring.datasource.url=jdbc:h2:mem:devdb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.com.knowledge.topic=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.mail=DEBUG

# Development Settings
app.schedule-enabled=false
app.daily-topic-limit=5

# Mock AI responses for development
app.mock-ai-responses=true
app.mock-email-sending=true
```

## Project Structure

```
knowledge-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/knowledge/topic/
│   │   │       ├── TopicKnowledgeServiceApplication.java
│   │   │       ├── controller/
│   │   │       │   └── TopicController.java
│   │   │       ├── dto/
│   │   │       │   ├── GeminiRequest.java
│   │   │       │   ├── GeminiResponse.java
│   │   │       │   ├── TopicDetail.java
│   │   │       │   └── TopicOverview.java
│   │   │       ├── model/
│   │   │       │   ├── KnowledgeContent.java
│   │   │       │   ├── Topic.java
│   │   │       │   └── TopicStatus.java
│   │   │       ├── repository/
│   │   │       │   ├── KnowledgeContentRepository.java
│   │   │       │   └── TopicRepository.java
│   │   │       ├── scheduler/
│   │   │       │   └── TopicScheduler.java
│   │   │       └── service/
│   │   │           ├── ContentGenerationService.java
│   │   │           ├── ContentParserService.java
│   │   │           ├── GeminiTopicClient.java
│   │   │           ├── TopicEmailService.java
│   │   │           ├── TopicExcelService.java
│   │   │           └── TopicProcessingService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── detailed-email-template.html
│   │       └── overview-email-template.html
│   └── test/
│       ├── java/
│       │   └── com/knowledge/topic/
│       │       ├── integration/
│       │       ├── service/
│       │       └── controller/
│       └── resources/
│           ├── application-test.properties
│           └── test-data/
├── docs/
│   ├── README.md
│   ├── API.md
│   └── DEVELOPMENT.md
├── topics.xlsx
├── pom.xml
└── README.md
```

### Package Responsibilities

- **`controller/`**: REST API endpoints and request handling
- **`dto/`**: Data Transfer Objects for API communication
- **`model/`**: JPA entities and domain models
- **`repository/`**: Data access layer interfaces
- **`scheduler/`**: Scheduled task management
- **`service/`**: Business logic and orchestration

## Coding Standards

### Java Conventions

1. **Naming Conventions**:
   ```java
   // Classes: PascalCase
   public class TopicProcessingService {}
   
   // Methods: camelCase
   public void processTopics() {}
   
   // Constants: UPPER_SNAKE_CASE
   private static final String DEFAULT_CATEGORY = "General";
   
   // Variables: camelCase
   private List<Topic> topicsToProcess;
   ```

2. **Class Structure**:
   ```java
   @Service
   @Slf4j
   @RequiredArgsConstructor
   public class TopicProcessingService {
       
       // Static constants
       private static final int DEFAULT_LIMIT = 10;
       
       // Dependencies (injected via constructor)
       private final TopicRepository topicRepository;
       private final ContentGenerationService contentService;
       
       // Configuration properties
       @Value("${app.daily-topic-limit}")
       private int dailyTopicLimit;
       
       // Public methods
       @Transactional
       public void processDailyTopics() {
           // Implementation
       }
       
       // Private helper methods
       private void markTopicsAsProcessing(List<Topic> topics) {
           // Implementation
       }
   }
   ```

3. **Error Handling**:
   ```java
   public void processTopics(List<Topic> topics) {
       try {
           // Main logic
           log.info("Processing {} topics", topics.size());
           
       } catch (SpecificException e) {
           log.error("Specific error occurred: {}", e.getMessage(), e);
           throw new ProcessingException("Processing failed", e);
           
       } catch (Exception e) {
           log.error("Unexpected error: {}", e.getMessage(), e);
           throw new RuntimeException("Unexpected processing failure", e);
       }
   }
   ```

4. **Logging Standards**:
   ```java
   // Use appropriate log levels
   log.trace("Detailed debugging info");
   log.debug("General debugging info");
   log.info("Important business events");
   log.warn("Recoverable errors or unusual conditions");
   log.error("Serious errors requiring attention");
   
   // Include context in log messages
   log.info("Processing topic: {} with status: {}", topic.getName(), topic.getStatus());
   
   // Log exceptions with full stack trace
   log.error("Failed to process topic: {}", topic.getName(), e);
   ```

### Spring Boot Best Practices

1. **Dependency Injection**:
   ```java
   // Prefer constructor injection
   @Service
   @RequiredArgsConstructor
   public class MyService {
       private final MyRepository repository;
   }
   
   // Avoid field injection
   @Autowired
   private MyRepository repository; // Don't do this
   ```

2. **Configuration**:
   ```java
   // Use @ConfigurationProperties for complex configurations
   @ConfigurationProperties(prefix = "app")
   @Data
   public class AppProperties {
       private int dailyTopicLimit = 1;
       private boolean scheduleEnabled = true;
       private String llmProvider;
   }
   ```

3. **Transaction Management**:
   ```java
   // Use @Transactional appropriately
   @Transactional(readOnly = true)
   public List<Topic> findUnprocessedTopics() {
       return topicRepository.findByStatus(TopicStatus.NEW);
   }
   
   @Transactional
   public void processTopics(List<Topic> topics) {
       // Modifying operations
   }
   ```

## Testing Strategy

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class TopicProcessingServiceTest {
    
    @Mock
    private TopicRepository topicRepository;
    
    @Mock
    private ContentGenerationService contentService;
    
    @InjectMocks
    private TopicProcessingService processingService;
    
    @Test
    void shouldProcessTopicsSuccessfully() {
        // Given
        List<Topic> topics = Arrays.asList(
            new Topic("Lambda Expressions", "Functional Programming")
        );
        when(topicRepository.findByStatus(TopicStatus.NEW))
            .thenReturn(topics);
        
        // When
        processingService.processDailyTopics();
        
        // Then
        verify(contentService).generateContentForTopics(topics);
        verify(topicRepository).saveAll(argThat(savedTopics -> 
            savedTopics.get(0).getStatus() == TopicStatus.DONE));
    }
    
    @Test
    void shouldHandleContentGenerationFailure() {
        // Given
        List<Topic> topics = Arrays.asList(new Topic("Test", "Category"));
        when(contentService.generateContentForTopics(any()))
            .thenThrow(new RuntimeException("AI API failed"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> 
            processingService.processTopics(topics));
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "app.schedule-enabled=false",
    "app.mock-ai-responses=true"
})
class TopicControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Test
    void shouldTriggerProcessingSuccessfully() {
        // Given
        Topic topic = new Topic("Test Topic", "Test Category");
        topicRepository.save(topic);
        
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/topics/trigger", null, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("successfully");
    }
}
```

### Test Data Management

```java
@TestConfiguration
public class TestDataConfiguration {
    
    @Bean
    @Primary
    public GeminiTopicClient mockGeminiClient() {
        return Mockito.mock(GeminiTopicClient.class);
    }
    
    @EventListener
    public void setupTestData(ApplicationReadyEvent event) {
        // Setup test data for integration tests
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TopicProcessingServiceTest

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run with coverage
mvn test jacoco:report

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

## Debugging Guide

### Common Issues

1. **AI API Timeouts**:
   ```java
   // Add timeout configuration
   @Value("${app.ai-timeout:60}")
   private int aiTimeoutSeconds;
   
   // Implement timeout in WebClient
   webClient.post()
       .retrieve()
       .bodyToMono(GeminiResponse.class)
       .timeout(Duration.ofSeconds(aiTimeoutSeconds))
       .block();
   ```

2. **JSON Parsing Errors**:
   ```java
   // Add debug logging
   log.debug("Raw AI response: {}", response.substring(0, 
       Math.min(response.length(), 500)));
   
   // Implement fallback parsing
   try {
       return parseStructuredJson(response);
   } catch (JsonProcessingException e) {
       log.warn("JSON parsing failed, using fallback: {}", e.getMessage());
       return parseAsPlainText(response);
   }
   ```

3. **Email Delivery Issues**:
   ```java
   // Test email configuration
   @Component
   public class EmailTestComponent {
       
       @EventListener
       public void testEmailOnStartup(ApplicationReadyEvent event) {
           if (isTestEnvironment()) {
               try {
                   sendTestEmail();
                   log.info("Email configuration test passed");
               } catch (Exception e) {
                   log.error("Email configuration test failed: {}", e.getMessage());
               }
           }
       }
   }
   ```

### Debug Configuration

```properties
# Enable debug logging
logging.level.com.knowledge.topic=DEBUG
logging.level.org.springframework.web.client=DEBUG
logging.level.org.springframework.mail=DEBUG

# Enable Spring Boot debug mode
debug=true

# Enable actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,loggers

# H2 console for database debugging
spring.h2.console.enabled=true
```

### Debugging Tools

1. **Application Insights**:
   ```bash
   # Check application health
   curl http://localhost:8283/actuator/health
   
   # View metrics
   curl http://localhost:8283/actuator/metrics
   
   # Check logger levels
   curl http://localhost:8283/actuator/loggers
   ```

2. **Database Debugging**:
   ```bash
   # H2 Console: http://localhost:8283/h2-console
   # JDBC URL: jdbc:h2:mem:devdb
   
   # Check topics
   SELECT * FROM topic WHERE status = 'NEW';
   
   # Check generated content
   SELECT t.name, kc.detailed_word_count, kc.created_at 
   FROM topic t 
   JOIN knowledge_content kc ON t.id = kc.topic_id;
   ```

3. **Profile-based Debugging**:
   ```bash
   # Run with debug profile
   mvn spring-boot:run -Dspring-boot.run.profiles=debug
   
   # Enable remote debugging
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```

## Contributing Guidelines

### Git Workflow

1. **Branch Naming**:
   ```bash
   # Feature branches
   git checkout -b feature/add-topic-categories
   
   # Bug fixes
   git checkout -b bugfix/fix-json-parsing
   
   # Hotfixes
   git checkout -b hotfix/critical-email-issue
   ```

2. **Commit Messages**:
   ```bash
   # Format: type(scope): description
   
   # Examples
   git commit -m "feat(email): add syntax highlighting for code blocks"
   git commit -m "fix(parser): handle truncated JSON responses"
   git commit -m "docs(api): update endpoint documentation"
   git commit -m "test(service): add unit tests for topic processing"
   ```

3. **Pull Request Process**:
   - Create feature branch from `main`
   - Implement changes with tests
   - Update documentation
   - Submit PR with description
   - Address review comments
   - Merge after approval

### Code Review Checklist

- [ ] Code follows project conventions
- [ ] Unit tests added for new functionality
- [ ] Integration tests updated if needed
- [ ] Documentation updated
- [ ] Error handling implemented
- [ ] Logging added appropriately
- [ ] Performance considerations addressed
- [ ] Security implications reviewed

### Development Workflow

```bash
# 1. Setup feature branch
git checkout main
git pull origin main
git checkout -b feature/new-feature

# 2. Make changes
# ... implement feature ...

# 3. Test locally
mvn clean test
mvn spring-boot:run -Dspring.profiles.active=dev

# 4. Commit and push
git add .
git commit -m "feat(scope): description"
git push origin feature/new-feature

# 5. Create pull request
# ... via GitHub/GitLab ...

# 6. After approval, merge and cleanup
git checkout main
git pull origin main
git branch -d feature/new-feature
```

## Release Process

### Versioning

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR.MINOR.PATCH** (e.g., 1.2.3)
- **MAJOR**: Breaking changes
- **MINOR**: New features, backwards compatible
- **PATCH**: Bug fixes, backwards compatible

### Release Steps

1. **Prepare Release**:
   ```bash
   # Update version in pom.xml
   mvn versions:set -DnewVersion=1.2.0
   
   # Update CHANGELOG.md
   vim CHANGELOG.md
   
   # Run full test suite
   mvn clean verify
   ```

2. **Create Release**:
   ```bash
   # Commit version changes
   git add pom.xml CHANGELOG.md
   git commit -m "release: version 1.2.0"
   
   # Create tag
   git tag -a v1.2.0 -m "Release version 1.2.0"
   
   # Push changes and tag
   git push origin main
   git push origin v1.2.0
   ```

3. **Build and Deploy**:
   ```bash
   # Build production JAR
   mvn clean package -Pprod
   
   # Build Docker image
   docker build -t knowledge-service:1.2.0 .
   
   # Deploy to production
   # ... deployment steps ...
   ```

### Environment Promotion

```
Development → Testing → Staging → Production
     ↓           ↓        ↓          ↓
   Feature    Integration System   Production
   Testing     Testing    Testing    Release
```

### Hotfix Process

```bash
# Create hotfix branch from main
git checkout main
git checkout -b hotfix/critical-fix

# Implement fix
# ... fix implementation ...

# Test thoroughly
mvn clean verify

# Merge to main and develop
git checkout main
git merge hotfix/critical-fix
git checkout develop
git merge hotfix/critical-fix

# Tag and release
git tag -a v1.2.1 -m "Hotfix version 1.2.1"
git push origin main develop v1.2.1
```

---

**Last Updated**: August 27, 2025  
**Development Guide Version**: 1.0.0
