# Contributing to AI-Based Recommendation System

Thank you for your interest in contributing to the **AI-Based Recommendation System** project! 🎉

We welcome contributions of all kinds, including new recommendation algorithms, UI enhancements, documentation improvements, and bug fixes.

---

## 🛠️ Development Setup

1. **Clone or download the repository:**
   ```bash
   git clone https://github.com/your-username/ai-recommendation-system.git
   cd ai-recommendation-system
   ```

2. **Prerequisites:**
   - Java Development Kit (JDK 17 or higher)
   - Any modern web browser (Chrome, Edge, Firefox, Safari)

3. **Run Locally:**
   - On Windows: Run `run.bat` or `.\run.ps1`
   - On Linux/macOS:
     ```bash
     mkdir -p bin
     javac -encoding UTF-8 -d bin $(find src/main/java -name "*.java")
     java -cp bin com.recommendation.Main 8080
     ```
   - Open [http://localhost:8080](http://localhost:8080) in your browser.

---

## 📋 Pull Request Process

1. Fork the repository and create your feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Write clean, readable Java code adhering to standard Java conventions.
3. Test all API endpoints and verify UI responsiveness on both desktop and mobile viewports.
4. Commit your changes with clear, descriptive commit messages:
   ```bash
   git commit -m "feat: add matrix factorization collaborative filtering"
   ```
5. Push to your fork and submit a Pull Request to `main`.
6. Describe the problem your PR solves and how reviewers can test your changes.

---

## 🐛 Reporting Issues

- Use GitHub Issues to submit bug reports or feature requests.
- Provide a clear title, description, and steps to reproduce the issue.
- Include browser type, Java version, and any relevant console error messages.

Thank you for making this project better! 🚀
