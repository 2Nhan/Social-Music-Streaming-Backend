# 🎵 Social Music Streaming Backend
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Cloudflare R2](https://img.shields.io/badge/Cloudflare_R2-Storage-F38020?logo=cloudflare&logoColor=white)](https://www.cloudflare.com/developer-platform/r2/)
This is a robust music streaming backend API inspired by SoundCloud. It provides a complete ecosystem for uploading, processing, and streaming audio content via **HLS (HTTP Live Streaming)**, along with rich social features like following, liking, reposting, and commenting.
---
## 📋 Table of Contents
- [🎯 Key Features](#-key-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Architecture](#-architecture)
- [🚀 Getting Started](#-getting-started)
- [📚 API Documentation](#-api-documentation)
- [📄 License](#-license)
---
## 🎯 Key Features
### 🔐 Authentication & Security
- **Stateless Auth**: Secure JWT-based authentication (Access + Refresh Token).
- **Role-Based Access Control (RBAC)**: Fine-grained permissions for users and admins.
### 🎧 Streaming & Content
- **HLS Streaming**: Adaptive bitrate streaming support using FFmpeg processing.
- **Cloud Storage**: Seamless integration with **Cloudflare R2** (S3-compatible) for storing media and HLS segments.
- **Song Management**: Upload, metadata handling, and processing workflows.
### 🤝 Social Interactions
- **Engagement**: Like, Repost, and Comment on songs.
- **Social Graph**: Follow/Unfollow users to build a network.
- **Listening History**: Track user listening habits.
### 📂 Organization
- **Playlists**: Create, update, and manage personal playlists.
- **User Profiles**: Customizable user profiles and activity feeds.
---
## 🛠️ Tech Stack
### 📝 Core
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.3
### 🧱 Infrastructure & Data
- **Database**: MySQL 8+ 
- **Storage**: Cloudflare R2 (via AWS SDK v2)
- **Media Processing**: FFmpeg (HLS Transcoding)
### 📚 Libraries & Tools
- **Security**: Spring Security, Nimbus JOSE+JWT
- **Utilities**: Lombok, MapStruct, Apache Commons
- **Build Tool**: Maven
---
## 🏗️ Architecture
The project follows a **Monolithic Layered Architecture**:
`	ext
src/main/java/com/tunhan/micsu/
├── config/       # Configuration 
├── controller/   # REST API Layer
├── service/      # Business Logic
├── repository/   # Data Access Layer
├── entity/       # Domain Models (JPA)
├── dto/          # Data Transfer Objects
├── security/     # JWT & Auth Filters
└── utils/        # Helper Utilities (HLS, File handling)
`
**Key Workflows:**
1. **Upload**: Client uploads audio -> Async Service processes with FFmpeg -> HLS segments stored in Cloudflare R2 -> Database updated.
2. **Stream**: Application serves .m3u8 playlists linking to storage URLs.
---

