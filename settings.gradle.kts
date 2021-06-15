rootProject.name = "TempFileDrop"

// order does matters
include("webserver")
include("storage-service")

// archive files
include("archive:design1:webserver")
include("archive:design1:storage-service")