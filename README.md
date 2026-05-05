# Wealthwise - Personal Finance Manager

Wealthwise is a comprehensive Android application designed to help users take control of their financial health. It provides tools for tracking transactions, managing budgets, and monitoring savings goals with intuitive visualizations.

## Features

### 1. Dashboard & Reports
- **Overview**: Get a quick glance at your current balance, recent transactions, and spending trends.
- **Visual Analytics**: View your spending habits through interactive charts (powered by MPAndroidChart), including category-wise breakdowns and monthly reports.

### 2. Transaction Management
- **Add Transactions**: Easily record income and expenses.
- **Categorization**: Assign categories to your transactions for better organization.
- **History**: Review your full transaction history to stay on top of your spending.

### 3. Budgeting
- **Set Limits**: Define monthly budget limits for different categories.
- **Track Progress**: Monitor how much of your budget remains in real-time.

### 4. Savings Goals
- **Goal Setting**: Define specific financial targets (e.g., "Emergency Fund", "New Car").
- **Progress Tracking**: Visualize your progress towards each goal with circular progress indicators.

### 5. Secure Authentication
- **Firebase Auth**: Securely sign up and log in to sync your data across devices.
- **Firestore**: Cloud-based storage ensures your financial data is always backed up and accessible.

## Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: Room Persistence Library
- **Cloud Database & Auth**: Firebase (Firestore & Authentication)
- **UI Components**: Jetpack Compose / Material Design 3
- **Navigation**: Jetpack Navigation Component
- **Image Loading**: Glide
- **Charts**: MPAndroidChart

## Getting Started

### Prerequisites
- Android Studio Iguana or newer
- Android SDK 34 (Upside Down Cake) or higher
- A Firebase project (for cloud features)

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/wealthwise.git
   ```
2. **Open in Android Studio**:
   - Select `File > Open` and navigate to the project folder.
3. **Firebase Configuration**:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.wealthwise`.
   - Download the `google-services.json` file and place it in the `app/` directory.
   - Enable Email/Password authentication in the Firebase Auth settings.
   - Set up a Cloud Firestore database.
4. **Sync Gradle**:
   - Click "Sync Project with Gradle Files" in Android Studio.
5. **Run the App**:
   - Connect an Android device or start an emulator and click the "Run" button.

## How to Use
1. **Sign Up**: Create an account to start tracking your finances.
2. **Dashboard**: Use the bottom navigation to switch between Home, Add Transaction, Reports, Budget, and Settings.
3. **Add Transaction**: Tap the '+' button in the navigation bar to record a new expense or income.
4. **View Reports**: Check the 'Reports' tab for detailed charts of your spending by category.
5. **Set Budgets**: Go to the 'Budget' tab to manage your monthly spending limits.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
