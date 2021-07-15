package com.tempstorage.gateway.util

import org.slf4j.LoggerFactory

object StorageAttributesUtils {
    const val STORAGE_HEADER_BUCKETS = "x-storage-buckets"
    const val STORAGE_HEADER_ROUTING_KEYS = "x-storage-routing-keys"
    private const val STORAGE_ROLES_PREFIX = "storagesvc_user_"
    private const val STORAGE_ATTRS_BUCKETS = "buckets"
    private const val STORAGE_ATTRS_ROUTING_KEYS = "routing-keys"
    private val logger = LoggerFactory.getLogger(StorageAttributesUtils::class.java)

    fun injectStorageAttributes(permissions: List<String>, attributes: Map<String, Map<String, List<String>>>): Pair<List<String>, List<String>> {
        val buckets = ArrayList<String>()
        val routingKeys = ArrayList<String>()
        for (permission in permissions) {
            if (permission.startsWith(STORAGE_ROLES_PREFIX)) {
                val key = permission.removePrefix(STORAGE_ROLES_PREFIX)
                if (key in attributes) {
                    val attrs = attributes[key]
                    logger.info("Extracted Attributes --> $attrs")
                    attrs?.let {
                        val tempBuckets = attrs[STORAGE_ATTRS_BUCKETS]
                        tempBuckets?.let {
                            if (tempBuckets.isNotEmpty()) {
                                buckets.addAll(tempBuckets)
                            }
                        }
                        val tempRoutingKeys = attrs[STORAGE_ATTRS_ROUTING_KEYS]
                        tempRoutingKeys?.let {
                            if (tempRoutingKeys.isNotEmpty()) {
                                routingKeys.addAll(tempRoutingKeys)
                            }
                        }
                    }
                }
            }
        }
        return Pair(buckets, routingKeys)
    }

}