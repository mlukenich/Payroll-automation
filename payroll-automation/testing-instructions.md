# IDE-Specific Setup: Payroll AI Agent

Since you are working in an IDE (IntelliJ/Antigravity), follow these steps to run the agent.

## 1. Setting the API Key in the IDE (Easiest)
1.  Open **PayrollAutomationApplication.java**.
2.  Right-click the file and select **"Edit Run Configuration"**.
3.  Find the **Environment Variables** section.
4.  Add: `GOOGLE_API_KEY=your-actual-key-here`.
5.  Save and click the **Run** (Play) button.

## 2. Using the IDE Terminal
If you use the Terminal tab inside the IDE, use these one-liners to ensure the key is active when the app starts:

### If the terminal is PowerShell (PS):
```powershell
$env:GOOGLE_API_KEY="your-key"; cd payroll-automation; .\mvnw spring-boot:run
```

### If the terminal is CMD:
```cmd
set GOOGLE_API_KEY=your-key && cd payroll-automation && mvnw spring-boot:run
```

---

## 3. Verifying the Agent
Once the app is running:
1.  Go to `http://localhost:8080` in your browser.
2.  Type: *"Pay Jane Doe 40 hours for Feb 1 to Feb 15. Check date is Feb 20."*
3.  **Crucial Detail:** The AI (using `gemini-2.5-flash-lite`) **MUST** show you a preview first. If it tries to submit without asking "Is this correct?", the review logic is failing.
4.  Confirm the preview by typing *"Yes"*.
5.  Watch the IDE console for the log: `AI Tool: Submitting payroll`.
