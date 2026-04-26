const { app, BrowserWindow, ipcMain } = require("electron");
const { execFile } = require("child_process");
const path = require("path");

const isDev = !app.isPackaged;
const repoRoot = path.resolve(__dirname, "..", "..");
const bridgeManifestPath = path.join(repoRoot, "core", "adapters", "electron", "Cargo.toml");
const bridgeBinaryPath = path.join(repoRoot, "core", "adapters", "electron", "target", "debug", process.platform === "win32" ? "radiogolha-electron-bridge.exe" : "radiogolha-electron-bridge");
const dbPath = path.join(repoRoot, "database", "golha_database.db");
let bridgeBuildPromise;

function execFilePromise(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    execFile(command, args, { maxBuffer: 1024 * 1024 * 20, ...options }, (error, stdout, stderr) => {
      if (error) {
        const message = stderr?.trim() || error.message;
        reject(new Error(message));
        return;
      }
      resolve(stdout);
    });
  });
}

function ensureBridgeBuilt() {
  if (!bridgeBuildPromise) {
    bridgeBuildPromise = execFilePromise("cargo", ["build", "--quiet", "--manifest-path", bridgeManifestPath], {
      cwd: repoRoot,
    });
  }
  return bridgeBuildPromise;
}

async function runCoreCommand(command, payload = {}) {
  await ensureBridgeBuilt();
  const stdout = await execFilePromise(bridgeBinaryPath, [command, JSON.stringify(payload)], {
    cwd: repoRoot,
    env: {
      ...process.env,
      RADIOGOLHA_DB_PATH: dbPath,
    },
  });
  return JSON.parse(stdout);
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 1280,
    minHeight: 800,
    backgroundColor: "#fcf9f0",
    frame: false,
    titleBarStyle: "hidden",
    trafficLightPosition: { x: 16, y: 16 },
    title: "رادیو گل‌ها",
    show: false,
    webPreferences: {
      preload: path.join(__dirname, "preload.cjs"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
    },
  });

  win.once("ready-to-show", () => win.show());

  if (isDev) {
    win.loadURL("http://127.0.0.1:5173");
  } else {
    win.loadFile(path.join(__dirname, "..", "dist", "index.html"));
  }
}

ipcMain.handle("core:getHomeData", async () => runCoreCommand("get-home-data"));
ipcMain.handle("core:getTopTracks", async (_event, limit) => runCoreCommand("get-top-tracks", { limit }));
ipcMain.handle("core:getSingers", async () => runCoreCommand("get-singers"));
ipcMain.handle("core:getMusicians", async () => runCoreCommand("get-musicians"));
ipcMain.handle("core:getModes", async () => runCoreCommand("get-modes"));
ipcMain.handle("core:getArtistDetail", async (_event, artistId) => runCoreCommand("get-artist-detail", { artistId }));
ipcMain.handle("core:getProgramTracks", async (_event, programId) => runCoreCommand("get-program-tracks", { programId }));
ipcMain.handle("core:getTrackDetail", async (_event, programId) => runCoreCommand("get-track-detail", { programId }));
ipcMain.handle("core:getSearchOptions", async () => runCoreCommand("get-search-options"));
ipcMain.handle("core:searchPrograms", async (_event, payload) => runCoreCommand("search-programs", payload));

app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
