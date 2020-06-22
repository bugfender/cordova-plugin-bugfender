/**
 * @name Bugfender
 * @description
 * A Cordova/Ionic plugin to send logs to the Bugfender service.
 *
 * @usage
 * ```typescript
 * import { Bugfender } from 'cordova-plugin-bugfender/www/bugfender';
 *
 * Bugfender.log('hello world!')
 */
export declare class Bugfender {
    static forceSendOnce(): void;
    static removeDeviceKey(key: string): void;
    static sendIssue(title: string, markdown: string, callback?: (url: string) => void): void;
    static setDeviceKey(key: string, value: string): void;
    static setForceEnabled(enabled: boolean): void;
    static setMaximumLocalStorageSize(bytes: number): void;
    static log(...details: any[]): void;
    static fatal(...details: any[]): void;
    static error(...details: any[]): void;
    static warn(...details: any[]): void;
    static info(...details: any[]): void;
    static debug(...details: any[]): void;
    static trace(...details: any[]): void;
    private static printToConsole;
    static setPrintToConsole(print: boolean): void;
    static getPrintToConsole(): boolean;
    private static logWithLevel;
    private static notLoadedWarningShown;
    private static checkLoaded;
    static getDeviceUrl(callback: (url: string) => void): void;
    static getSessionUrl(callback: (url: string) => void): void;
    static showUserFeedbackUI(title: string, hint: string, subjectHint: string, messageHint: string, sendButtonText: string, cancelButtonText: string, callback?: (sent: boolean) => void): void;
    static sendCrash(title: string, markdown: string, callback?: (url: string) => void): void;
    static sendUserFeedback(title: string, markdown: string, callback?: (url: string) => void): void;
}
