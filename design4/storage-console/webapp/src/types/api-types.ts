import { Moment } from "moment";

export interface UserTokenResponse {
    name: string;
    username: string;
    roles: string[];
    token: string;
    buckets: string[];
    routingKeys: string[];
}

export interface UserToken extends UserTokenResponse{
    isAdmin: boolean;
}

export interface FileSystemNode {
    isFile: boolean;
    label: string;
    storageSize: number;
    storageFullPath: string;
    storageBucket: string;
    storageDownloadLeft: number;
    storageExpiryDatetime?: Moment | null;
    children: FileSystemNode[];
}

export interface EventData {
    bucket: string;
    objectName: string;
    eventType: string;
    publishedDateTime: Moment;
}

export enum SchedulerJobStatus {
    COMPLETED = "COMPLETED",
    COMPLETED_WITH_ERROR = "COMPLETED_WITH_ERROR",
    PAUSED = "PAUSED",
    PENDING = "PENDING",
    RESCHEDULED = "RESCHEDULED",
    RESUMED = "RESUMED",
    RUNNING = "RUNNING",
    SCHEDULED = "SCHEDULED"
}

export enum SchedulerJobType {
    MONITOR_OBJECT = "MONITOR_OBJECT",
    MONITOR_OBJECT_CRONJOB = "MONITOR_OBJECT_CRONJOB"
}

export interface SchedulerJob {
    jobType: SchedulerJobType;
    jobName: string;
    description: string;
    cronExpression: string;
    startTime: Moment;
}

export interface WatchListJob {
    jobType: string;
    jobName: string;
    description: string;
    cronExpression: string;
    jobStatus: SchedulerJobStatus;
    isObjectValid: boolean;
}
