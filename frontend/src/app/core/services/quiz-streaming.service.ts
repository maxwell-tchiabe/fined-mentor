import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class QuizStreamingService {
    private apiUrl = environment.apiUrl;

    constructor(
        private http: HttpClient,
        private zone: NgZone
    ) { }

    /**
     * Specialized stream for JSON data that preserves all whitespace.
     * No typing delay or emission queue, emits chunks as they arrive.
     */
    getJsonStream(endpoint: string, body: any): Observable<string> {
        return new Observable<string>(observer => {
            const url = `${this.apiUrl}${endpoint}`;
            const controller = new AbortController();
            const signal = controller.signal;

            let buffer = '';

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

                    const processBuffer = (data: string): string => {
                        // Handle both \n and \r\n
                        const lines = data.split(/\r?\n/);
                        const lastLine = lines.pop() || '';

                        for (const line of lines) {
                            const trimmed = line.trim();
                            if (!trimmed) continue;

                            if (trimmed.startsWith('data:')) {
                                let content = trimmed.slice(5);
                                if (content.startsWith(' ')) {
                                    content = content.slice(1);
                                }

                                if (content && content !== '[DONE]') {
                                    // Run inside zone to ensure UI updates for every chunk
                                    this.zone.run(() => {
                                        observer.next(content);
                                    });
                                }
                            }
                        }
                        return lastLine;
                    };

                    const read = async () => {
                        try {
                            const { done, value } = await reader.read();
                            if (done) {
                                if (buffer) {
                                    processBuffer(buffer + '\n');
                                }
                                observer.complete();
                                return;
                            }

                            const chunk = decoder.decode(value, { stream: true });
                            buffer += chunk;
                            buffer = processBuffer(buffer);
                            read();
                        } catch (err) {
                            if (err instanceof Error && err.name === 'AbortError') {
                                // Handle abort
                            } else {
                                observer.error(err);
                            }
                        }
                    };

                    read();
                })
                .catch(err => {
                    if (err instanceof Error && err.name === 'AbortError') {
                        // Handle abort
                    } else {
                        observer.error(err);
                    }
                });

            return () => {
                controller.abort();
            };
        });
    }
}
