# FundTracker 📈

**FundTracker** is a modern, high-performance Android application built to simplify mutual fund exploration and portfolio management. It provides investors with real-time market data, interactive visualizations, and a flexible system to organize investments into custom portfolios.

---

## 🚀 Application & Functionality

FundTracker serves as a comprehensive tool for both casual and serious investors. The application handles the complexity of financial APIs and market fluctuations, presenting them through a clean, declarative UI. 

**Key Applications:**
* **Discovery:** Quickly find funds based on categories like Index, Bluechip, or Tax Savers.
* **Analysis:** Monitor 30-day performance trends via interactive NAV charts.
* **Organization:** Create multiple specialized portfolios (e.g., "Retirement", "Vacation", "Emergency Fund") and assign the same fund to multiple buckets.

---

✨ Features
📡 Smart Discovery & Caching
Experience a high-speed exploration engine that categorizes funds into Index, Bluechip, and Large-cap buckets. The system implements a sophisticated offline-first caching layer using Room, ensuring that real-time market data and live price indicators remain accessible even when you're off the grid.

💼 Advanced Portfolio Management
Move beyond basic tracking with a Many-to-Many relationship architecture. This allows you to add a single fund to multiple custom portfolios simultaneously, track "Saved" status via an intelligent heart-icon sensor, and manage cross-portfolio removals through guided, conflict-aware dialogs.

📊 Custom Data Visualization
Analyze market trends with a custom-built charting engine leveraging Jetpack Compose Canvas. The app transforms raw NAV history into smooth, gradient-filled line charts, providing a high-fidelity visual representation of a fund’s performance over the last 30 days.

🔍 Real-Time Search & Global View
Navigate the entire mutual fund universe with a high-performance search interface. Whether you are using the instant search bar or browsing the global directory, the app utilizes paginated loading and optimized API calls to ensure a lag-free experience across thousands of funds.

## 🛠 Tech Stack

FundTracker is built using the latest industry-standard tools and architectural patterns:

* **UI Framework:** Jetpack Compose (Declarative UI, Custom Canvas NAV Charts, and Dynamic Theming)
* **Architecture:** MVVM + Clean Architecture (Separation of concerns between UI, Business Logic, and Data Source)
* **Dependency Injection:** Hilt (Providing Singletons for Database, API Services, and Repository across ViewModels)
* **Local Database:** Room (Offline-First Explore Cache and Many-to-Many Portfolio/Fund relationships)
* **Networking:** Retrofit 2 & OkHttp 4 (Real-time Mutual Fund search and live NAV price fetching)
* **Concurrency:** Kotlin Coroutines & Flow (Reactive UI updates and background data synchronization)
* **Navigation:** Jetpack Navigation Component (Type-Safe routing between Explore, Portfolio, and Product Details)
* **State Management:** StateFlow & SharedFlow (Unidirectional Data Flow (UDF) to maintain UI consistency and handle lifecycle-aware data streams)

---

## 📋 Requirements

* **Minimum SDK:** API 24 (Android 7.0)
* **Target SDK:** API 34
* **Language:** Kotlin
* **Build System:** Gradle (Kotlin DSL)

---

## 🔧 Setup & Installation

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/jeet030106/Fund_Tracker.git](https://github.com/jeet030106/Fund_Tracker.git)
    ```
2.  **Open in Android Studio:** Ensure you are using the latest version of Android Studio (Hedgehog or higher).
3.  **Sync Gradle:** Allow the project to download all necessary dependencies.
4.  **Database Migration Note:** The project uses `fallbackToDestructiveMigration()`. During development, schema changes will reset the local database to prevent crashes.
5.  **Run:** Click the `Run` button in Android Studio to deploy to your emulator or physical device.

---

## 🗄️ Database Schema
The app utilizes four primary entities:
1.  **PortfolioEntity:** Stores user-created portfolio names and IDs.
2.  **FundEntity:** Caches specific fund metadata (Scheme Name, Category).
3.  **PortfolioFundCrossRef:** The junction table managing the Many-to-Many relationship.
4.  **ExploreCacheEntity:** Specialized table for high-speed offline access to the Explore page.

---

## 📄 License
Copyright © 2024 FundTracker. All rights reserved.
