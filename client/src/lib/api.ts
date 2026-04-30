const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
    token: string;
    expiresIn: number;
}

export async function register(username: string, email: string, password: string): Promise<AuthResponse> {
    return request('/api/auth/register', {
        method: 'POST',
        body: { username, email, password },
    });
}

export async function login(email: string, password: string): Promise<AuthResponse> {
    return request('/api/auth/login', {
        method: 'POST',
        body: { email, password },
    });
}

// ─── Crates ──────────────────────────────────────────────────────────────────

export interface CrateItem {
    id: string;
    crateId: string;
    trackName: string;
    s3Key: string;
    addedBy: string;
    addedAt: string;
}

export interface Crate {
    id: string;
    name: string;
    ownerId: string;
    createdAt: string;
    items: CrateItem[];
}

export interface UploadUrlResponse {
    presignedUrl: string;
    s3Key: string;
}

export async function createCrate(name: string, token: string): Promise<Crate> {
    return request('/api/crates', {
        method: 'POST',
        body: { name },
        token,
    });
}

export async function getCrate(id: string, token: string): Promise<Crate> {
    return request(`/api/crates/${id}`, { token });
}

export async function getUploadUrl(crateId: string, trackName: string, token: string): Promise<UploadUrlResponse> {
    return request(`/api/crates/${crateId}/upload-url?trackName=${encodeURIComponent(trackName)}`, { token });
}

export async function confirmUpload(
    crateId: string,
    trackName: string,
    s3Key: string,
    token: string,
): Promise<CrateItem> {
    return request(`/api/crates/${crateId}/items`, {
        method: 'POST',
        body: { trackName, s3Key },
        token,
    });
}

// ─── S3 Direct Upload ────────────────────────────────────────────────────────

/**
 * Uploads a file directly to S3 using the pre-signed PUT URL.
 * No auth header — S3 uses the pre-signed URL credentials.
 */
export async function uploadToS3(presignedUrl: string, file: File): Promise<void> {
    const res = await fetch(presignedUrl, {
        method: 'PUT',
        body: file,
        headers: { 'Content-Type': file.type || 'application/octet-stream' },
    });
    if (!res.ok) {
        throw new Error(`S3 upload failed: ${res.status} ${res.statusText}`);
    }
}

// ─── Internal fetch helper ───────────────────────────────────────────────────

interface RequestOptions {
    method?: string;
    body?: unknown;
    token?: string;
}

async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };
    if (opts.token) headers['Authorization'] = `Bearer ${opts.token}`;

    const res = await fetch(`${API_URL}${path}`, {
        method: opts.method ?? 'GET',
        headers,
        body: opts.body != null ? JSON.stringify(opts.body) : undefined,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => res.statusText);
        throw new Error(`${res.status}: ${text}`);
    }

    return res.json() as Promise<T>;
}
