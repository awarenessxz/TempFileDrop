import { Moment } from "moment";

export const NotificationWebSocketEndpoint = "/storage-ws";

export enum NotificationWebSocketTopics {
    FILES_UPLOADED= "/topic/file-uploaded",
    FILES_DOWNLOADED= "/topic/file-downloaded",
    FILES_DELETED= "/topic/file-deleted"
}

export enum EventType {
    FILES_UPLOADED = "FILES_UPLOADED",
    FILES_DOWNLOADED = "FILES_DOWNLOADED",
    FILES_DELETED = "FILES_DELETED"
}

export interface NotificationMessage {
    eventTime: Moment,
    eventType: EventType,
    bucket: string,
    objectName: string
}