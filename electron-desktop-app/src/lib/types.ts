export type HomeProgram = {
  id: number;
  title: string;
  count: number;
};

export type HomeTrack = {
  id: number;
  title: string;
  artist: string;
  duration: string;
};

export type BootstrapPayload = {
  appName: string;
  programs: HomeProgram[];
  tracks: HomeTrack[];
};
