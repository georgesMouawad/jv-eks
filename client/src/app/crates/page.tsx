'use client';

import { useState, useEffect, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { createCrate, getCrate, type Crate } from '@/lib/api';
import { getToken, clearToken } from '@/lib/auth';

export default function CratesPage() {
    const router = useRouter();
    const [crates, setCrates] = useState<Crate[]>([]);
    const [crateName, setCrateName] = useState('');
    const [crateId, setCrateId] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [creating, setCreating] = useState(false);

    const token = typeof window !== 'undefined' ? getToken() : null;

    useEffect(() => {
        if (!token) router.replace('/login');
    }, [token, router]);

    async function handleCreate(e: FormEvent) {
        e.preventDefault();
        if (!token) return;
        setError(null);
        setCreating(true);
        try {
            const crate = await createCrate(crateName, token);
            setCrates((prev) => [crate, ...prev]);
            setCrateName('');
            router.push(`/crates/${crate.id}`);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Failed to create crate');
        } finally {
            setCreating(false);
        }
    }

    async function handleOpen(e: FormEvent) {
        e.preventDefault();
        if (!token || !crateId.trim()) return;
        setError(null);
        try {
            await getCrate(crateId.trim(), token);
            router.push(`/crates/${crateId.trim()}`);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Crate not found');
        }
    }

    function handleSignOut() {
        clearToken();
        router.replace('/login');
    }

    return (
        <div className="min-h-screen p-6 max-w-2xl mx-auto space-y-8">
            <header className="flex items-center justify-between">
                <h1 className="text-2xl font-bold">CrateSync</h1>
                <button onClick={handleSignOut} className="text-sm text-gray-400 hover:text-gray-200 transition-colors">
                    Sign out
                </button>
            </header>

            {error && (
                <p className="text-sm text-red-400 bg-red-950 border border-red-800 rounded-lg px-3 py-2">{error}</p>
            )}

            {/* Create crate */}
            <section className="bg-gray-900 rounded-xl p-5 space-y-3 border border-gray-800">
                <h2 className="font-semibold text-lg">New Crate</h2>
                <form onSubmit={handleCreate} className="flex gap-2">
                    <input
                        type="text"
                        required
                        placeholder="Crate name…"
                        value={crateName}
                        onChange={(e) => setCrateName(e.target.value)}
                        className="flex-1 rounded-lg bg-gray-800 border border-gray-700 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <button
                        type="submit"
                        disabled={creating}
                        className="rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 px-4 py-2 text-sm font-semibold transition-colors"
                    >
                        {creating ? 'Creating…' : 'Create'}
                    </button>
                </form>
            </section>

            {/* Open existing crate by ID */}
            <section className="bg-gray-900 rounded-xl p-5 space-y-3 border border-gray-800">
                <h2 className="font-semibold text-lg">Open Crate by ID</h2>
                <form onSubmit={handleOpen} className="flex gap-2">
                    <input
                        type="text"
                        required
                        placeholder="Paste crate UUID…"
                        value={crateId}
                        onChange={(e) => setCrateId(e.target.value)}
                        className="flex-1 rounded-lg bg-gray-800 border border-gray-700 px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <button
                        type="submit"
                        className="rounded-lg bg-gray-700 hover:bg-gray-600 px-4 py-2 text-sm font-semibold transition-colors"
                    >
                        Open
                    </button>
                </form>
            </section>

            {/* Recently created this session */}
            {crates.length > 0 && (
                <section className="space-y-2">
                    <h2 className="font-semibold text-lg">Recent</h2>
                    <ul className="space-y-2">
                        {crates.map((c) => (
                            <li key={c.id}>
                                <Link
                                    href={`/crates/${c.id}`}
                                    className="flex items-center justify-between rounded-lg bg-gray-900 border border-gray-800 px-4 py-3 hover:border-indigo-600 transition-colors"
                                >
                                    <span className="font-medium">{c.name}</span>
                                    <span className="text-xs text-gray-500 font-mono">{c.id}</span>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </section>
            )}
        </div>
    );
}
