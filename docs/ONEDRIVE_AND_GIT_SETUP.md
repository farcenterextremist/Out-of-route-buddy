# OneDrive + Git: Work on desktop and use version control

This guide gets the project **into OneDrive** (so it syncs to your main desktop) and **under Git** for version control on both machines.

---

## Part 1 — Put the project in OneDrive

1. **Close Cursor and Android Studio** on this machine.
2. **Find the project folder** (e.g. `C:\Users\brand\OutofRoutebuddy` or wherever it lives now).
3. **Cut** the whole folder (Ctrl+X), then **paste** it inside your **OneDrive** folder (e.g. `C:\Users\brand\OneDrive` or `OneDrive - Personal`).
4. **Reopen the project** in Cursor: **File → Open Folder** and choose the project folder **inside OneDrive**.
5. Wait for OneDrive to **finish syncing** (green checkmark on the folder). Then on your **main desktop**, open **File Explorer → OneDrive** and you’ll see the same project folder. Open that folder in Cursor (or Android Studio) on the desktop to work there.

You now have one project that lives in OneDrive and syncs to both machines.

---

## Part 2 — Use Git for version control

The project is already a **Git repo** (on branch `master`). Use Git to save history and sync between machines.

### On this machine (after moving to OneDrive)

1. **Open the project** from its **new OneDrive path** (e.g. `C:\Users\brand\OneDrive\OutofRoutebuddy`).
2. **Commit your work** when you’re at a good stopping point:
   ```bash
   git add .
   git status
   git commit -m "Describe what you did"
   ```
3. **Optional but recommended:** Add a **remote** (e.g. GitHub) so you can push and pull, and so your desktop can clone from the cloud instead of relying only on OneDrive. See Part 3.

### On your main desktop

**Option A — Open the synced OneDrive folder**

- After OneDrive has synced, on the desktop go to **OneDrive** in File Explorer and open the project folder.
- Open that folder in **Cursor** (File → Open Folder). You’re editing the same files OneDrive synced; Git history is in the same `.git` folder.
- **Commit on the desktop** when you make changes; those commits will sync back via OneDrive to this machine.
- **Tip:** Avoid editing the same files on both machines at the same time. Prefer working on one machine, commit, let OneDrive sync, then open on the other.

**Option B — Clone from GitHub (or another remote) and use push/pull**

- Create a repo on **GitHub** (or GitLab, etc.), then on **this machine** (in the OneDrive project folder):
  ```bash
  git remote add origin https://github.com/YOUR_USERNAME/OutofRoutebuddy.git
  git push -u origin master
  ```
- On the **desktop**, **clone** the repo (to any folder, e.g. Desktop or a dev folder):
  ```bash
  git clone https://github.com/YOUR_USERNAME/OutofRoutebuddy.git
  cd OutofRoutebuddy
  ```
- Open that cloned folder in Cursor on the desktop. To sync:
  - **Desktop → this machine:** On desktop: `git push`. On this machine: `git pull` (in the OneDrive project folder).
  - **This machine → desktop:** On this machine: `git push`. On desktop: `git pull`.
- You can use **either** OneDrive-synced folder **or** the cloned folder on the desktop; if you use both, keep them in sync with `git pull` / `git push` so you don’t get confused.

---

## Part 3 — Add a Git remote (GitHub) — your setup

**GitHub account:** Brandonfrey2work@gmail.com  
**GitHub username:** `farcenterextremist`

### Step 1 — Create the repo on GitHub

1. Go to [github.com/new](https://github.com/new) and sign in (with Brandonfrey2work@gmail.com or your GitHub account).
2. **Repository name:** `OutofRoutebuddy` (or `OutOfRouteBuddy`).
3. **Public.** Do **not** check “Add a README” or “Add .gitignore” — the project already has them.
4. Click **Create repository.**

### Step 2 — Add remote and push (this machine)

In your project folder (terminal in Cursor or PowerShell), run these in order:

```bash
git remote add origin https://github.com/farcenterextremist/OutofRoutebuddy.git
git add .
git status
git commit -m "Initial commit: OutOfRouteBuddy with OneDrive + Git setup"
git push -u origin master
```

If you already added a remote named `origin`, use:  
`git remote set-url origin https://github.com/farcenterextremist/OutofRoutebuddy.git`  
then push.

### Step 3 — On your main desktop

```bash
git clone https://github.com/farcenterextremist/OutofRoutebuddy.git
cd OutofRoutebuddy
```

Then open the `OutofRoutebuddy` folder in Cursor. To sync: **push** from the machine where you worked, **pull** on the other (`git pull`).

---

## What’s ignored by Git (won’t sync via Git)

These stay local and are **not** committed (so they don’t go to GitHub or other remotes):

- `local.properties` (SDK path)
- `build/`, `.gradle/` (build output and cache)
- `.idea/` workspace files

So: **OneDrive** can sync the whole folder (including those files) between machines. **Git** syncs only tracked files and history. If you use GitHub, the desktop clone won’t have `.env` or `local.properties` until you add them locally (or copy from the other machine); that’s intentional for secrets and machine-specific paths.

---

## Quick reference

| Goal | Action |
|------|--------|
| Work on desktop | Open the project from **OneDrive** on the desktop, or **clone from GitHub** and open that folder. |
| Save progress (either machine) | `git add .` → `git commit -m "message"` |
| Sync via Git (if using remote) | Push on one machine: `git push`. Pull on the other: `git pull`. |
| Sync via OneDrive | Save and close; OneDrive syncs the folder. Then open on the other machine. |

If you use **both** OneDrive and a Git remote: keep the OneDrive project folder and the desktop clone in sync by doing `git pull` before you switch machines and `git push` when you’re done.
