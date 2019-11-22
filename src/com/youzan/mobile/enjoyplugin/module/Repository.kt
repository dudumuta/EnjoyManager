package com.youzan.mobile.enjoyplugin.module

class Repository(
        var choose: Boolean,
        var name: String,
        var version: String,
        var uninstall: Boolean
)

class Resp(
        val created_at: String,
        val name: String,
        val published_at: String,
        val tag_name: String
)

