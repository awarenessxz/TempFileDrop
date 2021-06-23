rootProject.name = "TempFileDrop"

// order does matters
include("design3:tempfiledrop:webserver")
include("design3:storage-service")
include("infra:gateway")

// archive files
include("archive:design1:webserver")
include("archive:design1:storage-service")
include("archive:design2:webserver")
include("archive:design2:storage-service")
