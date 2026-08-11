package com.zzyihao.stk.data.project

object ProjectPaging {
    fun merge(current: List<ProjectSummary>, next: List<ProjectSummary>): List<ProjectSummary> =
        (current + next).distinctBy { it.id }

    fun isAllowedExternalUrl(value: String, allowHosts: Set<String> = setOf("stk.zz-yihao.com")): Boolean =
        runCatching {
            val uri = java.net.URI(value)
            uri.scheme.equals("https", ignoreCase = true) &&
                uri.host?.lowercase() in allowHosts.map { it.lowercase() }.toSet()
        }.getOrDefault(false)

    fun isValidContactTarget(contact: ProjectContact, allowHosts: Set<String> = setOf("stk.zz-yihao.com")): Boolean =
        when (contact.type.lowercase()) {
            "phone" -> contact.value.matches(Regex("^\\+?[0-9 -]{6,20}$"))
            "wechat" -> contact.value.matches(Regex("^[A-Za-z][A-Za-z0-9_-]{5,19}$"))
            "qq" -> contact.value.matches(Regex("^[1-9][0-9]{4,11}$"))
            "url" -> isAllowedExternalUrl(contact.value, allowHosts)
            else -> false
        }
}
