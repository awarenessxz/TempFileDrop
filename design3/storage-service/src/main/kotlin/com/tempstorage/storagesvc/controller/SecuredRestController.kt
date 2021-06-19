package com.tempstorage.storagesvc.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement

@SecurityRequirement(name = "bearer-token")
interface SecuredRestController {

}