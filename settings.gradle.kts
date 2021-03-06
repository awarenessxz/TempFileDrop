rootProject.name = "TempFileDrop"

// order does matters
include("design4:tempfiledrop:webserver")
include("design4:storage-service")
include("design4:storage-console:webserver")
include("infra:gateway")

// archive files
include("archive:design1:webserver")
include("archive:design1:storage-service")
include("archive:design2:webserver")
include("archive:design2:storage-service")
include("archive:design3:tempfiledrop:webserver")
include("archive:design3:storage-service")
include("archive:design3:gateway")
