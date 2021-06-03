rootProject.name = "TempFileDrop"

// order does matters
include("webserver")
include("storage-service")

// archive files
include("archive:webserver")
include("archive:storage-service")