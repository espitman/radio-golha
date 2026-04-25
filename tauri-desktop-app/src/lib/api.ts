import { invoke } from "@tauri-apps/api/core";
import type { BootstrapPayload } from "./types";

export async function getBootstrapPayload(): Promise<BootstrapPayload> {
  return invoke<BootstrapPayload>("get_bootstrap_payload");
}
