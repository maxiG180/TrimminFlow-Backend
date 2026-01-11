# 🔍 SonarQube Analysis - Quick Trigger

## ⚡ Option 1: Trigger GitHub Actions (Automatic)

Just push any change to trigger the CI workflow:

```bash
cd C:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend

# Make a trivial change 
echo "" >> README.md

# Commit and push
git add .
git commit -m "chore: trigger CI for SonarQube analysis"
git push origin main
```

Then check: https://github.com/maxiG180/TrimminFlow-Backend/actions

## ⚡ Option 2: Run SonarQube Locally (Fast)

If you have SONAR_TOKEN configured:

```bash
cd C:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend\demo

# Run analysis
./gradlew sonar -Dsonar.login=YOUR_SONAR_TOKEN
```

## 📊 Why CI May Not Run

**Possible Issues:**
1. ❌ GitHub Actions disabled
2. ❌ Workflow failures (check Actions tab)
3. ❌ Missing SONAR_TOKEN secret
4. ❌ SonarQube service down

## ✅ Quick Fix

**Check GitHub Actions:**
1. Go to: https://github.com/maxiG180/TrimminFlow-Backend/actions
2. Look for failed workflows
3. Re-run any failed jobs

**Or manually trigger:**
```bash
# Just push a change
git commit --allow-empty -m "trigger: sonarqube analysis"
git push origin main
```

## 🎯 Expected After Tests Added

With the new tests, your coverage should show:
- **From:** 31.3%
- **To:** ~75-85%

**The tests are there, just need SonarQube to analyze them!**
