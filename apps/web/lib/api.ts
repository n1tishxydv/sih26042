export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "") || "https://sih26042-1.onrender.com";

export interface HealthResponse {
  status: string;
  service: string;
  version: string;
  request_id?: string;
  timestamp?: string;
  offline_first?: boolean;
  philosophy?: string;
}

export interface PackSummaryResponse {
  pack_id: string;
  language_code: string;
  language_name: string;
  version: string;
  validation_status: "VALIDATED" | "EXPERIMENTAL" | "DRAFT";
  download_url?: string;
  pack_size_bytes?: number;
  checksum?: string;
}

export interface BackendStatus {
  online: boolean;
  loading: boolean;
  data: HealthResponse | null;
  error: string | null;
  lastChecked: string | null;
}

export async function checkBackendHealth(): Promise<{
  online: boolean;
  data: HealthResponse | null;
  error: string | null;
}> {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3500);

    const res = await fetch(`${API_BASE_URL}/api/v1/health`, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      cache: "no-store",
      signal: controller.signal,
    });
    clearTimeout(timeoutId);

    if (res.ok) {
      const data: HealthResponse = await res.json();
      return { online: true, data, error: null };
    }
    return {
      online: false,
      data: null,
      error: `Server responded with status ${res.status}`,
    };
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : "Network request failed";
    return { online: false, data: null, error: message };
  }
}

export async function fetchRemotePacks(): Promise<PackSummaryResponse[] | null> {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 4000);

    const res = await fetch(`${API_BASE_URL}/api/v1/packs`, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      cache: "no-store",
      signal: controller.signal,
    });
    clearTimeout(timeoutId);

    if (res.ok) {
      return await res.json();
    }
    return null;
  } catch {
    return null;
  }
}
