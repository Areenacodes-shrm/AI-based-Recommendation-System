# 📤 Step-by-Step Guide: Uploading to GitHub

This guide covers 3 easy ways to upload your **AI-Based Recommendation System** project to a new GitHub repository.

The project files are located at:
`C:\Users\asus\.gemini\antigravity\scratch\ai-recommendation-system`

---

## 🌟 Method 1: Upload via GitHub Web Interface (Easiest & Fastest)

No installation required!

1. Go to [github.com](https://github.com) and log in.
2. Click the **`+`** icon in the top right corner and select **New repository**.
3. Name your repository (e.g. `ai-recommendation-system`).
4. Set visibility to **Public** or **Private**.
5. Leave **"Initialize with README"** unchecked (you already have a professional README).
6. Click **Create repository**.
7. On the repository page, click the **"uploading an existing file"** link.
8. Open File Explorer to:
   ```
   C:\Users\asus\.gemini\antigravity\scratch\ai-recommendation-system
   ```
9. Drag and drop the following files and folders into the GitHub upload area:
   - `src/` (contains all Java source code and web files)
   - `.github/` (contains CI workflow)
   - `pom.xml`
   - `README.md`
   - `LICENSE`
   - `CONTRIBUTING.md`
   - `SECURITY.md`
   - `Dockerfile`
   - `docker-compose.yml`
   - `.gitignore`
   - `run.bat`, `run.ps1`, `run-jar.bat`
10. At the bottom, type a commit message (e.g. *"Initial commit: AI-Based Recommendation System"*) and click **Commit changes**.

---

## 🖥️ Method 2: Upload via GitHub Desktop (Recommended for GUI Users)

1. Download and install [GitHub Desktop](https://desktop.github.com/) if not already installed.
2. Open GitHub Desktop and log in with your GitHub account.
3. Click **File** ➔ **Add Local Repository...** (or press `Ctrl + O`).
4. Browse and select the folder:
   `C:\Users\asus\.gemini\antigravity\scratch\ai-recommendation-system`
5. If prompted *"This directory does not appear to be a Git repository"*, click **"create a repository"**.
6. Set the Name to `ai-recommendation-system` and click **Create Repository**.
7. In the left panel, you'll see all your project files. Type a commit message:
   `Initial commit: AI-Based Recommendation System`
8. Click **Commit to main**.
9. Click **Publish repository** in the top bar to push it to your GitHub account!

---

## 💻 Method 3: Upload via Git Command Line (CLI)

If you have Git installed on your system:

1. Open PowerShell or Command Prompt.
2. Navigate to your project folder:
   ```powershell
   cd C:\Users\asus\.gemini\antigravity\scratch\ai-recommendation-system
   ```
3. Initialize the git repository:
   ```powershell
   git init -b main
   ```
4. Add all project files:
   ```powershell
   git add .
   ```
5. Commit the files:
   ```powershell
   git commit -m "feat: initial commit - AI-based recommendation system"
   ```
6. Link your local repository to your remote GitHub repository (replace with your repo URL):
   ```powershell
   git remote add origin https://github.com/<YOUR-USERNAME>/ai-recommendation-system.git
   ```
7. Push the code to GitHub:
   ```powershell
   git push -u origin main
   ```

---

## ✅ Files Included in Your Repository

| File / Folder | Purpose |
|---|---|
| `src/main/java/` | Full Java backend (Recommendation Engine, Models, Server, Analytics) |
| `src/main/resources/static/` | Web frontend (HTML5, CSS3, Vanilla JS, Dashboards) |
| `pom.xml` | Maven build & dependency configuration |
| `Dockerfile` | Multi-stage Docker build for cloud / container deployment |
| `docker-compose.yml` | 1-command deployment configuration |
| `.github/workflows/ci.yml` | GitHub Actions automated build and test pipeline |
| `README.md` | Complete documentation with architecture diagrams, formulas, API docs |
| `LICENSE` | MIT License |
| `CONTRIBUTING.md` | Guide for open-source contributors |
| `SECURITY.md` | Security vulnerability reporting policy |
| `.gitignore` | Excludes unnecessary build artifacts (`bin/`, `target/`, IDE files) |
| `run.bat`, `run.ps1`, `run-jar.bat` | 1-click execution scripts for Windows users |
