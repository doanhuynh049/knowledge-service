# Change Log

All notable changes to the Topic Knowledge Service project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Enhanced JSON parsing with repair mechanisms for truncated AI responses
- Comprehensive logging for debugging AI response issues
- Better error handling for malformed JSON content

### Changed
- Improved email template styling with modern CSS
- Enhanced ContentParserService with robust JSON extraction
- Updated AI prompt structure for better programming-focused content

### Fixed
- JSON parsing errors when AI responses are truncated
- String.join compilation error in ContentParserService
- Email display issues with raw JSON content

## [1.0.0] - 2025-08-27

### Added
- Initial release of Topic Knowledge Service
- Automated content generation using Google Gemini AI
- Dual email system (overview and detailed emails)
- Excel-based topic management
- REST API for manual control
- Scheduled daily processing
- Rich HTML email templates with syntax highlighting
- Comprehensive error handling and logging
- Docker support and deployment guides
- Complete documentation suite

### Features
- **AI Integration**: Google Gemini API for content generation
- **Content Processing**: JSON parsing and HTML conversion
- **Email Service**: Professional HTML emails with CSS styling
- **Scheduling**: Daily automated processing at 5:00 AM Vietnam time
- **API Endpoints**: Manual trigger, specific topic processing, statistics
- **Excel Integration**: Topic management and status tracking
- **Database Support**: PostgreSQL and H2 database support
- **Monitoring**: Spring Boot Actuator endpoints
- **Configuration**: Profile-based configuration management

### Technical Stack
- **Framework**: Spring Boot 3.x
- **Java**: OpenJDK 17+
- **Database**: PostgreSQL (production), H2 (development)
- **AI**: Google Gemini API
- **Email**: Spring Mail with HTML templates
- **Build**: Maven 3.6+
- **Containerization**: Docker support

## [0.9.0] - 2025-08-26

### Added
- Core application structure and Spring Boot setup
- Basic AI client integration
- Initial email service implementation
- Topic repository and model definitions
- Excel service for topic management
- Basic scheduling functionality

### Technical Debt
- Need to improve JSON parsing robustness
- Email templates require enhancement
- Error handling needs refinement
- Documentation is incomplete

## [0.8.0] - 2025-08-25

### Added
- Project initialization
- Maven project structure
- Basic Spring Boot configuration
- Initial model definitions
- Development environment setup

### Infrastructure
- Spring Boot starter dependencies
- H2 database for development
- Basic logging configuration
- Maven build configuration

## Security Updates

### [1.0.0] - 2025-08-27
- Added secure handling of API keys through environment variables
- Implemented proper email credential management
- Added SSL/TLS configuration options
- Secured actuator endpoints

## Breaking Changes

### [1.0.0] - 2025-08-27
- **Configuration**: Moved from properties-based to environment variable configuration for sensitive data
- **Database**: Updated schema to support enhanced content tracking
- **API**: Standardized REST API response formats

## Migration Guide

### From 0.9.x to 1.0.0

#### Configuration Changes
1. **Environment Variables**: Update configuration to use environment variables for sensitive data:
   ```bash
   # Old (not recommended)
   app.llm-api-key=your-api-key-here
   
   # New (recommended)
   export GEMINI_API_KEY=your-api-key-here
   ```

2. **Database Schema**: Run migration scripts for enhanced content tracking:
   ```sql
   ALTER TABLE knowledge_content ADD COLUMN email_sent_at TIMESTAMP;
   ALTER TABLE topic ADD COLUMN last_processed TIMESTAMP;
   ```

3. **Email Templates**: Update email template references if customized:
   - `overview-email-template.html` → Enhanced CSS styling
   - `detailed-email-template.html` → Added code syntax highlighting

#### API Changes
- Standardized response formats for all endpoints
- Added comprehensive error responses
- Enhanced statistics endpoint with more metrics

#### Dependencies
- Updated Spring Boot from 2.x to 3.x
- Added new dependencies for JSON processing
- Enhanced email dependencies for HTML rendering

### From 0.8.x to 0.9.0

#### Database Changes
1. **New Tables**: Added tables for content tracking:
   ```sql
   CREATE TABLE knowledge_content (
       id BIGSERIAL PRIMARY KEY,
       topic_id BIGINT REFERENCES topic(id),
       overview_content TEXT,
       detailed_content TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

2. **Updated Tables**: Enhanced topic table:
   ```sql
   ALTER TABLE topic ADD COLUMN status VARCHAR(20) DEFAULT 'NEW';
   ALTER TABLE topic ADD COLUMN category VARCHAR(100);
   ```

#### Configuration Changes
- Added AI service configuration
- Enhanced email configuration options
- Added scheduling configuration

## Performance Improvements

### [1.0.0] - 2025-08-27
- **JSON Parsing**: Optimized JSON extraction and parsing performance
- **Email Generation**: Improved HTML template rendering speed
- **Database**: Added connection pooling and query optimization
- **Caching**: Implemented content caching for repeated requests

### [0.9.0] - 2025-08-26
- **AI Client**: Added retry logic and timeout handling
- **Database**: Optimized query performance with proper indexing
- **Scheduling**: Improved scheduler performance and reliability

## Bug Fixes

### [1.0.0] - 2025-08-27
- Fixed JSON parsing errors with truncated AI responses
- Resolved email template rendering issues
- Fixed scheduling timezone issues
- Corrected database connection pool configuration
- Fixed logging configuration for different environments

### [0.9.0] - 2025-08-26
- Fixed email delivery failures
- Resolved AI API timeout issues
- Fixed Excel file reading problems
- Corrected topic status tracking

## Known Issues

### [1.0.0] - 2025-08-27
- **AI Response Length**: Very long AI responses may still be truncated by the API
- **Email Delivery**: Some SMTP providers may require additional configuration
- **Excel Concurrency**: Multiple instances may conflict when accessing Excel files

## Dependencies

### [1.0.0] - 2025-08-27

#### Updated Dependencies
- `spring-boot-starter-parent`: 3.1.5
- `spring-boot-starter-web`: 3.1.5
- `spring-boot-starter-data-jpa`: 3.1.5
- `spring-boot-starter-mail`: 3.1.5
- `spring-boot-starter-webflux`: 3.1.5

#### New Dependencies
- `apache-poi`: 5.2.4 (Excel processing)
- `jackson-databind`: 2.15.2 (JSON processing)
- `lombok`: 1.18.30 (Code generation)

#### Development Dependencies
- `spring-boot-starter-test`: 3.1.5
- `testcontainers`: 1.19.1
- `mockito-core`: 5.5.0

## Documentation

### [1.0.0] - 2025-08-27
- Added comprehensive README with setup instructions
- Created detailed API documentation
- Added development guide with coding standards
- Created deployment guide for various environments
- Added configuration reference documentation
- Created troubleshooting guide

### [0.9.0] - 2025-08-26
- Basic README with installation instructions
- Initial API documentation
- Basic configuration examples

## Contributors

### [1.0.0] - 2025-08-27
- **Development Team**: Core application development and architecture
- **QA Team**: Testing and quality assurance
- **DevOps Team**: Deployment and infrastructure setup
- **Documentation Team**: Comprehensive documentation creation

## Future Roadmap

### [1.1.0] - Planned for Q4 2025
- **Enhanced AI**: Support for multiple AI providers (OpenAI, Claude, etc.)
- **User Interface**: Web-based dashboard for topic management
- **Advanced Scheduling**: Flexible scheduling with multiple time slots
- **Content Versioning**: Track content changes and improvements
- **Analytics**: Detailed analytics and reporting features

### [1.2.0] - Planned for Q1 2026
- **Multi-language Support**: Content generation in multiple languages
- **Content Categories**: Advanced categorization and tagging
- **Collaboration**: Multi-user support and team features
- **API Enhancement**: GraphQL API support
- **Mobile App**: Mobile application for content management

### [2.0.0] - Planned for Q2 2026
- **Microservices Architecture**: Split into focused microservices
- **Cloud Native**: Kubernetes-native deployment
- **Real-time Features**: WebSocket support for real-time updates
- **Machine Learning**: Content quality prediction and optimization
- **Integration Platform**: Support for external content management systems

## Support

For support, bug reports, and feature requests:

- **GitHub Issues**: Create issues for bugs and feature requests
- **Documentation**: Refer to the comprehensive documentation in `/docs`
- **Email**: Contact the development team for urgent issues
- **Community**: Join discussions and share experiences

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Changelog Maintained By**: Development Team  
**Last Updated**: August 27, 2025  
**Version**: 1.0.0
