const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("radioGolhaCore", {
  getHomeData: () => ipcRenderer.invoke("core:getHomeData"),
  getTopTracks: (limit) => ipcRenderer.invoke("core:getTopTracks", limit),
  getSingers: () => ipcRenderer.invoke("core:getSingers"),
  getMusicians: () => ipcRenderer.invoke("core:getMusicians"),
  getModes: () => ipcRenderer.invoke("core:getModes"),
  getArtistDetail: (artistId) => ipcRenderer.invoke("core:getArtistDetail", artistId),
  getProgramTracks: (programId) => ipcRenderer.invoke("core:getProgramTracks", programId),
  getTrackDetail: (programId) => ipcRenderer.invoke("core:getTrackDetail", programId),
  getSearchOptions: () => ipcRenderer.invoke("core:getSearchOptions"),
  searchPrograms: (payload) => ipcRenderer.invoke("core:searchPrograms", payload),
});
