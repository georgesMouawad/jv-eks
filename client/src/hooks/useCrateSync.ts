'use client';

import { useEffect, useRef, useCallback } from 'react';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? 'ws://localhost:8084';

/**
 * Connects to the sync-service WebSocket for a given crateId and calls
 * onUpdate whenever the server pushes an event (i.e. a new track was added).
 *
 * Handles reconnection with exponential back-off (max 30s).
 */
export function useCrateSync(crateId: string, onUpdate: () => void): void {
    const wsRef = useRef<WebSocket | null>(null);
    const retryDelayRef = useRef(1000);
    const unmountedRef = useRef(false);

    const connect = useCallback(() => {
        if (unmountedRef.current) return;

        const ws = new WebSocket(`${WS_URL}/ws/sync/${crateId}`);
        wsRef.current = ws;

        ws.onopen = () => {
            retryDelayRef.current = 1000; // reset back-off on successful connect
        };

        ws.onmessage = () => {
            onUpdate();
        };

        ws.onclose = () => {
            if (unmountedRef.current) return;
            // Exponential back-off capped at 30s
            const delay = retryDelayRef.current;
            retryDelayRef.current = Math.min(delay * 2, 30_000);
            setTimeout(connect, delay);
        };

        ws.onerror = () => {
            ws.close();
        };
    }, [crateId, onUpdate]);

    useEffect(() => {
        unmountedRef.current = false;
        connect();
        return () => {
            unmountedRef.current = true;
            wsRef.current?.close();
        };
    }, [connect]);
}
