const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");

const isDev = !app.isPackaged;

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

ipcMain.handle("core:getHomeData", async () => null);
ipcMain.handle("core:getTopTracks", async () => null);
ipcMain.handle("core:getSingers", async () => null);
ipcMain.handle("core:getMusicians", async () => null);
ipcMain.handle("core:getModes", async () => null);
ipcMain.handle("core:getArtistDetail", async () => null);
ipcMain.handle("core:getProgramTracks", async () => null);
ipcMain.handle("core:getTrackDetail", async () => null);
ipcMain.handle("core:getSearchOptions", async () => null);
ipcMain.handle("core:searchPrograms", async () => null);

app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
