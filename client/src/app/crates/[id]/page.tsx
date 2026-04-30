'use client';

import { useState, useEffect, useCallback, useRef, ChangeEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { getCrate, getUploadUrl, uploadToS3, confirmUpload, type Crate } from '@/lib/api';
import { getToken } from '@/lib/auth';
import { useCrateSync } from '@/hooks/useCrateSync';

type UploadStatus = 'idle' | 'presigning' | 'uploading' | 'confirming' | 'done' | 'error';

export default function CrateDetailPage() {
    const router = useRouter();
    const params = useParams<{ id: string }>();
    const crateId = params.id;

    const [crate, setCrate] = useState<Crate | null>(null);
    const [loadError, setLoadError] = useState<string | null>(null);

    // Upload state
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [file, setFile] = useState<File | null>(null);
    const [uploadStatus, setUploadStatus] = useState<UploadStatus>('idle');
    const [uploadError, setUploadError] = useState<string | null>(null);
    const [uploadProgress, setUploadProgress] = useState<string>('');

    const token = typeof window !== 'undefined' ? getToken() : null;

    // ── Load crate data ───────────────────────────────────────────────────────

    const loadCrate = useCallback(async () => {
        if (!token) return;
        try {
            const data = await getCrate(crateId, token);
            setCrate(data);
            setLoadError(null);
        } catch (err: unknown) {
            setLoadError(err instanceof Error ? err.message : 'Failed to load crate');
        }
    }, [crateId, token]);

    useEffect(() => {
        if (!token) {
            router.replace('/login');
            return;
        }
        loadCrate();
    }, [token, router, loadCrate]);

    // ── Live sync via WebSocket ───────────────────────────────────────────────

    // Refreshes the crate data whenever the sync-service pushes an event
    useCrateSync(crateId, loadCrate);

    // ── Upload flow ───────────────────────────────────────────────────────────

    function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
        setFile(e.target.files?.[0] ?? null);
        setUploadStatus('idle');
        setUploadError(null);
    }

    async function handleUpload() {
        if (!file || !token) return;
        setUploadError(null);

        try {
            // Step 1 — get pre-signed URL from crate-service
            setUploadStatus('presigning');
            setUploadProgress('Requesting upload URL…');
            const { presignedUrl, s3Key } = await getUploadUrl(crateId, file.name, token);

            // Step 2 — PUT file directly to S3 (no auth header, URL is self-signed)
            setUploadStatus('uploading');
            setUploadProgress('Uploading to S3…');
            await uploadToS3(presignedUrl, file);

            // Step 3 — confirm upload to crate-service so it saves the metadata and
            //           publishes the Redis event that notifies all sync-service clients
            setUploadStatus('confirming');
            setUploadProgress('Confirming upload…');
            await confirmUpload(crateId, file.name, s3Key, token);

            setUploadStatus('done');
            setUploadProgress('Upload complete!');
            setFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';

            // Eagerly refresh — the WebSocket will also trigger loadCrate shortly
            await loadCrate();
        } catch (err: unknown) {
            setUploadStatus('error');
            setUploadError(err instanceof Error ? err.message : 'Upload failed');
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    if (loadError) {
        return (
            <div className="min-h-screen flex items-center justify-center p-6">
                <div className="text-center space-y-4">
                    <p className="text-red-400">{loadError}</p>
                    <Link href="/crates" className="text-indigo-400 hover:underline text-sm">
                        ← Back to crates
                    </Link>
                </div>
            </div>
        );
    }

    if (!crate) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <p className="text-gray-500 text-sm animate-pulse">Loading crate…</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen p-6 max-w-2xl mx-auto space-y-8">
            <header className="flex items-center gap-3">
                <Link href="/crates" className="text-gray-400 hover:text-gray-200 transition-colors text-sm">
                    ← Crates
                </Link>
                <h1 className="text-2xl font-bold">{crate.name}</h1>
            </header>

            <p className="text-xs text-gray-500 font-mono -mt-4">{crate.id}</p>

            {/* ── Upload section ──────────────────────────────────────────── */}
            <section className="bg-gray-900 rounded-xl p-5 space-y-4 border border-gray-800">
                <h2 className="font-semibold">Add Track</h2>
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="audio/*"
                        onChange={handleFileChange}
                        className="text-sm text-gray-300 file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:bg-gray-700 file:text-sm file:font-medium file:text-gray-200 hover:file:bg-gray-600 cursor-pointer"
                    />
                    <button
                        onClick={handleUpload}
                        disabled={!file || ['presigning', 'uploading', 'confirming'].includes(uploadStatus)}
                        className="rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 px-4 py-2 text-sm font-semibold transition-colors whitespace-nowrap"
                    >
                        {['presigning', 'uploading', 'confirming'].includes(uploadStatus) ? uploadProgress : 'Upload'}
                    </button>
                </div>
                {uploadStatus === 'done' && <p className="text-sm text-green-400">{uploadProgress}</p>}
                {uploadError && (
                    <p className="text-sm text-red-400 bg-red-950 border border-red-800 rounded-lg px-3 py-2">
                        {uploadError}
                    </p>
                )}
            </section>

            {/* ── Track list ──────────────────────────────────────────────── */}
            <section className="space-y-3">
                <div className="flex items-center justify-between">
                    <h2 className="font-semibold">Tracks</h2>
                    <span className="text-xs text-gray-500">
                        {crate.items.length} {crate.items.length === 1 ? 'track' : 'tracks'} · live
                    </span>
                </div>

                {crate.items.length === 0 ? (
                    <p className="text-sm text-gray-500 italic">No tracks yet. Upload the first one!</p>
                ) : (
                    <ul className="space-y-2">
                        {crate.items.map((item) => (
                            <li
                                key={item.id}
                                className="flex items-center justify-between rounded-lg bg-gray-900 border border-gray-800 px-4 py-3"
                            >
                                <div>
                                    <p className="text-sm font-medium">{item.trackName}</p>
                                    <p className="text-xs text-gray-500 mt-0.5">
                                        Added {new Date(item.addedAt).toLocaleString()}
                                    </p>
                                </div>
                                <span className="text-xs text-gray-600 font-mono hidden sm:block truncate max-w-[200px]">
                                    {item.s3Key}
                                </span>
                            </li>
                        ))}
                    </ul>
                )}
            </section>
        </div>
    );
}
