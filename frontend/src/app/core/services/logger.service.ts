import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class LoggerService {
    private readonly enabled = environment.enableLogging;

    log(message: any, ...optionalParams: any[]): void {
        if (this.enabled) {
            console.log(message, ...optionalParams);
        }
    }

    error(message: any, ...optionalParams: any[]): void {
        // In production, we might want to log errors even if logging is disabled, 
        // or send them to an external service. For now, we'll keep it simple.
        console.error(message, ...optionalParams);
    }

    warn(message: any, ...optionalParams: any[]): void {
        if (this.enabled) {
            console.warn(message, ...optionalParams);
        }
    }

    debug(message: any, ...optionalParams: any[]): void {
        if (this.enabled) {
            console.debug(message, ...optionalParams);
        }
    }
}
