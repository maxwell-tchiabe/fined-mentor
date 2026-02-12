import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class StreamingService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    /**
     * Streams data from an SSE endpoint using POST.
     * Standard EventSource only supports GET, so we use fetch with a ReadableStream.
     * @param endpoint The API endpoint (relative to base URL)
     * @param body The request body
     * @returns An Observable that emits chunks of strings
     */
    getStream(endpoint: string, body: any): Observable<string> {
        return new Observable<string>(observer => {
            const url = `${this.apiUrl}${endpoint}`;
            const controller = new AbortController();
            const signal = controller.signal;

            let buffer = '';
            let emissionQueue: string[] = [];
            let isEmitting = false;

            // Modern feel parameters
            const MIN_DELAY = 5;  // ms for fast typing
            const MAX_DELAY = 20; // ms for natural typing

            const emitFromQueue = () => {
                if (emissionQueue.length === 0) {
                    isEmitting = false;
                    return;
                }
                isEmitting = true;

                // Adjust delay based on backlog: if backlog is large, type faster
                const currentDelay = emissionQueue.length > 30 ? MIN_DELAY : MAX_DELAY;

                const char = emissionQueue.shift();
                if (char) {
                    observer.next(char);
                }
                setTimeout(emitFromQueue, currentDelay);
            };

            const queueText = (text: string) => {
                emissionQueue.push(...text.split(''));
                if (!isEmitting) {
                    emitFromQueue();
                }
            };

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
                credentials: 'include',
                signal
            })
                .then(async response => {
                    if (!response.ok) {
                        const errorMsg = await response.text().catch(() => response.statusText);
                        observer.error(new Error(`HTTP ${response.status}: ${errorMsg}`));
                        return;
                    }

                    const reader = response.body?.getReader();
                    if (!reader) {
                        observer.error(new Error('ReadableStream not supported'));
                        return;
                    }

                    const decoder = new TextDecoder();

                    const read = async () => {
                        try {
                            const { done, value } = await reader.read();
                            if (done) {
                                if (buffer.trim()) processLines(buffer + '\n');
                                checkCompletion();
                                return;
                            }

                            const chunk = decoder.decode(value, { stream: true });
                            buffer += chunk;
                            buffer = processLines(buffer);
                            read();
                        } catch (err) {
                            observer.error(err);
                        }
                    };

                    const processLines = (data: string): string => {
                        const lines = data.split('\n');
                        const lastLine = lines.pop() || '';

                        for (const line of lines) {
                            const trimmed = line.trim();
                            if (!trimmed) continue;

                            let content = '';
                            if (trimmed.startsWith('data:')) {
                                content = trimmed.slice(5).trim();
                            } else {
                                content = trimmed;
                            }

                            if (content && content !== '[DONE]') {
                                queueText(content);
                            }
                        }
                        return lastLine;
                    };

                    const checkCompletion = () => {
                        if (emissionQueue.length === 0) {
                            observer.complete();
                        } else {
                            setTimeout(checkCompletion, 100);
                        }
                    };

                    read();
                })
                .catch(err => observer.error(err));

            return () => {
                controller.abort();
                emissionQueue = [];
                isEmitting = false;
            };
        });
    }
}
