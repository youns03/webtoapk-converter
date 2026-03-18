import express from "express";
import { createServer as createViteServer } from "vite";
import path from "path";
import { fileURLToPath } from "url";
import fs from "fs";
import axios from "axios";
import dotenv from "dotenv";
import multer from "multer";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config();

// --- Hardcoded Configuration (Strict) ---
const CONFIG = {
  GITHUB_TOKEN: "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",
  GITHUB_OWNER: "youns03",
  GITHUB_REPO: "my-android-app",
  SECRET_KEY: "super-secret-key-123"
};

// Ensure uploads directory exists
const uploadDir = path.join(__dirname, "uploads");
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Configure Multer for temporary file storage
const upload = multer({ dest: "uploads/" });

// In-memory logs for debugging
const serverLogs: string[] = [];
function addServerLog(msg: string) {
  const timestamp = new Date().toLocaleTimeString();
  const log = `[${timestamp}] ${msg}`;
  serverLogs.push(log);
  if (serverLogs.length > 100) serverLogs.shift();
  console.log(log);
}

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json());

  // --- Debug & Logs ---
  app.get("/api/logs", (req, res) => {
    res.json({ logs: serverLogs });
  });

  app.get("/api/debug", async (req, res) => {
    try {
      addServerLog("Running debug check...");
      const githubCheck = await axios.get(`https://api.github.com/repos/${CONFIG.GITHUB_OWNER}/${CONFIG.GITHUB_REPO}`, {
        headers: { Authorization: `token ${CONFIG.GITHUB_TOKEN}` }
      });
      res.json({
        status: "ok",
        github: "connected",
        repo: githubCheck.data.full_name,
        config: {
          owner: CONFIG.GITHUB_OWNER,
          repo: CONFIG.GITHUB_REPO,
          token_prefix: CONFIG.GITHUB_TOKEN.substring(0, 7) + "..."
        }
      });
    } catch (error: any) {
      addServerLog(`Debug check failed: ${error.message}`);
      res.status(500).json({
        status: "error",
        message: error.message,
        details: error.response?.data || "No details"
      });
    }
  });

  // --- API Endpoints (The "Worker" Logic) ---

  // 1. Trigger GitHub Action (Now accepts file directly)
  app.post("/api/trigger", upload.single("file"), async (req, res) => {
    const { appName, buildType } = req.body;
    const file = req.file;
    const secretKey = req.headers["x-secret-key"];

    addServerLog(`Received build request for: ${appName}`);

    // التحقق من المفتاح السري
    if (secretKey !== CONFIG.SECRET_KEY) {
      if (file) fs.unlinkSync(file.path);
      addServerLog("Error: Unauthorized build attempt (Secret Key mismatch)");
      return res.status(401).json({ error: "Unauthorized", details: "Secret key mismatch on server." });
    }

    if (!file) {
      addServerLog("Error: No file uploaded in request");
      return res.status(400).json({ error: "No file uploaded" });
    }

    try {
      // Step A: Upload to transfer.sh
      addServerLog(`Uploading ${file.originalname} to transfer.sh...`);
      const fileStream = fs.createReadStream(file.path);
      const transferResponse = await axios.put(`https://transfer.sh/${file.originalname}`, fileStream, {
        headers: { "Content-Type": "application/octet-stream" },
        timeout: 30000
      });
      
      const zipUrl = transferResponse.data.trim();
      addServerLog(`File uploaded to transfer.sh: ${zipUrl}`);

      fs.unlinkSync(file.path);

      // Step B: GitHub API Call
      addServerLog(`Triggering GitHub Action: ${CONFIG.GITHUB_OWNER}/${CONFIG.GITHUB_REPO}`);
      
      const githubUrl = `https://api.github.com/repos/${CONFIG.GITHUB_OWNER}/${CONFIG.GITHUB_REPO}/actions/workflows/build-from-zip.yml/dispatches`;
      
      await axios.post(
        githubUrl,
        {
          ref: "main",
          inputs: {
            zip_url: zipUrl,
            app_name: appName,
            build_type: buildType || "webview"
          }
        },
        {
          headers: {
            Authorization: `token ${CONFIG.GITHUB_TOKEN}`,
            Accept: "application/vnd.github.v3+json",
          },
        }
      );

      addServerLog("Success: GitHub Action triggered!");
      res.json({ message: "Build triggered successfully", status: "queued", zipUrl });
    } catch (error: any) {
      if (file && fs.existsSync(file.path)) fs.unlinkSync(file.path);
      
      let errorMessage = error.message;
      let errorDetails = error.response?.data || error.message || "No additional details";
      
      if (error.response?.status === 404) {
        errorMessage = "GitHub Workflow Not Found";
        errorDetails = "The file '.github/workflows/build-from-zip.yml' was not found in the repository. Check your repo structure.";
      } else if (error.response?.status === 401) {
        errorMessage = "GitHub Token Invalid";
        errorDetails = "The token provided is invalid or does not have permission for this repo. Check your GITHUB_TOKEN.";
      } else if (error.code === 'ECONNABORTED') {
        errorMessage = "Connection Timeout";
        errorDetails = "The request to transfer.sh or GitHub timed out. Please try again.";
      }

      addServerLog(`Build Trigger Failed: ${errorMessage}`);
      console.error("Full Error Details:", error.response ? JSON.stringify(error.response.data) : error.message);
      
      res.status(error.response?.status || 500).json({ 
        error: errorMessage,
        details: typeof errorDetails === 'object' ? JSON.stringify(errorDetails) : errorDetails
      });
    }
  });

  // 2. Get Build Status
  app.get("/api/status", async (req, res) => {
    try {
      const response = await axios.get(
        `https://api.github.com/repos/${CONFIG.GITHUB_OWNER}/${CONFIG.GITHUB_REPO}/actions/runs?per_page=1`,
        {
          headers: {
            Authorization: `token ${CONFIG.GITHUB_TOKEN}`,
          },
        }
      );
      const latestRun = response.data.workflow_runs[0];
      res.json({
        id: latestRun.id,
        status: latestRun.status,
        conclusion: latestRun.conclusion,
        url: latestRun.html_url
      });
    } catch (error) {
      res.status(500).json({ error: "Failed to fetch status" });
    }
  });

  // Health Check
  app.get("/api/health", (req, res) => res.json({ status: "ok" }));

  // --- Vite Middleware ---
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
    
    // Fallback to index.html for SPA
    app.use("*", async (req, res, next) => {
      const url = req.originalUrl;
      try {
        let template = fs.readFileSync(path.resolve(__dirname, "index.html"), "utf-8");
        template = await vite.transformIndexHtml(url, template);
        res.status(200).set({ "Content-Type": "text/html" }).end(template);
      } catch (e) {
        next(e);
      }
    });
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
