# TripShare - Android Travel Planning App

TripShare is a mobile application that helps users discover and plan trips by finding interesting places, saving favorites..

## Features

- **User Authentication**: Email/password login and registration via Firebase
- **Location Search**: Find places worldwide using Google Places API
- **Interest-Based Discovery**: Filter places by categories (restaurants, museums, parks, etc.)
- **Interactive Maps**: View locations on Google Maps
- **Favorites**: Save places you love for future reference
- **Place Details**: View comprehensive information about each location

## Screenshots


## Technologies Used

- **Kotlin**: Primary programming language
- **Firebase**: 
  - Authentication for user management
  - Firestore for data storage
  - Realtime Database for real-time updates
- **Google Maps & Places API**: Location services and place data
- **Volley**: Network requests
- **Glide**: Image loading and caching
- **Material Design Components**: UI elements and styling

## Architecture

The app follows a modular activity-based architecture with:
- View Models for data management
- Adapters for RecyclerView implementations
- Utils for shared functionality
- Activities for screen navigation and user interaction

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Minimum SDK: API 26 (Android 8.0)
- Google Maps API key
- Firebase project

### Installation

1. Clone the repository:
```
git clone https://github.com/yourusername/tripshare.git
```

2. Open the project in Android Studio

3. Add your Google Maps API key in the `AndroidManifest.xml` file:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE"/>
```

4. Connect your Firebase project:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download the `google-services.json` file and place it in the app directory
   - Follow Firebase setup instructions

5. Build and run the project

## Usage

1. **Sign Up/Login**: Create an account or log in with existing credentials
2. **Plan a Trip**: Enter your destination, travel dates, and interests
3. **Explore**: Browse places matching your criteria
4. **Save Favorites**: Like places to save them to your favorites
5. **View Details**: Tap on any place to see detailed information
6. **Navigate**: Use the "Navigate" button to open Google Maps for directions

## Future Enhancements

- Social sharing capabilities
- Trip cost estimation



1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
