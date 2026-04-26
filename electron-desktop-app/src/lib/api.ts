import type { BootstrapPayload } from "./types";

export async function getBootstrapPayload(): Promise<BootstrapPayload> {
  return {
    appName: "رادیو گل‌ها",
    programs: [],
    tracks: [],
  };
}
